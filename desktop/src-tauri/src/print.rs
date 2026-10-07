use std::fs;
use std::sync::atomic::{AtomicU64, Ordering};

use tauri::{AppHandle, Manager, WebviewUrl, WebviewWindowBuilder, WindowEvent};

/// 内置模板白名单（desktop.md D2：处方笺 A5 / 小票 80mm）
fn builtin_template(name: &str) -> Option<&'static str> {
    match name {
        "prescription" => Some(include_str!("../templates/prescription.html")),
        "receipt" => Some(include_str!("../templates/receipt.html")),
        _ => None,
    }
}

/// 极简模板填充：
/// - `<!-- BEGIN key -->…<!-- END key -->` 段按 data[key] 数组逐项重复（子项内 {{字段}} 再填充）；
/// - `{{key}}` 替换为标量值，对象/数组转 JSON 文本，缺省置空。
pub fn render(name: &str, data: &serde_json::Value) -> Result<String, String> {
    let raw = builtin_template(name)
        .ok_or_else(|| format!("未知模板：{name}（可用：prescription / receipt）"))?;
    Ok(fill_sections(raw, data))
}

fn fill_sections(raw: &str, data: &serde_json::Value) -> String {
    let mut out = String::with_capacity(raw.len());
    let mut rest = raw;
    while let Some(begin) = find_marker(rest, "BEGIN") {
        let (head, after_begin) = rest.split_at(begin);
        out.push_str(head);
        let after_begin = &after_begin["<!-- BEGIN ".len()..];
        let end_tag_idx = match after_begin.find("-->") {
            Some(i) => i,
            None => {
                out.push_str("<!-- BEGIN");
                out.push_str(after_begin);
                rest = "";
                break;
            }
        };
        let key = after_begin[..end_tag_idx].trim();
        let after_key = &after_begin[end_tag_idx + "-->".len()..];
        let end_marker = format!("<!-- END {key} -->");
        let (body, tail) = match after_key.find(&end_marker) {
            Some(i) => (
                &after_key[..i],
                &after_key[i + end_marker.len()..],
            ),
            None => {
                out.push_str(after_key);
                rest = "";
                break;
            }
        };
        if let Some(items) = data.get(key).and_then(|v| v.as_array()) {
            for item in items {
                out.push_str(&fill_inline(body, item));
            }
        }
        rest = tail;
    }
    out.push_str(rest);
    fill_inline(&out, data)
}

fn find_marker(text: &str, marker: &str) -> Option<usize> {
    text.find(&format!("<!-- {marker} "))
}

fn fill_inline(text: &str, data: &serde_json::Value) -> String {
    let mut out = text.to_string();
    if let Some(obj) = data.as_object() {
        for (key, value) in obj {
            let token = format!("{{{{{key}}}}}");
            let rendered = match value {
                serde_json::Value::String(s) => s.clone(),
                serde_json::Value::Null => String::new(),
                other => other.to_string(),
            };
            out = out.replace(&token, &rendered);
        }
    }
    // 数据里没有的占位符置空，避免样张上留 {{key}} 痕迹
    while let Some(start) = out.find("{{") {
        let Some(end_rel) = out[start..].find("}}") else { break };
        let end = start + end_rel + 2;
        out.replace_range(start..end, "");
    }
    out
}

/// 打印引导脚本：webview 加载后拉起系统打印对话框（打印机选择交驱动），打印/取消后自关窗口。
const PRINT_BOOTSTRAP: &str =
    "<script>window.addEventListener('afterprint',function(){window.close()});setTimeout(function(){window.print()},400)</script>";

/// 渲染结果 + 打印引导脚本（print_html 落盘的就是它）
pub fn printable_document(name: &str, data: &serde_json::Value) -> Result<String, String> {
    Ok(format!("{}\n{PRINT_BOOTSTRAP}", render(name, data)?))
}

static PRINT_SEQ: AtomicU64 = AtomicU64::new(0);

/// 打开隐藏打印窗口：渲染模板落盘 app_cache_dir/prints/，窗口销毁时清临时文件。
#[tauri::command]
pub fn print_html(app: AppHandle, template: String, data: serde_json::Value) -> Result<String, String> {
    let html = printable_document(&template, &data)?;
    let dir = app
        .path()
        .app_cache_dir()
        .map_err(|e| format!("缓存目录不可用：{e}"))?
        .join("prints");
    fs::create_dir_all(&dir).map_err(|e| format!("创建打印目录失败：{e}"))?;
    let seq = PRINT_SEQ.fetch_add(1, Ordering::Relaxed);
    let path = dir.join(format!("{template}-{seq}.html"));
    fs::write(&path, html).map_err(|e| format!("写打印文件失败：{e}"))?;
    let url = tauri::Url::from_file_path(&path)
        .map_err(|_| format!("打印文件地址不可用：{}", path.display()))?;

    let label = format!("print-{template}-{seq}");
    let window = WebviewWindowBuilder::new(&app, &label, WebviewUrl::External(url))
        .title("打印")
        .visible(false)
        .inner_size(420.0, 560.0)
        .build()
        .map_err(|e| format!("打开打印窗口失败：{e}"))?;
    let temp_path = path.clone();
    window.on_window_event(move |event| {
        if matches!(event, WindowEvent::Destroyed) {
            let _ = fs::remove_file(&temp_path);
        }
    });
    Ok(path.display().to_string())
}

#[cfg(test)]
mod tests {
    use super::*;
    use serde_json::json;

    #[test]
    fn unknown_template_rejected() {
        assert!(render("nope", &json!({})).is_err());
        assert!(render("../../etc/passwd", &json!({})).is_err());
    }

    #[test]
    fn placeholders_replaced_and_missing_blank() {
        let html = printable_document(
            "receipt",
            &json!({
                "clinicName": "示例医馆",
                "orderNo": "12",
                "customerName": "测试客人",
                "date": "2026-10-08 10:20",
                "items": [
                    { "name": "艾灸调理", "qty": "1", "amount": "¥100" }
                ],
                "total": "¥100",
                "staff": "前台"
            }),
        )
        .unwrap();
        assert!(html.contains("示例医馆"));
        assert!(html.contains("艾灸调理 × 1"));
        assert!(html.contains("¥100"));
        assert!(!html.contains("{{"));
        assert!(html.contains("window.print()"));
    }

    #[test]
    fn section_repeats_per_item_and_empty_renders_none() {
        let data = json!({ "items": [ {"name":"a"}, {"name":"b"} ] });
        let rendered = fill_sections("X<!-- BEGIN items -->[{{name}}]<!-- END items -->Y", &data);
        assert_eq!(rendered, "X[a][b]Y");
        let empty = fill_sections(
            "X<!-- BEGIN items -->[{{name}}]<!-- END items -->Y",
            &json!({ "items": [] }),
        );
        assert_eq!(empty, "XY");
    }

    #[test]
    fn prescription_page_rule_present() {
        let html = render("prescription", &json!({})).unwrap();
        assert!(html.contains("size: A5 portrait"));
        let receipt = render("receipt", &json!({})).unwrap();
        assert!(receipt.contains("size: 80mm auto"));
    }
}
