# 安卓端原生 Kotlin CameraX Demo 开发记录

日期：2026-04-18

## 本轮目标

在 `test/CameraX` 下新建一个原生 Android Studio 工程，优先打通下面这条链路：

- `bootstrap`
- `session/start`
- CameraX 持续取帧
- MediaPipe Hand Landmarker 单手 21 点
- `predict(landmarks)`
- 本地句子缓冲
- 倒计时播报
- `session/close`

## 协议核对结果

本轮以 `docs/02-协议接口/api_full_manual_2026-04-18.md` 和后端 `RecognitionController / DTO / VO` 为准，确认如下口径：

- 接口根路径：`/api`
- `GET /recognition/bootstrap`
- `POST /recognition/session/start`
  - `clientType = android`
  - `sceneCode = bridge`
  - `engineType = mediapipe`
- `POST /recognition/predict`
  - 主模式：`landmarks`
  - 单手 21 点
  - 点序：`MEDIAPIPE_HANDS_21`
  - 坐标：`x / y / z`
  - 透传：
    - `sessionId`
    - `frameNo`
    - `source`
    - `handedness`
    - `mirrored`
    - `cameraFacing`
- `POST /recognition/session/close`
  - `reason = page-close`

## 本轮选型

基础模板：

- 原生 Android Studio 最小单 Activity Kotlin 工程

参考实现：

- 官方 MediaPipe Hand Landmarker Android sample
- 官方 CameraX 用法

不直接搬完整第三方 demo 的原因：

- 当前目标目录原本是空的
- 这轮重点是以后端协议和可维护结构为核心，不是通用视觉示例
- 直接用最小模板更容易收口到单页面可跑 demo

## 本轮完成

### 1. 工程骨架

已在 `test/CameraX` 下新增原生 Android 工程文件：

- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`
- `gradlew / gradlew.bat / gradle-wrapper.jar`
- `app/build.gradle.kts`

### 2. 依赖与资源

已接入：

- Retrofit / OkHttp
- Kotlin Coroutines
- ViewModel + StateFlow
- CameraX
- MediaPipe `tasks-vision`
- Android TextToSpeech

已补资源：

- 单页面布局 `activity_main.xml`
- `hand_landmarker.task`

其中模型文件来源采用官方 sample 中的下载地址：

- `https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task`

### 3. 代码结构

已按目标拆分：

- `camera`
- `mediapipe`
- `network/api`
- `repository`
- `ui`
- `tts`
- `model/dto`
- `domain`

### 4. 主链路代码

本轮已落的关键类：

- `MainActivity`
  - 页面入口
  - 相机权限
  - CameraX 绑定
  - UI 渲染
- `RecognitionViewModel`
  - `bootstrap -> session/start -> predict -> session/close`
  - 节流
  - 本地状态
  - 倒计时与 TTS 调度
- `HandLandmarkerManager`
  - MediaPipe LIVE_STREAM 模式
  - CameraX 帧转 MPImage
- `HandLandmarkResultMapper`
  - 第一只手 21 点映射
- `SentenceBufferManager`
  - TEXT / PHRASE / CONTROL 处理
  - DELETE / CLEAR / RESET / CONFIRM 可扩展控制动作
- `CountdownManager`
  - 无新输出时触发播报

## 当前验证

### 1. 目录与资源

已确认：

- `gradle wrapper` 文件已落地
- `hand_landmarker.task` 已落地到 `app/src/main/assets`

### 2. 编译自检修正

本轮额外做了一次真实编译前的源码自检，修正了几类容易卡编译或资源链接的问题：

- `activity_main.xml`
  - 把错误的 `@style/Widget.Material3.OutlinedButton`
  - 改成 `@style/Widget.Material3.Button.OutlinedButton`
- `RecognitionDtos.kt`
  - 将 Retrofit/Gson 可能缺省的列表字段改成可空
  - 避免响应缺字段时因为 Kotlin 非空类型导致运行期异常
- `RecognitionViewModel.kt`
  - 保存非法 baseUrl 时增加兜底错误提示
  - `bootstrap / predict` 对可空列表统一用 `orEmpty()`
  - 初始化时补了 `starting / error` 状态切换
  - 避免已有 active session 时重复初始化

### 3. 本机构建验证

本轮已找到可用 JDK：

- `C:\Program Files\Java\jdk-21`

并完成：

- 本地下载 `gradle-8.7-bin.zip`
- 解压本地 Gradle 发行包
- 使用 JDK 21 执行：
  - `gradle.bat assembleDebug --stacktrace`

结果：

- `assembleDebug` 通过
- 产物已生成：
  - `test/CameraX/app/build/outputs/apk/debug/app-debug.apk`

### 4. 当前命令行环境补充

系统默认 `java` 仍然是：

- `Java 1.8.0_202`

但本轮编译已经绕过默认 Java，显式切到了 `jdk-21`，因此当前工程至少已完成一次真实 Debug 构建验证。

## 2026-04-19 TTS 诊断增强

### 1. 问题背景

联调时发现前端页面里的 TTS 状态长期停留在：

- `initializing`

原实现只有一个简单布尔值：

- `ttsReady`

这会导致我们无法判断实际是哪一类问题：

- 系统根本没发现 TTS 服务
- `TextToSpeech` 构造成功，但 `onInit` 没有回调
- `onInit` 回调失败
- 中文语言包缺失或不支持
- 播报文本被排队但没有真正执行

### 2. 本轮改造

本轮将 `SpeechPlayer` 改造成可诊断版本，新增：

- `TtsDiagnostics` 状态流
- `phase`
  - `creating`
  - `waiting_on_init`
  - `ready`
  - `error`
  - `timeout`
  - `shutdown`
- `initStatusCode`
- `languageStatusCode`
- `defaultEngine`
- `installedEngines`
- `discoveredServices`
- `pendingText`
- `lastSpokenText`
- `lastError`
- `logLines`

同时新增：

- 初始化超时判断
  - 8 秒未收到 `onInit`，标记为 `timeout`
- PackageManager 级别的 TTS Service 扫描
- Logcat 日志
  - Tag：`SpeechPlayer`

### 3. 页面可见性

本轮已把 TTS 诊断信息同步到页面调试面板，当前页面会直接显示：

- TTS phase
- 默认引擎
- 初始化状态码
- 语言设置状态码
- 已发现系统服务
- 已安装引擎
- 最近一次排队文本
- 最近一次已播报文本
- 最近错误
- 最近 12 条 TTS 日志

这样即使不看 Logcat，也能直接在页面判断 TTS 卡在哪一步。

### 4. 后端地址测试

用户提供了测试地址：

- `http://8.133.16.236/api/`

