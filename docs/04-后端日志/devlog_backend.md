# 后端开发日志

更新时间：2026-04-17

本文档用于记录当前项目后端主线的实际落地情况，重点保留：

- 已完成的模块改造
- 数据库与运行验证
- 与管理端、安卓端联调相关的交付状态
- 各阶段后端能力的边界与当前结论

---

## 一期收口

### 1. 平台语义迁移

一期已完成从原“校园失物招领系统”到“智能手语翻译工具内容平台”的基础迁移：

- 新增 `post` 模块，承接原 `lostfound` 的发帖、列表、详情、我的发布、审核链路
- `comment` 语义从 `infoId` 切换为 `postId`
- `category` 升级为通用分类，支持 `POST / LEARNING`
- `statistics` 统计口径改成社区内容平台一期口径
- 文件上传目录前缀从 `lostfound/` 调整为 `deafmute/`

### 2. 旧业务入口处理

原旧接口默认不再对外暴露：

- `LostFoundController`
- `AdminLostFoundController`

处理方式为保留代码但仅在显式启用 `legacy-lostfound` profile 时生效。

### 3. 一期数据库与运行验证

已完成：

- 初始化脚本收敛到 [init.sql](/D:/Downloads/deaf-mute/sql/init.sql)
- 开发环境数据源切换到 `deafmute`
- `JDK 21 + mvnw -DskipTests compile` 通过
- `JDK 21 + mvnw -DskipTests package` 通过
- 应用短启动成功
- `GET /api/actuator` 返回 `HTTP 200`

---

## 二期第一批：识别配置基础模块

### 已落地模块

- `gesture_library`
- `phrase_template`
- `recognition_config`

### 已落地能力

- 实体、Mapper、DTO、VO、Service、Controller
- 管理端 CRUD 接口
- 客户端只读接口

### 已完成验证

- 编译通过
- `init.sql` 导入成功
- 公开接口冒烟通过：
  - `GET /api/gesture-library/enabled`
  - `GET /api/phrase-template/list`
  - `GET /api/recognition-config/active`

### 管理端联调归档

管理端本批已完成页面、API 与带 token 联调，归档见：

- [phase2_batch1_admin_acceptance.md](/D:/Downloads/deaf-mute/docs/05-管理端日志/phase2_batch1_admin_acceptance.md)

---

## 二期第二批：动作流配置模块

### 已落地模块

- `gesture_flow`
- `gesture_flow_node`
- `gesture_flow_output`

### 已落地能力

- 动作流主表维护
- 节点列表整体提交与覆盖更新
- 输出列表整体提交与覆盖更新
- 根据 `nodeCode` 或临时节点 ID 回填父子节点关系
- 根据 `endNodeCode` 或临时节点 ID 回填输出绑定结束节点
- 详情接口返回 `nodeList` 与 `outputList`

### 已提供接口

- `GET /api/admin/gesture-flow/list`
- `GET /api/admin/gesture-flow/{id}`
- `POST /api/admin/gesture-flow`
- `PUT /api/admin/gesture-flow/{id}`
- `DELETE /api/admin/gesture-flow/{id}`

### 已完成验证

- `JDK 21 + mvnw -DskipTests compile` 通过
- `init.sql` 导入成功
- `JDK 21 + mvnw -DskipTests package` 通过
- 短启动成功
- `GET /api/actuator` 返回 `HTTP 200`
- 使用 `admin / admin123` 获取 token 后，以下接口验证通过：
  - `GET /api/admin/gesture-flow/list`
  - `GET /api/admin/gesture-flow/1`

### 管理端联调归档

- [phase2_batch2_admin_acceptance.md](/D:/Downloads/deaf-mute/docs/05-管理端日志/phase2_batch2_admin_acceptance.md)

---

## 2026-04-16：识别消费层首批接口

### 本轮目标

为安卓端 uni-app 客户端提供识别消费层首批可联调接口，而不是一次性做完整识别引擎。

### 已落地接口

- `GET /api/recognition/bootstrap`
- `POST /api/recognition/session/start`
- `POST /api/recognition/predict`
- `POST /api/recognition/session/reset`
- `POST /api/recognition/session/close`
- `GET /api/recognition-record/my`

### 当前实现边界

- 支持两种输入：
  - `gestureCode`
  - `landmarks`
