# 安卓端 landmarks 解耦改造记录

日期：2026-04-17

## 本轮目标

- 把识别功能代码从页面 UI 中拆开
- 保留当前联调能力，但让后续 UI 重做时不需要再改识别链路
- 在 `gestureCode` 已联通的基础上，往 `landmarks` 直传模式推进

## 本轮完成

### 1. 识别运行时解耦

新增目录：

- `mobile/uniapp-deafmute/src/modules/recognition`

拆分结果：

- `constants.js`
  - 输入模式、存储 key、控制动作常量
- `storage.js`
  - 本地配置读写
- `input.js`
  - bridge 消息归一化
  - `gestureCode / landmarks` 输入归一化
  - backend `predict` 请求体拼装
- `state.js`
  - 纯状态初始化
  - 输出缓冲与控制动作处理
  - 识别结果落状态
- `runtime.js`
  - `bootstrap / session/start / predict / session/reset / session/close / recognition-record/my`
  - bridge 消息转运行时输入
  - 预测串行队列

调整后：

- `src/stores/recognition.js` 只保留状态暴露和薄动作分发
- 页面不再直接承载识别拼装逻辑

### 2. bridge 页改成运行时壳子

改造文件：

- `mobile/uniapp-deafmute/src/pages/bridge/index.vue`

变化：

- bridge 页只做配置录入、web-view 承载和运行时状态展示
- 新增输入模式切换：
  - `gestureCode bridge`
  - `landmarks direct`
- 保存配置时同步持久化：
  - `blind origin`
  - `Recognition API base`
  - `token`
  - `inputMode`

### 3. bridge 协议补 landmarks 通道

改造文件：

- `mobile/uniapp-deafmute/src/config/bridge.js`
- `blind/src/static/index.html`

新增协议：

- `gesture-landmarks`

当前行为：

- `gestureCode` 模式：
  - `blind` 继续本地 `/predict`
  - 再把结果通过 `gesture-predict` 回传 uni-app
- `landmarks` 模式：
  - `blind` 不再本地 `/predict`
  - 直接把 21 点 landmarks 通过 `gesture-landmarks` 回传 uni-app
  - uni-app 再走 Spring Boot `/api/recognition/predict`

补充：

- `blind` 现在会读取 query 参数 `bridgeInputMode`
- 增加了简单帧率节流，避免 landmarks 直传过密
- 会一起回传 `frameNo / capturedAt / source / handedness`

注意：

- 当前后端 DTO 还没有正式使用 `handedness`
- 这部分先用于安卓端侧保留信息，后续等后端约定

### 4. 接入后端新增 landmarks 口径

本轮已同步接入后端新增字段：

- `predict`
  - `handedness`
  - `mirrored`
  - `cameraFacing`
- `bootstrap`
  - `supportedLandmarkGestureCodes`
  - `landmarkPointCount`
  - `supportsHandedness`
  - `supportsMirrored`
  - `landmarkCoordinateMode`
  - `landmarkOrigin`
  - `landmarkOrder`
  - `landmarkHandMode`
  - `landmarkAssumptions`
- `predict` 调试字段
  - `inputType`
  - `stableCount`
  - `requiredHits`
  - `debounced`
  - `detectedGestureCandidates`

安卓端当前处理：

- bridge landmarks 默认透传：
  - `source = android-landmarks`
  - `cameraFacing = front`
  - `mirrored = false`
- store 已接住 bootstrap 契约字段
- store 已接住 predict 调试字段
- bridge 页现在可以直接看：
  - landmarks 固定契约
  - 支持的基础手势列表
  - 当前稳定帧累计
  - 是否 debounced
  - 当前候选手势
  - 最近一次输入的 handedness / mirrored / cameraFacing

## 本轮联调判断

当前状态：

- 功能层已经可以脱离当前 UI 继续迭代
- landmarks 直传链路代码已接通
- 后续即使首页、bridge 页、个人页重做，也不需要重写识别核心逻辑

## 本轮验证

### 1. uni-app 构建

- `mobile/uniapp-deafmute`
- 已执行：`npm run build`
- 结果：通过

### 2. landmarks 接口冒烟

2026-04-17 已对真后端 `http://127.0.0.1:8080/api` 做 landmarks 模式冒烟：

- `session/start` 成功
- 连续 3 次 `predict(landmarks)` 成功
- 第 3 次命中：
  - `gesture = is_v_sign`
  - `code = single_hello`
  - `matchedFlowCode = single_hello`
  - `matchedNodeCode = start`
- `session/reset` 成功
- `session/close` 成功

说明：

- 当前后端默认稳定帧策略仍然生效，所以第 1、2 次返回空结果，第 3 次才正式命中

### 3. 后端新增调试字段验证

本轮再做了一次带调试字段的 landmarks 冒烟，结果已确认：

- `bootstrap` 返回：
  - `supportedLandmarkGestureCodes = [is_thumbs_up, is_v_sign, is_four_sign, is_fist, is_ok_sign]`
  - `landmarkPointCount = 21`
  - `supportsHandedness = false`
  - `supportsMirrored = false`
  - `landmarkCoordinateMode = NORMALIZED_XYZ_0_TO_1`
  - `landmarkOrigin = TOP_LEFT`
  - `landmarkOrder = MEDIAPIPE_HANDS_21`
  - `landmarkHandMode = SINGLE_HAND_FIRST_ONLY`
- 连续 3 次 `predict(landmarks)` 返回：
  - 第 1 次：`inputType = landmarks`，`stableCount = 1`，`requiredHits = 3`，`debounced = false`
  - 第 2 次：`stableCount = 2`
  - 第 3 次：`stableCount = 3`，并命中 `single_hello`
- 3 次里 `detectedGestureCandidates` 都包含：
  - `is_v_sign`

## 给后端的新需求单

已新增交流文档：

- `docs/交流/给后端+landmarks直传联调补充需求.md`

主要诉求：

- landmarks 坐标契约
- 左右手 / 镜像口径
- bootstrap 能力摘要
- predict 调试字段
- recognition_record 区分输入来源

## 下一步

安卓端下一步按这个顺序继续：

1. 用真后端跑 landmarks 模式冒烟
2. 确认真机 / 模拟器 web-view 下 landmarks 回流是否稳定
3. 再评估从 `blind web-view` 过渡到原生摄像头采集
