# 给后端 + landmarks 直传联调补充需求

日期：2026-04-17

提出方：安卓端 uni-app  
对接方：后端识别消费层

## 背景

安卓端已经完成两件事：

- 识别运行时从页面 UI 里拆出来，后续可以重做界面而不动识别链路。
- `blind -> uni-app` bridge 已新增 `gesture-landmarks` 通道，可以开始按 `landmarks` 直传模式联调。

当前 landmarks 直传已经不是阻塞项，现有接口足够先跑 PoC。  
下面这些点是“为了让 landmarks 直传在真机上稳定”，建议后端后续补充或明确。

## 当前已确认可用

- `POST /api/recognition/predict` 已支持传 `landmarks`
- 当前格式可按单手 21 点发送
- 当前基础识别器已覆盖：
  - `is_thumbs_up`
  - `is_v_sign`
  - `is_four_sign`
  - `is_fist`
  - `is_ok_sign`

## 补充诉求

### 1. 明确 landmarks 坐标契约

希望后端补一份固定口径，至少明确：

- 坐标是否要求归一化到 `0 ~ 1`
- 原点位置是否固定为左上角
- 点位顺序是否完全按 MediaPipe Hands 21 点顺序
- 是否默认按单手处理，多手时安卓端是否只上传第一只手

说明：

- 安卓端现在准备往原生摄像头/原生 landmarks 走，后面不一定永远通过 `blind` H5。
- 如果坐标契约不先定下来，后面换采集方式时很容易出现“同一手势，识别结果变了”的问题。

### 2. 明确左右手 / 镜像约定

这是本轮最希望后端配合补清楚的一点。

基于当前代码推断：

- `BasicGestureDetector` 里有拇指方向相关判断
- 这类规则对左右手、前置镜像、不同 SDK 的 handedness 定义比较敏感

希望后端补充其一：

- 方案 A：在 `predict` landmarks 模式下支持附加字段
  - `handedness`
  - `mirrored`
  - `cameraFacing`
- 方案 B：先不收字段，但明确后端默认假设
  - 只支持右手还是左右手都支持
  - 输入 landmarks 是否要求已经去镜像
  - 前置摄像头是否需要安卓端先做坐标翻转

这部分不是当前 PoC 的硬阻塞，但是真机接入后非常容易踩坑，建议尽快统一口径。

### 3. bootstrap 增加 landmarks 能力摘要

建议 `GET /api/recognition/bootstrap` 后续补充可选字段：

- `supportedLandmarkGestureCodes`
- `landmarkPointCount`
- `supportsHandedness`
- `supportsMirrored`

安卓端好处：

- 启动时就能知道当前配置下 landmarks 直传到底支持哪些基础动作
- 页面不用硬编码能力范围
- 后面如果基础检测器扩展，也不用同步改死前端常量

### 4. predict 返回更多调试信息

建议 `POST /api/recognition/predict` 在 landmarks 模式下，后续可选补充这些调试字段：

- `inputType`
- `stableCount`
- `requiredHits`
- `debounced`
- `detectedGestureCandidates`

安卓端当前确实能跑，但 landmarks 调参阶段很需要知道：

- 这次是不是被 `debounceMs` 吃掉了
- 当前稳定帧累计到第几次
- 这帧完全没识别，还是识别到了但未达到触发阈值

如果这些字段暂时不方便进正式返回，也可以考虑先放调试开关或开发环境输出。

### 5. recognition_record 中区分输入来源

建议后端后续在记录层可区分：

- `gestureCode` 模式命中
- `landmarks` 模式命中

最好能保留来源信息，例如：

- `inputMode`
- `source`

这样安卓端后面切到 landmarks 直传以后，方便对比两种接法的命中率和误识别情况。

## 安卓端当前默认约定

在后端补充字段之前，安卓端这边先按下面的保守约定推进：

- landmarks 按单手 21 点上送
- 坐标沿用 MediaPipe 输出的 `x/y/z`
- 当前先不向后端正式传 `handedness / mirrored`
- 当前仍按后端默认 `requiredHits = 3`、`debounceMs`、`maxIntervalMs` 进行联调

## 优先级建议

建议优先级：

1. 先明确 landmarks 坐标契约
2. 再明确 handedness / 镜像口径
3. 然后补 bootstrap 能力摘要
4. 最后补 predict 调试字段和 record 来源字段

## 结论

安卓端会继续先按现有接口把 landmarks 直传链路跑通。  
上面这些补充项不是为了阻塞当前开发，而是为了避免真机摄像头接入后，因为左右手和镜像口径不一致而返工。
