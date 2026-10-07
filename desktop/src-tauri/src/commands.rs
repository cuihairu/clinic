use std::fs;

use tauri::{AppHandle, Manager};

use crate::config::AppConfig;

/// config.json 落在应用配置目录（Windows %APPDATA%/<identifier>，Linux ~/.config/<identifier>）
fn config_path(app: &AppHandle) -> std::path::PathBuf {
    app.path()
        .app_config_dir()
        .expect("app_config_dir 不可用")
        .join("config.json")
}

/// 读配置；文件缺失或损坏时返回默认值（引导页可见可改）
pub fn load(app: &AppHandle) -> AppConfig {
    fs::read_to_string(config_path(app))
        .ok()
        .and_then(|text| serde_json::from_str(&text).ok())
        .unwrap_or_default()
}

/// 全量保存（引导页先 get_config 再改字段后整体回写）；返回保存后的配置
pub fn save(app: &AppHandle, config: AppConfig) -> Result<AppConfig, String> {
    config.validate()?;
    let path = config_path(app);
    if let Some(dir) = path.parent() {
        fs::create_dir_all(dir).map_err(|e| format!("创建配置目录失败：{e}"))?;
    }
    let text = serde_json::to_string_pretty(&config).map_err(|e| format!("序列化失败：{e}"))?;
    fs::write(&path, text).map_err(|e| format!("写入配置失败：{e}"))?;
    Ok(config)
}

#[tauri::command]
pub fn get_config(app: AppHandle) -> AppConfig {
    load(&app)
}

#[tauri::command]
pub fn set_config(app: AppHandle, config: AppConfig) -> Result<AppConfig, String> {
    save(&app, config)
}

/// 把系统自启项收敛到配置值：不一致才动（enable/disable），失败仅记日志不打扰启动。
/// 自启注册随安装路径走，装机迁移 / 注册表被清后下次启动自动恢复（desktop.md D6）。
pub fn apply_autostart(app: &AppHandle, enabled: bool) {
    use tauri_plugin_autostart::ManagerExt;
    let manager = app.autolaunch();
    let current = manager.is_enabled().unwrap_or(false);
    if current == enabled {
        return;
    }
    let result = if enabled {
        manager.enable()
    } else {
        manager.disable()
    };
    match result {
        Ok(()) => eprintln!("[autostart] 已{}开机自启", if enabled { "开启" } else { "关闭" }),
        Err(e) => eprintln!("[autostart] 设置开机自启失败：{e}"),
    }
}

/// 引导页开关开机自启：写配置（事实来源）并立即收敛系统自启项
#[tauri::command]
pub fn set_autostart(app: AppHandle, enabled: bool) -> Result<bool, String> {
    let mut cfg = load(&app);
    cfg.autostart = enabled;
    save(&app, cfg)?;
    apply_autostart(&app, enabled);
    Ok(enabled)
}
