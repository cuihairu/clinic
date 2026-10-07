#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

//! Sinomed 桌面工作站：Tauri 2 薄壳。
//! 业务全在服务端——主窗口直接加载管理端 web 地址；未配置时落 ui/ 引导页做首次配置。
//! 启动后后台查更新（desktop.md D5）：命中新版弹提示，确认后静默下载安装、重启生效。

mod commands;
mod config;
mod print;

use tauri::{AppHandle, WebviewUrl, WebviewWindowBuilder};

use crate::config::AppConfig;

/// updater 清单地址按配置的更新通道取（config.json update_channel）
const NIGHTLY_ENDPOINT: &str =
    "https://github.com/cuihairu/sinomed/releases/download/nightly/latest.json";
const STABLE_ENDPOINT: &str =
    "https://github.com/cuihairu/sinomed/releases/latest/download/latest.json";

fn endpoint_for_channel(channel: &str) -> &'static str {
    if channel == "stable" {
        STABLE_ENDPOINT
    } else {
        NIGHTLY_ENDPOINT
    }
}

fn main() {
    // 启动参数优先：--server-url=<地址>（或空格分隔），便于现场调试指向不同环境
    let arg_server_url = config::server_url_from_args(std::env::args());
    tauri::Builder::default()
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_updater::Builder::new().build())
        .invoke_handler(tauri::generate_handler![
            commands::get_config,
            commands::set_config,
            print::print_html
        ])
        .setup(move |app| {
            let handle = app.handle().clone();
            let mut cfg = commands::load(&handle);
            if let Some(url) = arg_server_url {
                cfg.server_url = url;
            }
            open_main_window(&handle, &cfg)?;
            tauri::async_runtime::spawn(check_for_updates(handle));
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

/// 后台查更新：失败只记日志不打扰使用（无 Release / 离线均属常态）；命中新版弹窗确认后
/// 下载安装并重启（NSIS 安装器接管，重启即新版）。
async fn check_for_updates(app: AppHandle) {
    use tauri_plugin_updater::UpdaterExt;

    // 等窗口先起来，别跟首屏抢带宽
    tauri::async_runtime::spawn_blocking(|| std::thread::sleep(std::time::Duration::from_secs(5)))
        .await
        .ok();

    let channel = commands::load(&app).update_channel;
    let endpoint = endpoint_for_channel(&channel);
    let endpoint_url: tauri::Url = endpoint.parse().expect("更新端点 URL");
    let updater = match app
        .updater_builder()
        .endpoints(vec![endpoint_url])
        .and_then(|builder| builder.build())
    {
        Ok(updater) => updater,
        Err(e) => {
            eprintln!("[updater] 构建更新器失败（通道 {channel}）：{e}");
            return;
        }
    };

    match updater.check().await {
        Ok(Some(update)) => {
            eprintln!(
                "[updater] 发现新版本 {}（当前 {}）",
                update.version,
                app.package_info().version
            );
            use tauri_plugin_dialog::{DialogExt, MessageDialogButtons};
            let confirmed = app
                .dialog()
                .message(format!(
                    "发现新版本 {}（当前 {}），立即更新？\n更新将下载并自动重启工作站。",
                    update.version,
                    app.package_info().version
                ))
                .title("Sinomed 工作站")
                .buttons(MessageDialogButtons::OkCancelCustom(
                    "立即更新".into(),
                    "下次再说".into(),
                ))
                .blocking_show();
            if !confirmed {
                eprintln!("[updater] 用户选择暂不更新");
                return;
            }
            let mut downloaded: usize = 0;
            match update
                .download_and_install(
                    |chunk, total| {
                        downloaded += chunk;
                        if let Some(total) = total {
                            eprintln!("[updater] 下载 {downloaded}/{total} 字节");
                        }
                    },
                    || eprintln!("[updater] 下载完成，开始安装"),
                )
                .await
            {
                Ok(()) => {
                    eprintln!("[updater] 安装完成，重启生效");
                    app.restart();
                }
                Err(e) => eprintln!("[updater] 下载/安装失败：{e}"),
            }
        }
        Ok(None) => eprintln!("[updater] 已是最新版本（通道 {channel}）"),
        Err(e) => eprintln!("[updater] 检查更新失败（通道 {channel}）：{e}"),
    }
}
