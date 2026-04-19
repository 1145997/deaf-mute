# 安卓端 gestureCode 联调冒烟记录
日期：2026-04-17

## 结论

Spring Boot 识别消费层在 `http://127.0.0.1:8080/api` 已可访问，安卓端当前接入的 `gestureCode` 模式与后端首批接口口径一致。

本轮已实际验证：

- `GET /api/recognition/bootstrap` 可返回 `200`
- `POST /api/recognition/session/start` 可返回 `sessionId`
- `POST /api/recognition/predict` 在 `gestureCode` 模式下可命中单动作流
- `POST /api/recognition/session/reset` 可成功重置会话
- `POST /api/recognition/session/close` 可成功关闭会话

## 单动作验证

测试口径：

- `clientType = UNI_APP_ANDROID`
- `sceneCode = HOME`
- `engineType = MEDIAPIPE_JS`
- `gestureCode = is_v_sign`

实测结果：

- 当前生效配置 `requiredHits = 3`
- 前两次 `predict` 返回空结果，`state = idle`
- 第三次 `predict` 命中：
  - `gesture = is_v_sign`
  - `code = single_hello`
  - `label = 你好`
  - `outputType = TEXT`
  - `matchedFlowCode = single_hello`
  - `matchedNodeCode = start`

说明：

- 安卓端当前 store 按后端返回结果入句，这一点与验证结果一致
- 单动作输出链路已经可用

## 动作流验证

测试动作流：

- 第一步：`is_four_sign`
- 第二步：`is_v_sign`

实测结果：

- 第一步连续 3 次后进入锁定：
  - `code = n1`
  - `state = locked`
  - `matchedFlowCode = flow_thanks`
  - `matchedNodeCode = n1`
- 第二步如果过快切换并很快结束，后端会因为 `debounceMs = 500` 和 `requiredHits = 3` 暂时不给最终输出
- 第二步持续保持更久后，动作流可命中最终输出：
  - `gesture = is_v_sign`
  - `code = flow_thanks`
  - `label = 谢谢`
  - `outputType = PHRASE`
  - `matchedFlowCode = flow_thanks`
  - `matchedNodeCode = n2`

说明：

- 这与后端交流文档中的 `requiredHits = 3`、`maxIntervalMs = 1500`、`debounceMs = 500` 一致
- 桥接联调阶段，第二段动作不要太快松手，否则看起来像“没有结果”，本质上是命中次数还不够

## 当前状态

安卓端现状：

- `blind -> uni-app -> Spring Boot` 的 `gestureCode` 联调链路已完成
- `home / bridge / profile` 已接入首批识别消费层接口
- `npm run build` 已通过

尚未在本轮完成：

- `recognition-record/my` 的登录态真值验证

原因：

- 该接口需要用户 token，本轮未注入用户登录态
