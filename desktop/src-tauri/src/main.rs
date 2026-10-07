#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

//! Sinomed 桌面工作站：Tauri 2 薄壳。
//! 业务全在服务端——主窗口直接加载管理端 web 地址；未配置时落 ui/ 引导页做首次配置。

mod commands;
mod config;

use tauri::{AppHandle, WebviewUrl, WebviewWindowBuilder};

use crate::config::AppConfig;

fn main() {
    // 启动参数优先：--server-url=<地址>（或空格分隔），便于现场调试指向不同环境
    let arg_server_url = config::server_url_from_args(std::env::args());
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![
            commands::get_config,
            commands::set_config
        ])
        .setup(move |app| {
            let handle = app.handle().clone();
            let mut cfg = commands::load(&handle);
            if let Some(url) = arg_server_url {
                cfg.server_url = url;
            }
            open_main_window(&handle, &cfg)?;
            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("Sinomed 工作站启动失败");
}

/// 已配置 → 直接加载远程 web 地址；未配置 → 引导页（ui/index.html）做首次配置。
fn open_main_window(app: &AppHandle, cfg: &AppConfig) -> tauri::Result<()> {
    let url = match cfg.server_url() {
        Some(url) => WebviewUrl::External(url),
        None => WebviewUrl::App("index.html".into()),
    };
    WebviewWindowBuilder::new(app, "main", url)
        .title("Sinomed 工作站")
        .inner_size(cfg.window_width as f64, cfg.window_height as f64)
        .min_inner_size(960.0, 600.0)
        .build()?;
    Ok(())
}
