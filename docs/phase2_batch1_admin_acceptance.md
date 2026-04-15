# 二期第一批管理端对接记录

日期：2026-04-15

## 对接范围

- 基础手势库
- 短语模板
- 全局识别配置

## 本轮新增

- 管理端 API：
  - `src/common/apis/gesture-library.ts`
  - `src/common/apis/phrase-template.ts`
  - `src/common/apis/recognition-config.ts`
- 管理端页面：
  - `src/pages/gesture-library/list.vue`
  - `src/pages/phrase-template/list.vue`
  - `src/pages/recognition-config/list.vue`
- 路由与菜单：
  - 在 `src/router/index.ts` 新增“识别配置”分组
  - 菜单项包括“基础手势库 / 短语模板 / 全局识别配置”

## 页面能力

### 基础手势库

- 支持关键词、状态、是否内置筛选
- 支持列表、详情、新增、编辑、状态切换
- 对齐字段：
  - `gestureCode`
  - `gestureName`
  - `description`
  - `previewImage`
  - `status`
  - `sort`
  - `isBuiltin`
  - `detectionKey`

### 短语模板

- 支持关键词、场景类型、状态筛选
- 支持列表、详情、新增、编辑、状态切换
- 对齐字段：
  - `phraseCode`
  - `phraseText`
  - `ttsText`
  - `sceneType`
  - `status`
  - `sort`

### 全局识别配置

- 支持配置名称、生效状态筛选
- 支持列表、详情、新增、编辑、启用配置
- 对齐字段：
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
- 前端将 `gestureOrderJson` 抽象为“手势顺序”输入，支持：
  - JSON 数组录入
  - 逗号分隔录入
  - 换行分隔录入

## 验证结果

### 前端构建

- 已执行 `pnpm build`
- 构建通过

### 管理员 token 联调

- 已使用管理员账号 `admin / admin123` 登录成功
- 已验证以下接口返回成功：
  - `GET /api/admin/gesture-library/list`
  - `GET /api/admin/gesture-library/1`
  - `GET /api/admin/phrase-template/list`
  - `GET /api/admin/phrase-template/1`
  - `GET /api/admin/recognition-config/list`
  - `GET /api/admin/recognition-config/active`
  - `GET /api/admin/recognition-config/1`

## 当前结论

- 二期第一批三张配置表的管理端页面已接通
- 前端构建与后台带 token 冒烟均已通过
- 可以继续进入二期第二批 `gesture_flow / gesture_flow_node / gesture_flow_output` 对接
