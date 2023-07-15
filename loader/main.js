// Modules to control application life and create native browser window
const { app, BrowserWindow,Menu } = require('electron')
const path = require('path')
const {accessSync,appendFileSync,readFileSync,mkdirSync} = require("fs");
const {R_OK,W_OK,X_OK} = require("constants");
const defaultUrl = "http://dash.youngs.fun";
const Store = require('electron-store')
const option = {
  name: "config",
  fileExtension: "json",
  cwd:app.getPath("userData"),
  clearInvalidConfig: true,
}
console.log("userData:",app.getPath("userData"));
const store = new Store(option);
console.log("url:",store.get("url",defaultUrl));
function createWindow () {
  // Create the browser window.
  const mainWindow = new BrowserWindow({
    width: 1200,
    height: 900,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js')
    }
  })
  Menu.setApplicationMenu(null);
  // and load the index.html of the app.
  //mainWindow.loadFile('index.html')
  mainWindow.loadURL(store.get("url",defaultUrl))

  // Open the DevTools.
  // mainWindow.webContents.openDevTools()
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  createWindow()
  //window.webContents.openDevTools()
  app.on('activate', function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') app.quit()
})

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.