- 当前基础 landmarks 检测器已覆盖：
  - `is_thumbs_up`
  - `is_v_sign`
  - `is_four_sign`
  - `is_fist`
  - `is_ok_sign`
- 已接通动作流配置消费：
  - 单动作
  - 基础顺序流
- 已支持识别记录落库

### 已完成验证

- `compile` 通过
- `package` 通过
- 短启动成功
- `GET /api/recognition/bootstrap` 通过
- `POST /api/recognition/session/start` 通过
- `POST /api/recognition/predict` 通过
- `POST /api/recognition/session/reset` 通过
- 登录用户态下 `GET /api/recognition-record/my` 通过
- `predict` 结果已写入 `recognition_record`

### 安卓端对接说明

- [给安卓端+识别消费层首批接口可联调说明.md](/D:/Downloads/deaf-mute/docs/交流/给安卓端+识别消费层首批接口可联调说明.md)

---

## 2026-04-17：landmarks 直传联调补充

### 本轮输入

本轮需求来自：

- [landmarks_refactor_2026-04-17.md](/D:/Downloads/deaf-mute/docs/06-安卓端日志/landmarks_refactor_2026-04-17.md)
- [给后端+landmarks直传联调补充需求.md](/D:/Downloads/deaf-mute/docs/交流/给后端+landmarks直传联调补充需求.md)

### 本轮补充目标

- 固定 landmarks 直传坐标契约
- 明确 handedness / mirrored 当前支持边界
- 给 `bootstrap` 增加 landmarks 能力摘要
- 给 `predict` 增加联调调试字段
- 给 `recognition_record` 增加输入来源字段

### 本轮后端改动

- `RecognitionPredictDTO`
  - 新增 `handedness`
  - 新增 `mirrored`
  - 新增 `cameraFacing`
- `RecognitionBootstrapVO`
  - 新增 `supportedLandmarkGestureCodes`
  - 新增 `landmarkPointCount`
  - 新增 `supportsHandedness`
  - 新增 `supportsMirrored`
  - 新增 landmarks 契约说明字段
- `RecognitionResultVO`
  - 新增 `inputType`
  - 新增 `stableCount`
  - 新增 `requiredHits`
  - 新增 `debounced`
  - 新增 `detectedGestureCandidates`
- `recognition_record`
  - 新增 `input_mode`
  - 新增 `source`

### 当前固定口径

当前后端按以下契约处理 landmarks：

- 坐标模式：`NORMALIZED_XYZ_0_TO_1`
- 原点：`TOP_LEFT`
- 点位顺序：`MEDIAPIPE_HANDS_21`
- 手模式：`SINGLE_HAND_FIRST_ONLY`

当前也允许安卓端在 `predict` 中透传：

- `handedness`
- `mirrored`
- `cameraFacing`

但这三个字段目前仅作为联调上下文透传与记录，基础检测器尚未按这些字段切左右手或镜像分支。

### 本轮验证结果

已完成实际验证：

- `JDK 21 + mvnw -DskipTests compile` 通过
- `JDK 21 + mvnw -DskipTests package` 通过
- `GET /api/recognition/bootstrap` 返回 `HTTP 200`
- `POST /api/recognition/session/start` 返回成功
- landmarks 模式连续 3 次 `POST /api/recognition/predict` 返回成功
- 第 1、2 次返回：
  - `inputType = landmarks`
  - `stableCount = 1 / 2`
  - `requiredHits = 3`
  - `detectedGestureCandidates = [is_v_sign]`
- 第 3 次正式命中：
  - `gesture = is_v_sign`
  - `matchedFlowCode = single_hello`
  - `matchedNodeCode = start`
  - `outputType = TEXT`
- 本地数据库补列后，`recognition_record` 实写验证通过：
  - `input_mode = landmarks`
  - `source = android-landmarks`

### 本轮对接文档

- [给安卓端+landmarks直传联调口径补充说明.md](/D:/Downloads/deaf-mute/docs/交流/给安卓端+landmarks直传联调口径补充说明.md)

---

## 当前结论

截至 2026-04-17：

- 一期内容平台后端已收口
- 管理端一期与二期前两批已完成
- 后端已具备识别配置、动作流配置、识别消费层首批接口能力
- landmarks 直传联调需要的首批补充字段已完成
- 当前下一步可以继续推进：
  - 识别引擎对 handedness / mirrored 的实质判定支持
  - 多手场景输入协议
  - 客户端识别记录与统计闭环
