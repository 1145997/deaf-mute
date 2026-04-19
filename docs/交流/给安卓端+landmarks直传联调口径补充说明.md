# 给安卓端 + landmarks 直传联调口径补充说明

日期：2026-04-17

提出方：后端  
对接方：安卓端 uni-app / recognition runtime

## 1. 本轮已补齐内容

后端本轮已补齐以下内容：

- `bootstrap` 增加 landmarks 能力摘要
- `predict` 增加 landmarks 联调调试字段
- `predict` 支持透传：
  - `handedness`
  - `mirrored`
  - `cameraFacing`
- `recognition_record` 增加：
  - `inputMode`
  - `source`

## 2. landmarks 固定契约

当前后端固定按下面口径处理 landmarks：

- 坐标模式：`NORMALIZED_XYZ_0_TO_1`
- 原点：`TOP_LEFT`
- 点位顺序：`MEDIAPIPE_HANDS_21`
- 手模式：`SINGLE_HAND_FIRST_ONLY`

解释：

- `x / y / z` 默认按 MediaPipe Hands 原始归一化输出理解
- 如果是多手场景，当前后端只按第一只手处理
- 安卓端如果未来切原生采集，保持这个契约即可继续复用当前接口

## 3. handedness / mirrored 当前边界

当前后端已经接受这三个字段：

- `handedness`
- `mirrored`
- `cameraFacing`

但当前阶段请按下面理解：

- 这三个字段目前主要用于联调透传和记录
- 当前基础检测器尚未根据这三个字段做左右手或镜像分支判定
- 因此 `supportsHandedness = false`
- 因此 `supportsMirrored = false`

建议安卓端当前继续：

- 优先保证 landmarks 坐标契约一致
- 多手时只上传第一只手
- handedness / mirrored 先透传，后续等后端判定器升级后再正式启用分支逻辑

## 4. bootstrap 新增字段

`GET /api/recognition/bootstrap`

本轮已新增字段：

- `supportedLandmarkGestureCodes`
- `landmarkPointCount`
- `supportsHandedness`
- `supportsMirrored`
- `landmarkCoordinateMode`
- `landmarkOrigin`
- `landmarkOrder`
- `landmarkHandMode`
- `landmarkAssumptions`

示例字段含义：

- `supportedLandmarkGestureCodes`
  当前 landmarks 模式下基础检测器真正支持的动作编码
- `landmarkPointCount`
  当前固定为 `21`
- `landmarkAssumptions`
  当前单手、handedness/mirrored 未参与判定等补充说明

## 5. predict 新增调试字段

`POST /api/recognition/predict`

本轮已新增：

- `inputType`
- `stableCount`
- `requiredHits`
- `debounced`
- `detectedGestureCandidates`

字段说明：

- `inputType`
  当前输入模式，典型值为 `gestureCode` 或 `landmarks`
- `stableCount`
  当前稳定命中累计次数
- `requiredHits`
  当前触发阈值，默认是后端生效配置里的值
- `debounced`
  本次是否被防抖拦截
- `detectedGestureCandidates`
  当前帧 landmarks 下已识别出的候选基础手势

## 6. 当前联调建议

安卓端本轮可以按下面顺序联调：

1. 启动时先调用 `bootstrap`
2. 读取 `landmarkPointCount` 和 `supportedLandmarkGestureCodes`
3. landmarks 模式下继续按单手 21 点直传
4. 页面调试时重点观察：
   - `stableCount`
   - `requiredHits`
   - `debounced`
   - `detectedGestureCandidates`
5. 如果出现“同一手势命中不稳定”，先核对：
   - landmarks 坐标是否仍为归一化值
   - 点位顺序是否仍为 MediaPipe Hands 21 点顺序
   - 是否误传了多手数据

## 7. 当前已验证结果

后端已完成这轮实际验证：

- `bootstrap` 返回成功，并包含 landmarks 摘要字段
- landmarks 模式连续 3 次 `predict` 返回成功
- 第 1、2 次返回：
  - `stableCount = 1 / 2`
  - `detectedGestureCandidates = [is_v_sign]`
- 第 3 次命中：
  - `gesture = is_v_sign`
  - `matchedFlowCode = single_hello`
  - `outputType = TEXT`
- `recognition_record` 已实际写入：
  - `input_mode = landmarks`
  - `source = android-landmarks`

## 8. 后续预告

后端后续如果继续推进 landmarks 识别层，会优先考虑：

- handedness / mirrored 真正参与判定
- 多手输入协议
- landmarks 原始调试信息的更细粒度输出
