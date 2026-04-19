# 全量 API 手册

日期：2026-04-18

适用范围：当前仓库后端实际已落地接口  
服务基址：`http://{host}:8080/api`  
上下文路径：`/api`

说明：

- 本文档按“当前代码真实存在的接口”整理
- 旧 `lostfound` 控制器默认通过 `legacy-lostfound` profile 下线，因此不计入正式接口手册
- 返回结构统一使用 `Result<T>`

---

## 1. 通用约定

## 1.1 通用响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

失败时：

```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

## 1.2 鉴权头

需要登录的接口统一使用：

```http
Authorization: Bearer {token}
```

其中：

- 管理端接口使用管理员 token
- 用户端接口使用用户 token

## 1.3 分页返回约定

多数分页接口返回：

```json
{
  "list": [],
  "total": 0,
  "pageNum": 1,
  "pageSize": 10
}
```

---

## 2. 认证接口

## 2.1 管理员登录

### `POST /auth/admin/login`

用途：管理员登录

请求体：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

返回 `data`：

- `token`
- `username`
- `nickname`
- `role`

### `GET /auth/admin/info`

用途：获取当前管理员信息  
鉴权：管理员 token

返回 `data`：

- `id`
- `username`
- `nickname`
- `role`

### `POST /auth/admin/logout`

用途：管理员退出登录  
鉴权：管理员 token

返回：空数据，`message` 为退出成功

## 2.2 用户登录

### `POST /auth/user/login`

用途：普通用户 openid 登录

请求体：

```json
{
  "openid": "test_openid_001",
  "nickname": "张三",
  "avatar": "https://example.com/a.png"
}
```

返回 `data`：

- `token`
- `userId`
- `openid`
- `nickname`
- `avatar`

### `POST /auth/user/wx-login`

用途：微信小程序 code 登录

请求体：

```json
{
  "code": "wx-code",
  "nickname": "张三",
  "avatar": "https://example.com/a.png"
}
```

返回 `data`：

- `token`
- `userId`
- `openid`
- `nickname`
- `avatar`

---

## 3. 分类接口

## 3.1 客户端分类

### `GET /category/list`

用途：获取启用中的分类列表

查询参数：

- `type` 可选，典型值：`POST`、`LEARNING`

返回 `data`：`Category[]`

主要字段：

- `id`
- `name`
- `type`
- `icon`
- `description`
- `sort`
- `status`

## 3.2 管理端分类

### `GET /admin/category/list`

用途：分类分页查询  
鉴权：管理员 token

查询参数：

- `keyword`
- `type`
- `status`
- `pageNum`
- `pageSize`

### `POST /admin/category`

用途：新增分类  
鉴权：管理员 token

请求体：

```json
{
  "name": "交流分享",
  "type": "POST",
  "icon": "",
  "description": "社区交流帖子",
  "sort": 1,
  "status": 1
}
```

### `PUT /admin/category/{id}`

用途：修改分类  
鉴权：管理员 token

请求体同新增

### `DELETE /admin/category/{id}`

用途：删除分类  
鉴权：管理员 token

---

## 4. 帖子接口

## 4.1 客户端帖子

### `POST /post`

用途：发布帖子  
鉴权：用户 token

请求体：

```json
{
  "categoryId": 1,
  "title": "手语学习打卡第一天",
  "content": "今天开始学习手语",
  "coverImage": "https://example.com/cover.jpg",
  "imageList": [
    "https://example.com/1.jpg",
    "https://example.com/2.jpg"
  ],
  "sourceType": "MANUAL",
  "sourceRecordId": null
}
```

字段说明：

- `sourceType` 常见值：`MANUAL`、`RECOGNITION`
- `sourceRecordId` 可用于识别结果转帖子的场景

### `GET /post/list`

用途：帖子分页列表

查询参数：

- `pageNum`
- `pageSize`
- `categoryId`
- `keyword`
- `status`
- `sortBy`

说明：

- 如果未传 `status`，控制器会默认按 `1` 处理，即已发布帖子

返回 `list` 元素主要字段：

- `id`
- `userId`
- `userNickname`
- `userAvatar`
- `categoryId`
- `categoryName`
- `title`
- `contentPreview`
- `coverImage`
- `status`
- `viewCount`
- `commentCount`
- `likeCount`
- `sourceType`
- `createTime`

### `GET /post/{id}`

用途：帖子详情

返回 `data` 主要字段：

- `id`
- `userId`
- `userNickname`
- `userAvatar`
- `categoryId`
- `categoryName`
- `title`
- `content`
- `coverImage`
- `imageList`
- `status`
- `viewCount`
- `commentCount`
- `likeCount`
- `sourceType`
- `sourceRecordId`
- `auditReason`
- `createTime`

### `GET /post/my`

用途：我的帖子分页  
鉴权：用户 token

查询参数：

- `pageNum`
- `pageSize`

### `PUT /post/{id}`

用途：修改自己的帖子  
鉴权：用户 token

请求体：

```json
{
  "categoryId": 1,
  "title": "修改后的标题",
  "content": "修改后的内容",
  "coverImage": "https://example.com/cover.jpg",
  "imageList": [
    "https://example.com/1.jpg"
  ]
}
```

说明：

- 当前消息语义是“修改成功，重新进入审核”

### `DELETE /post/{id}`

用途：删除自己的帖子  
鉴权：用户 token

## 4.2 管理端帖子

### `GET /admin/post/pending`

用途：待审核帖子分页  
鉴权：管理员 token

查询参数：

- `pageNum`
- `pageSize`
- `keyword`
- `categoryId`

### `GET /admin/post/list`

用途：管理员帖子分页查询  
鉴权：管理员 token

查询参数：

- `pageNum`
- `pageSize`
- `keyword`
- `categoryId`
- `status`
- `userId`
- `sourceType`

### `GET /admin/post/{id}`

用途：帖子详情  
鉴权：管理员 token

### `PUT /admin/post/{id}/approve`

用途：审核通过  
鉴权：管理员 token

### `PUT /admin/post/{id}/reject`

用途：驳回帖子  
鉴权：管理员 token

请求体：

```json
{
  "auditReason": "内容不符合规范"
}
```

### `PUT /admin/post/{id}/status`

用途：修改帖子状态  
鉴权：管理员 token

请求体：

```json
{
  "status": 3
}
```

常见状态语义：

- `0` 待审核
- `1` 已发布
- `2` 已驳回
- `3` 已下架

---

## 5. 评论接口

## 5.1 客户端评论

### `POST /comment`

用途：发表评论  
鉴权：用户 token

请求体：

```json
{
  "postId": 1,
  "parentId": null,
  "content": "这条评论很有帮助"
}
```

### `GET /comment/list`

用途：按帖子获取评论树

查询参数：

- `postId` 必填

返回 `data`：`CommentVO[]`

主要字段：

- `id`
- `postId`
- `userId`
- `userNickname`
- `userAvatar`
- `parentId`
- `content`
- `status`
- `createTime`
- `children`

## 5.2 管理端评论

### `GET /admin/comment/list`

用途：评论分页查询  
鉴权：管理员 token

查询参数：

- `postId`
- `userId`
- `status`
- `keyword`
- `pageNum`
- `pageSize`

### `DELETE /admin/comment/{id}`

用途：删除评论  
鉴权：管理员 token

### `PUT /admin/comment/{id}/status`

用途：修改评论状态  
鉴权：管理员 token

请求体：

```json
{
  "status": 0
}
```

### `PUT /admin/comment/{id}/hide`

用途：隐藏评论  
鉴权：管理员 token

### `PUT /admin/comment/{id}/show`

用途：恢复评论显示  
鉴权：管理员 token

---

## 6. 公告接口

## 6.1 客户端公告

### `GET /notice/public/list`

用途：获取已发布公告列表

返回：`Notice[]`

主要字段：

- `id`
- `title`
- `content`
- `isTop`
- `status`
- `publishAdminId`
- `createTime`
- `updateTime`

### `GET /notice/public/{id}`

用途：公告详情

## 6.2 管理端公告

### `POST /admin/notice`

用途：新增公告  
鉴权：管理员 token

请求体：

```json
{
  "title": "系统公告",
  "content": "公告内容",
  "isTop": 1,
  "status": 1
}
```

### `GET /admin/notice/list`

用途：公告分页  
鉴权：管理员 token

查询参数：

- `pageNum`
- `pageSize`

### `PUT /admin/notice/{id}`

用途：修改公告  
鉴权：管理员 token

请求体同新增

### `DELETE /admin/notice/{id}`

用途：删除公告  
鉴权：管理员 token

---

## 7. 文件接口

### `POST /file/upload`

用途：上传文件到 MinIO

请求方式：

- `multipart/form-data`

表单字段：

- `file`

返回 `data`：

- `fileName`
- `objectName`
- `url`

说明：

- 当前对象前缀为 `deafmute/`

---

## 8. 用户管理接口

说明：

- 这组接口当前代码里没有显式调用 `adminTokenUtils.checkAdminLogin()`
- 从业务语义上应视为管理端接口
- 建议后续尽快补鉴权保护

### `GET /admin/user/list`

用途：用户分页查询

查询参数：

- `keyword`
- `status`
- `pageNum`
- `pageSize`

返回 `list` 元素主要字段：

- `id`
- `openid`
- `nickname`
- `avatar`
- `realName`
- `studentNo`
- `phone`
- `email`
- `gender`
- `status`
- `lastLoginTime`
- `createTime`

### `GET /admin/user/{id}`

用途：用户详情

### `PUT /admin/user/{id}/status`

用途：修改用户状态

请求体：

```json
{
  "status": 0
}
```

常见状态：

- `1` 正常
- `0` 禁用

---

## 9. 统计接口

### `GET /admin/statistics/overview`

用途：仪表盘总览  
鉴权：管理员 token

返回 `data`：

- `totalUsers`
- `totalPosts`
- `pendingPosts`
- `totalComments`
- `totalCategories`
- `totalNotices`

### `GET /admin/statistics/content-trend`

用途：最近 7 天内容趋势  
鉴权：管理员 token

返回：

```json
{
  "list": [
    {
      "date": "2026-04-18",
      "count": 5
    }
  ]
}
```

### `GET /admin/statistics/trend/recent7days`

用途：最近 7 天趋势兼容接口  
鉴权：管理员 token

说明：

- 当前实现等价于 `/admin/statistics/content-trend`

---

## 10. 基础手势库接口

## 10.1 客户端

### `GET /gesture-library/enabled`

用途：获取启用的基础手势列表

### `GET /gesture-library/list`

用途：获取启用的基础手势列表

说明：

- 当前两个接口行为一致

返回元素主要字段：

- `id`
- `gestureCode`
- `gestureName`
- `description`
- `previewImage`
- `status`
- `sort`
- `isBuiltin`
- `detectionKey`

## 10.2 管理端

### `GET /admin/gesture-library/list`

用途：基础手势分页  
鉴权：管理员 token

查询参数：

- `pageNum`
- `pageSize`
- `keyword`
- `status`
- `isBuiltin`

### `GET /admin/gesture-library/{id}`

用途：基础手势详情  
鉴权：管理员 token

### `POST /admin/gesture-library`

用途：新增基础手势  
鉴权：管理员 token

请求体：

```json
{
  "gestureCode": "is_v_sign",
  "gestureName": "V手势",
  "description": "剪刀手",
  "previewImage": "",
  "status": 1,
  "sort": 2,
  "isBuiltin": 1,
  "detectionKey": "is_v_sign"
}
```

### `PUT /admin/gesture-library/{id}`

用途：修改基础手势  
鉴权：管理员 token

### `PUT /admin/gesture-library/{id}/status`

用途：修改手势状态  
鉴权：管理员 token

请求体：

```json
{
  "status": 1
}
```

---

## 11. 短语模板接口

## 11.1 客户端

### `GET /phrase-template/list`

用途：获取启用的短语模板

查询参数：

- `sceneType` 可选

返回元素主要字段：

- `id`
- `phraseCode`
- `phraseText`
- `ttsText`
- `sceneType`
- `status`
- `sort`

## 11.2 管理端

### `GET /admin/phrase-template/list`

用途：短语模板分页  
鉴权：管理员 token

查询参数：

- `pageNum`
- `pageSize`
- `keyword`
- `sceneType`
- `status`

### `GET /admin/phrase-template/{id}`

用途：短语模板详情  
鉴权：管理员 token

### `POST /admin/phrase-template`

用途：新增短语模板  
鉴权：管理员 token

请求体：

```json
{
  "phraseCode": "hello",
  "phraseText": "你好",
  "ttsText": "你好",
  "sceneType": "daily",
  "status": 1,
  "sort": 1
}
```

### `PUT /admin/phrase-template/{id}`

用途：修改短语模板  
鉴权：管理员 token

### `PUT /admin/phrase-template/{id}/status`

用途：修改短语模板状态  
鉴权：管理员 token

请求体：

```json
{
  "status": 1
}
```

---

## 12. 全局识别配置接口

## 12.1 客户端

### `GET /recognition-config/active`

用途：获取当前生效识别配置

返回 `data` 主要字段：

- `id`
- `configName`
- `confidenceMin`
- `holdMs`
- `debounceMs`
- `cooldownMs`
- `requiredHits`
- `maxIntervalMs`
- `lockTimeoutMs`
- `resetOnFail`
- `allowRepeat`
- `gestureOrderJson`
- `activeFlag`
- `remark`

## 12.2 管理端

### `GET /admin/recognition-config/list`

用途：识别配置分页  
鉴权：管理员 token

查询参数：

- `pageNum`
- `pageSize`
- `keyword`
- `activeFlag`

### `GET /admin/recognition-config/active`

用途：获取当前生效识别配置  
鉴权：管理员 token

### `GET /admin/recognition-config/{id}`

用途：识别配置详情  
鉴权：管理员 token

### `POST /admin/recognition-config`

用途：新增识别配置  
鉴权：管理员 token

请求体：

```json
{
  "configName": "默认识别配置",
  "confidenceMin": 0.5,
  "holdMs": 300,
  "debounceMs": 500,
  "cooldownMs": 1000,
  "requiredHits": 3,
  "maxIntervalMs": 1500,
  "lockTimeoutMs": 3000,
  "resetOnFail": 1,
  "allowRepeat": 0,
  "gestureOrderJson": "[\"is_thumbs_up\",\"is_v_sign\"]",
  "activeFlag": 1,
  "remark": "默认配置"
}
```

### `PUT /admin/recognition-config/{id}`

用途：修改识别配置  
鉴权：管理员 token

### `PUT /admin/recognition-config/{id}/activate`

用途：启用指定识别配置  
鉴权：管理员 token

---

## 13. 动作流配置接口

当前动作流接口只有管理端。

## 13.1 管理端

### `GET /admin/gesture-flow/list`

用途：动作流分页  
鉴权：管理员 token

查询参数：

- `pageNum`
- `pageSize`
- `keyword`
- `flowType`
- `status`
- `isBuiltin`

### `GET /admin/gesture-flow/{id}`

用途：动作流详情  
鉴权：管理员 token

返回 `data` 主要字段：

- 主体：
  - `id`
  - `flowCode`
  - `flowName`
  - `flowType`
  - `triggerMode`
  - `status`
  - `priority`
  - `versionNo`
  - `startNodeId`
  - `isBuiltin`
  - `description`
- `nodeList`
- `outputList`

`nodeList` 主要字段：

- `id`
- `nodeCode`
- `nodeName`
- `parentNodeId`
- `gestureLibraryId`
- `gestureCode`
- `gestureName`
- `isStart`
- `isEnd`
- `nodeOrder`
- `confidenceMin`
- `holdMs`
- `debounceMs`
- `cooldownMs`
- `requiredHits`
- `maxIntervalMs`
- `resetOnFail`
- `allowRepeat`
- `successNextStrategy`
- `failStrategy`
- `remark`

`outputList` 主要字段：

- `id`
- `endNodeId`
- `endNodeCode`
- `endNodeName`
- `outputType`
- `outputText`
- `phraseTemplateId`
- `phraseText`
- `controlAction`
- `ttsText`
- `displayText`

### `POST /admin/gesture-flow`

用途：新增动作流  
鉴权：管理员 token

请求体示例：

```json
{
  "flowCode": "single_hello",
  "flowName": "单动作-你好",
  "flowType": "SINGLE",
  "triggerMode": "DIRECT",
  "status": 1,
  "priority": 100,
  "isBuiltin": 0,
  "description": "V手势输出你好",
  "nodeList": [
    {
      "nodeCode": "start",
      "nodeName": "起始节点",
      "gestureLibraryId": 2,
      "isStart": 1,
      "isEnd": 1,
      "nodeOrder": 1,
      "requiredHits": 3
    }
  ],
  "outputList": [
    {
      "endNodeCode": "start",
      "outputType": "TEXT",
      "outputText": "你好",
      "displayText": "你好",
      "ttsText": "你好"
    }
  ]
}
```

### `PUT /admin/gesture-flow/{id}`

用途：修改动作流  
鉴权：管理员 token

请求体结构同新增

### `DELETE /admin/gesture-flow/{id}`

用途：删除动作流  
鉴权：管理员 token

---

## 14. 识别消费层接口

## 14.1 `GET /recognition/bootstrap`

用途：识别页面初始化配置

返回 `data`：

- `activeConfig`
- `gestureList`
- `phraseList`
- `flowList`
- `supportedLandmarkGestureCodes`
- `landmarkPointCount`
- `supportsHandedness`
- `supportsMirrored`
- `landmarkCoordinateMode`
- `landmarkOrigin`
- `landmarkOrder`
- `landmarkHandMode`
- `landmarkAssumptions`

当前固定 landmarks 契约：

- `landmarkCoordinateMode = NORMALIZED_XYZ_0_TO_1`
- `landmarkOrigin = TOP_LEFT`
- `landmarkOrder = MEDIAPIPE_HANDS_21`
- `landmarkHandMode = SINGLE_HAND_FIRST_ONLY`

## 14.2 `POST /recognition/session/start`

用途：启动识别会话

请求体：

```json
{
  "clientType": "android",
  "sceneCode": "bridge",
  "engineType": "mediapipe",
  "appVersion": "0.1.0"
}
```

返回 `data`：

- `sessionId`
- `startedAt`
- `activeConfigId`

## 14.3 `POST /recognition/predict`

用途：提交一帧识别输入，支持 `gestureCode` 模式和 `landmarks` 模式

请求体通用字段：

- `sessionId` 必填
- `frameNo` 可选
- `capturedAt` 可选
- `source` 可选
- `handedness` 可选
- `mirrored` 可选
- `cameraFacing` 可选

### A. gestureCode 模式

```json
{
  "sessionId": "rec_xxx",
  "gestureCode": "is_v_sign"
}
```

### B. landmarks 模式

```json
{
  "sessionId": "rec_xxx",
  "source": "android-landmarks",
  "handedness": "Right",
  "mirrored": false,
  "cameraFacing": "back",
  "landmarks": [
    { "x": 0.50, "y": 0.90, "z": 0.0 }
  ]
}
```

说明：

- 当前 landmarks 应传单手 21 点
- 当前后端只按第一只手处理

返回 `data` 主要字段：

- `gesture`
- `code`
- `label`
- `locked`
- `state`
- `outputType`
- `displayText`
- `ttsText`
- `controlAction`
- `matchedFlowCode`
- `matchedNodeCode`
- `traceId`
- `inputType`
- `stableCount`
- `requiredHits`
- `debounced`
- `detectedGestureCandidates`

字段理解：

- `locked`
  当前是否处于动作流锁定态
- `state`
  常见值：`idle`、`locked`
- `outputType`
  常见值：`NONE`、`TEXT`、`PHRASE`、`CONTROL`
- `stableCount`
  当前连续稳定命中次数
- `requiredHits`
  当前命中阈值
- `debounced`
  本次是否被防抖拦截
- `detectedGestureCandidates`
  当前帧识别出的候选基础手势

当前基础 landmarks 检测器已支持：

- `is_thumbs_up`
- `is_v_sign`
- `is_four_sign`
- `is_fist`
- `is_ok_sign`

## 14.4 `POST /recognition/session/reset`

用途：重置识别会话状态

请求体：

```json
{
  "sessionId": "rec_xxx",
  "reason": "manual-reset"
}
```

返回 `data`：

- `ok`
- `message`

## 14.5 `POST /recognition/session/close`

用途：关闭识别会话

请求体：

```json
{
  "sessionId": "rec_xxx",
  "reason": "page-close"
}
```

返回 `data`：

- `ok`
- `message`

## 14.6 `GET /recognition-record/my`

用途：获取我的识别记录  
鉴权：用户 token

查询参数：

- `pageNum`
- `pageSize`

返回 `list` 元素主要字段：

- `id`
- `sessionId`
- `matchedGestureCode`
- `matchedFlowCode`
- `matchedNodePath`
- `outputType`
- `outputText`
- `controlAction`
- `clientPlatform`
- `inputMode`
- `source`
- `createTime`

---

## 15. 测试接口

### `GET /test/hello`

用途：简单测试接口

返回：

```text
hello campus lost found
```

说明：

- 这是遗留测试接口，返回文案尚未迁移

---

## 16. 当前未纳入正式手册的内容

以下内容当前不建议作为正式对外接口使用：

- `legacy-lostfound` profile 下的旧 `/lostfound/**`
- 直接依赖数据库初始化测试数据的逻辑
- 任何未在控制器暴露的 Service 内部方法

---

## 17. 当前接口覆盖结论

截至 2026-04-18，后端正式可用接口已经覆盖：

- 管理员认证
- 用户认证
- 分类
- 帖子
- 评论
- 公告
- 文件上传
- 用户管理
- 仪表盘统计
- 基础手势库
- 短语模板
- 全局识别配置
- 动作流管理
- 识别消费层
- 识别记录

尚未进入正式接口层的目标模块主要是：

- 学习模块
- 更完整的识别引擎高级能力
- 更完整的统计分析接口
