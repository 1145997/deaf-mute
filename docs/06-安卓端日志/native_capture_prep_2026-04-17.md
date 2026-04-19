# 安卓端原生摄像头迁移准备

日期：2026-04-17

## 本轮目的

在 `web-view camera -> gesture-landmarks -> Spring Boot` 已经可联调的前提下，先把原生摄像头采集迁移需要的落脚点准备出来，避免后续改造时再次把 UI、bridge 和识别链路耦在一起。

## 本轮准备内容

### 1. 增加采集提供者抽象

新增目录：

- `mobile/uniapp-deafmute/src/modules/capture`

已新增：

- `constants.js`
  - `webviewBridge`
  - `uniCamera`
  - `nativePlugin`
  - 同时补了迁移阶段常量
- `preflight.js`
  - 运行环境采样
  - camera 授权状态读取
  - localhost / emulator / LAN 地址预设

当前意义：

- 现在默认采集提供者仍然是 `webviewBridge`
- 但后续切到 `uniCamera` 或原生插件时，不需要重新设计整条识别状态链

### 2. 首页与 bridge 页改成联调壳子

已调整：

- 首页重点展示：
  - 当前识别回流
  - predict 调试信息
  - landmarks 固定契约
  - capture provider / migration stage
- bridge 页重点展示：
  - 设备预检
  - camera 权限状态
  - 地址预设切换
  - web-view 承载

已落代码：

- `src/pages/home/index.vue`
- `src/pages/bridge/index.vue`
- `src/modules/capture/preflight.js`

这意味着：

- 真机联调时首页已经足够承担“回流面板”
- bridge 页已经更像“运行时控制台”

### 3. Android 联调兜底项

已补：

- `manifest.json`
  - `usesCleartextTraffic = true`

作用：

- 方便继续使用 `http://127.0.0.1`、`10.0.2.2`、局域网 IP 做开发联调
- 降低 Android WebView 在明文调试链路上的拦截概率

## 本轮验证

### 1. 构建验证

- `mobile/uniapp-deafmute`
- 已执行：`npm run build`
- 结果：通过

### 2. 真后端联调烟测

针对 `http://127.0.0.1:8080/api` 再次做了 landmarks 烟测，确认本轮页面和预检改造没有破坏链路：

- `bootstrap` 返回：
  - `landmarkPointCount = 21`
  - `landmarkCoordinateMode = NORMALIZED_XYZ_0_TO_1`
  - `landmarkOrigin = TOP_LEFT`
  - `landmarkOrder = MEDIAPIPE_HANDS_21`
- 单次 `predict(landmarks)` 返回：
  - `inputType = landmarks`
  - `stableCount = 1`
  - `requiredHits = 3`
  - `detectedGestureCandidates = is_v_sign`

说明：

- 本轮新增的设备预检、地址预设、capture provider 抽象和首页调试面板没有影响现有 landmarks 联调链路

## 当前迁移建议

建议按下面顺序推进原生摄像头采集：

1. 先保留当前 `webviewBridge + landmarks` 作为可回退链路
2. 新增 `uniCamera` 采集 PoC
3. 验证原生帧 -> landmarks 产出方式
4. 复用现有 recognition runtime 直接提交 `/api/recognition/predict`
5. 稳定后再考虑是否需要更底层原生插件

## 当前不急着做的部分

以下内容这轮先不做：

- 原生相机预览页面正式 UI
- 原生 landmarks 模型接入
- 多手识别
- handedness / mirrored 真正参与前端侧坐标修正

原因：

- 当前优先级还是把 landmarks 直传联调收口
- 先保留一条稳定可用的 web-view 识别链路更稳

## 下一步

安卓端下一步继续：

1. 用真机/模拟器跑通 `web-view camera -> landmarks -> Spring Boot -> 首页回流`
2. 记录设备侧实际阻塞点
3. 再单开 `uniCamera` 采集 PoC 页面或模块

## 首页相机位补充

本轮已额外完成：

- H5 / 浏览器场景下，首页圆形相机位直接内嵌 `blind` 精简预览页
- 精简预览页继续通过 `postMessage` 把 `gesture-landmarks` 回流给首页 recognition runtime
- App 端暂时仍保留 bridge 页承载摄像头联调

对应代码：

- `mobile/uniapp-deafmute/src/pages/home/index.vue`
- `mobile/uniapp-deafmute/src/config/bridge.js`
- `blind/src/static/index.html`

这样当前浏览器预览和 App 联调各自有稳定承载方式：

- 浏览器：首页相机位直接看画面
- App：桥接页承载 web-view，后续再切原生摄像头提供者
