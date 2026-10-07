# brand-assets — 默认品牌资产

白标构建在**未传商家 icon 参数**时使用的仓库内置资产：

| 文件 | 来源 | 用途 |
| ---- | ---- | ---- |
| `logo.svg` | 复制自 `docs/public/logo.svg`（不改原形） | H5 页内展示兜底 |
| `logo.png` | 由 `logo.svg` 栅格化转制一次：`magick convert -background none -density 300 logo.svg -resize 512x512 logo.png` | H5 favicon 默认、页内 `<Image>` 渲染（小程序端不支持 SVG） |

口径：

- 这里只是**默认包**的占位品牌资产；商家包的 icon **只用商家提供的文件**（`--icon` 参数直传，不转制、不代餐、不在此目录留副本）。
- 微信小程序的「应用图标」不在代码包里，由商家在微信公众平台后台自行上传——商家提供的 icon 文件直接拿去上传即可。
