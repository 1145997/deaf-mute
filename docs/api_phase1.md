# 一期 API 文档

本文档对应当前仓库已落地并可运行的第一期后端接口。接口统一前缀为：

```text
/api
```

返回结构统一为：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

分页接口统一为：

```json
{
  "list": [],
  "total": 0,
  "pageNum": 1,
  "pageSize": 10
}
```

## 说明

- 管理端接口统一放在 `/api/admin/**`
- 用户态接口用用户 token
- 管理端接口用管理员 token
- JWT 请求头统一：

```text
Authorization: Bearer <token>
```

- 旧 `/api/lostfound/**` 已默认下线，不再作为当前文档内容

## 1. 管理员认证

### POST `/api/auth/admin/login`

说明：
- 管理员登录

请求体：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

### GET `/api/auth/admin/info`

说明：
- 获取当前管理员信息

### POST `/api/auth/admin/logout`

说明：
- 管理员退出登录

## 2. 用户认证

### POST `/api/auth/user/login`

说明：
- 普通用户登录

### POST `/api/auth/user/wx-login`

说明：
- 微信登录入口

## 3. 分类接口

### GET `/api/category/list`

说明：
- 获取启用分类

查询参数：
- `type`：可选，`POST` 或 `LEARNING`

### GET `/api/admin/category/list`

查询参数：
- `pageNum`
- `pageSize`
- `keyword`
- `type`
- `status`

### POST `/api/admin/category`

请求体：

```json
{
  "name": "交流分享",
  "type": "POST",
  "icon": null,
  "description": "社区交流帖子",
  "sort": 1,
  "status": 1
}
```

### PUT `/api/admin/category/{id}`

### DELETE `/api/admin/category/{id}`

## 4. 帖子接口

### POST `/api/post`

说明：
- 发布帖子

请求体：

```json
{
  "categoryId": 1,
  "title": "手语学习打卡第一天",
  "content": "今天开始做手语学习记录，欢迎大家一起交流基础词汇和练习方法。",
  "coverImage": "https://example.com/post1.jpg",
  "imageList": [
    "https://example.com/post1.jpg"
  ],
  "sourceType": "MANUAL",
  "sourceRecordId": null
}
```

### GET `/api/post/list`

查询参数：
- `pageNum`
- `pageSize`
- `categoryId`
- `keyword`
- `status`
- `sortBy`

补充：
- 不传 `status` 时控制器默认查已发布帖子
- `sortBy=hot` 时按浏览数和评论数排序

### GET `/api/post/{id}`

说明：
- 帖子详情
- 自动增加浏览数

### GET `/api/post/my`

### PUT `/api/post/{id}`

说明：
- 修改我的帖子
- 修改后重新进入审核

### DELETE `/api/post/{id}`

## 5. 后台帖子接口

### GET `/api/admin/post/pending`

查询参数：
- `pageNum`
- `pageSize`
- `keyword`
- `categoryId`

### GET `/api/admin/post/list`

查询参数：
- `pageNum`
- `pageSize`
- `keyword`
- `categoryId`
- `status`
- `userId`
- `sourceType`

### GET `/api/admin/post/{id}`

### PUT `/api/admin/post/{id}/approve`

### PUT `/api/admin/post/{id}/reject`

请求体：

```json
{
  "auditReason": "内容不符合规范"
}
```

### PUT `/api/admin/post/{id}/status`

请求体：

```json
{
  "status": 3
}
```

状态含义：
- `0` 待审核
- `1` 已发布
- `2` 已驳回
- `3` 已下架

## 6. 评论接口

### POST `/api/comment`

请求体：

```json
{
  "postId": 1,
  "parentId": null,
  "content": "可以先从固定问候词开始练，动作节奏稳定最重要。"
}
```

### GET `/api/comment/list`

查询参数：
- `postId`

## 7. 后台评论接口

### GET `/api/admin/comment/list`

查询参数：
- `pageNum`
- `pageSize`
- `postId`
- `userId`
- `status`
- `keyword`

### DELETE `/api/admin/comment/{id}`

### PUT `/api/admin/comment/{id}/status`

请求体：

```json
{
  "status": 0
}
```

### PUT `/api/admin/comment/{id}/hide`

### PUT `/api/admin/comment/{id}/show`

## 8. 公告接口

### GET `/api/notice/public/list`

### GET `/api/notice/public/{id}`

### POST `/api/admin/notice`

请求体：

```json
{
  "title": "平台上线通知",
  "content": "欢迎使用智能手语翻译工具一期版本。",
  "isTop": 1,
  "status": 1
}
```

### GET `/api/admin/notice/list`

### PUT `/api/admin/notice/{id}`

### DELETE `/api/admin/notice/{id}`

## 9. 用户管理接口

### GET `/api/admin/user/list`

查询参数：
- `pageNum`
- `pageSize`
- `keyword`
- `status`

### GET `/api/admin/user/{id}`

### PUT `/api/admin/user/{id}/status`

请求体：

```json
{
  "status": 0
}
```

## 10. 文件上传接口

### POST `/api/file/upload`

说明：
- 上传文件到 MinIO

请求方式：
- `multipart/form-data`

表单字段：
- `file`

当前对象前缀：

```text
deafmute/
```

## 11. 统计接口

### GET `/api/admin/statistics/overview`

当前返回字段：
- `totalUsers`
- `totalPosts`
- `pendingPosts`
- `totalComments`
- `totalCategories`
- `totalNotices`

### GET `/api/admin/statistics/content-trend`

### GET `/api/admin/statistics/trend/recent7days`

说明：
- 与 `content-trend` 兼容的旧风格别名接口

## 12. 测试接口

### GET `/api/test/hello`

当前返回：

```text
hello campus lost found
```

说明：
- 该返回值还没改文案，只保留最基础测试入口

## 13. 已默认下线接口

以下旧接口默认不会注册：

- `/api/lostfound/**`
- `/api/admin/lostfound/**`

原因：
- 对应控制器已加 `@Profile("legacy-lostfound")`
- 默认启动不启用该 profile
