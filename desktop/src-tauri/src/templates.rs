//! 打印模板缓存（desktop.md D7）：服务端模板接口 + 本地缓存覆盖。
//! 服务端按 version（内容摘要）下发全部模板；这里拉取后写 app_cache_dir/templates/，
//! 打印时缓存优先、内置兜底——模板更新只需改服务端，不发壳版本。

use std::fs;
use std::path::{Path, PathBuf};

use serde::Deserialize;
use tauri::{AppHandle, Manager};

/// 可缓存模板白名单：与内置模板、服务端下发白名单一一对应
const WHITELIST: &[&str] = &["prescription", "receipt"];

#[derive(Deserialize)]
struct TemplatesPayload {
    #[serde(default)]
    version: String,
    #[serde(default)]
    templates: Vec<TemplateItem>,
}

#[derive(Deserialize)]
struct TemplateItem {
    name: String,
    content: String,
}

/// 模板缓存目录：app_cache_dir/templates
fn cache_dir(app: &AppHandle) -> Option<PathBuf> {
    app.path().app_cache_dir().ok().map(|dir| dir.join("templates"))
}

/// 解析模板内容：本地缓存优先（服务端下发的新版），缺失/为空回落内置；未知模板返回 None
pub fn resolve(cache_dir: Option<&Path>, name: &str) -> Option<String> {
    if let Some(dir) = cache_dir {
        if let Ok(text) = fs::read_to_string(dir.join(format!("{name}.html"))) {
            if !text.trim().is_empty() {
                return Some(text);
            }
        }
    }
    crate::print::builtin_template(name).map(str::to_string)
}

/// 从服务端拉取模板并按版本增量覆盖缓存；任何失败只记日志（离线 / 旧版服务端是常态，不打扰打印）
pub async fn refresh(app: AppHandle) {
    if let Err(e) = refresh_inner(&app).await {
        eprintln!("[templates] {e}");
    }
}

async fn refresh_inner(app: &AppHandle) -> Result<(), String> {
    let base = crate::commands::load(app)
        .server_url()
        .ok_or("未配置服务端地址，跳过模板刷新")?;
    let url = base
        .join("/api/v1/print/templates")
        .map_err(|e| format!("模板接口地址非法：{e}"))?;
    let payload: TemplatesPayload = reqwest::Client::builder()
        .timeout(std::time::Duration::from_secs(3))
        .build()
        .map_err(|e| format!("HTTP 客户端构建失败：{e}"))?
        .get(url)
        .send()
        .await
        .map_err(|e| format!("拉取模板失败：{e}"))?
        .error_for_status()
        .map_err(|e| format!("模板接口响应异常：{e}"))?
        .json()
        .await
        .map_err(|e| format!("模板响应解析失败：{e}"))?;

    let items = select_items(&payload);
    if payload.version.is_empty() || items.is_empty() {
        return Err("模板响应为空，跳过缓存".into());
    }
    let dir = cache_dir(app).ok_or("缓存目录不可用")?;
    if cached_version(&dir).as_deref() == Some(payload.version.as_str()) {
        return Ok(());
    }
    fs::create_dir_all(&dir).map_err(|e| format!("创建模板缓存目录失败：{e}"))?;
    for (name, content) in &items {
        fs::write(dir.join(format!("{name}.html")), content)
            .map_err(|e| format!("写模板缓存失败：{e}"))?;
    }
    let meta = serde_json::json!({ "version": payload.version }).to_string();
    fs::write(dir.join("meta.json"), meta).map_err(|e| format!("写模板版本失败：{e}"))?;
    eprintln!("[templates] 模板缓存已更新至 {}", payload.version);
    Ok(())
}

/// 白名单 + 非空过滤：只缓存认识的模板，杜绝服务端误发内容污染
fn select_items(payload: &TemplatesPayload) -> Vec<(String, String)> {
    payload
        .templates
        .iter()
        .filter(|item| WHITELIST.contains(&item.name.as_str()) && !item.content.trim().is_empty())
        .map(|item| (item.name.clone(), item.content.clone()))
        .collect()
}

/// 已缓存版本号（meta.json），未缓存返回 None
fn cached_version(dir: &Path) -> Option<String> {
    fs::read_to_string(dir.join("meta.json"))
        .ok()
        .and_then(|text| serde_json::from_str::<serde_json::Value>(&text).ok())
        .and_then(|meta| meta["version"].as_str().map(str::to_string))
}

#[cfg(test)]
mod tests {
    use super::*;

    struct TempDir(PathBuf);
    impl TempDir {
        fn new(tag: &str) -> Self {
            let dir = std::env::temp_dir().join(format!(
                "sinomed-d7-{tag}-{}",
                std::process::id()
            ));
            let _ = fs::remove_dir_all(&dir);
            fs::create_dir_all(&dir).unwrap();
            TempDir(dir)
        }
    }
    impl Drop for TempDir {
        fn drop(&mut self) {
            let _ = fs::remove_dir_all(&self.0);
        }
    }

    #[test]
    fn resolve_prefers_cache_and_falls_back_to_builtin() {
        let tmp = TempDir::new("resolve");
        // 无缓存 → 内置
        let builtin = resolve(Some(&tmp.0), "receipt").unwrap();
        assert!(builtin.contains("size: 80mm auto"));
        // 有缓存 → 缓存优先
        fs::write(tmp.0.join("receipt.html"), "<html>服务端新版小票</html>").unwrap();
        assert_eq!(
            resolve(Some(&tmp.0), "receipt").unwrap(),
            "<html>服务端新版小票</html>"
        );
        // 缓存文件为空 → 回落内置
        fs::write(tmp.0.join("receipt.html"), "  ").unwrap();
        assert!(resolve(Some(&tmp.0), "receipt").unwrap().contains("80mm"));
        // 未知模板 → None（render 层报错）
        assert!(resolve(Some(&tmp.0), "nope").is_none());
        // 缓存目录缺失 → 内置兜底
        assert!(
            resolve(Some(&tmp.0.join("missing")), "prescription")
                .unwrap()
                .contains("A5 portrait")
        );
    }

    #[test]
    fn select_items_filters_whitelist_and_empty() {
        let payload: TemplatesPayload = serde_json::from_str(
            r#"{
                "version": "abc123",
                "templates": [
                    { "name": "receipt", "content": "<html>小票</html>" },
                    { "name": "evil", "content": "<html>不该缓存</html>" },
                    { "name": "prescription", "content": "   " }
                ]
            }"#,
        )
        .unwrap();
        let items = select_items(&payload);
        assert_eq!(items, vec![("receipt".into(), "<html>小票</html>".into())]);
    }

    #[test]
    fn cached_version_reads_meta_and_tolerates_absence() {
        let tmp = TempDir::new("meta");
        assert_eq!(cached_version(&tmp.0), None);
        fs::write(tmp.0.join("meta.json"), r#"{"version":"v7"}"#).unwrap();
        assert_eq!(cached_version(&tmp.0).as_deref(), Some("v7"));
    }
}