本轮命令行验证结果：

- `GET /recognition/bootstrap`
  - 返回 `200`
  - 说明公网后端地址可达

`session/start` 在 PowerShell 下用 `Invoke-RestMethod` 测试时出现超时，但这轮重点是 TTS 诊断增强，未继续追 shell 侧 POST 超时原因。

### 5. 构建结果

修改 TTS 诊断层后再次执行：

- `assembleDebug`

结果：

- 通过

说明本轮新增的诊断逻辑没有破坏现有工程编译。

## 2026-04-19 TTS 用户可读状态页

### 1. 本轮目的

在已有技术诊断字段基础上，再补一层用户可读说明，解决“页面一直显示 initializing，但不知道该怎么办”的问题。

### 2. 本轮新增能力

已新增 TTS 用户态状态映射：

- `ready`
- `no_engine`
- `init_timeout`
- `missing_language_data`
- `language_not_supported`
- `error`

并新增页面卡片：

- `语音播报状态`

当前会直接展示：

- 当前状态
- 默认引擎名称
- 问题原因
- 建议操作

同时新增两个按钮：

- `打开系统 TTS 设置`
- `重试检测`

### 3. 设置跳转策略

优先使用：

- `Intent("com.android.settings.TTS_SETTINGS")`

如果失败，降级到：

- `Settings.ACTION_SETTINGS`

当用户从设置页返回后：

- 页面会自动重新检测 TTS 状态

### 4. 播报请求反馈调整

本轮调整后，当 TTS 当前不可用时：

- 页面不会再显示“已播报”
- 会明确提示：
  - `已触发播报请求，但系统 TTS 不可用`

### 5. 构建验证

本轮修改后再次执行：

- `assembleDebug`

结果：

- 通过

## 下一步建议

建议按下面顺序继续：

1. 用 Android Studio 打开 `test/CameraX`
2. 确认 JDK 17 与 SDK 35
3. 修改首页里的 API Base
4. 先用后摄跑通 `bootstrap -> start -> predict -> close`
5. 再根据真机/模拟器情况微调节流间隔和倒计时
