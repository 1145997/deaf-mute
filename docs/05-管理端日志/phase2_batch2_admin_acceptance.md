# 二期第二批管理端对接记录

日期：2026-04-15

## 对接范围

- 动作流管理

## 本轮新增

- 管理端 API：
  - `src/common/apis/gesture-flow.ts`
- 管理端页面：
  - `src/pages/gesture-flow/list.vue`
- 路由与菜单：
  - 在“识别配置”分组下新增“动作流管理”

## 页面能力

### 动作流列表页

- 支持关键词、动作流类型、状态、是否内置筛选
- 支持列表、详情、新增、编辑、删除
- 列表字段覆盖：
  - `flowCode`
  - `flowName`
  - `flowType`
  - `triggerMode`
  - `status`
  - `priority`
  - `versionNo`
  - `isBuiltin`
  - `description`

### 动作流编辑弹窗

- 拆分为三个区域：
  - 基本信息
  - 节点配置
  - 输出配置
- 节点配置使用树表格展示父子关系
- 输出配置按结束节点绑定输出行为

### 节点配置

- 支持新增、编辑、删除节点
- 支持字段：
  - `nodeCode`
  - `nodeName`
  - `parentNodeId`
  - `gestureLibraryId`
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

### 输出配置

- 支持新增、编辑、删除输出配置
- 支持字段：
  - `endNodeId`
  - `outputType`
  - `outputText`
  - `phraseTemplateId`
  - `controlAction`
  - `ttsText`
  - `displayText`

## 实现说明

- 页面内部仍使用本地节点 ID 组织树结构和结束节点选择
- 实际提交给后端时，当前第一版统一走：
  - `parentNodeCode`
  - `endNodeCode`
- 这样可以稳定完成“新增 / 修改 / 回显”闭环，不依赖数据库真实节点 ID

## 验证结果

### 前端构建

- 已执行 `pnpm build`
- 构建通过

### 后端联调

- 已使用管理员账号 `admin / admin123` 完成带 token 联调
- 已验证：
  - `GET /api/admin/gesture-flow/list`
  - `GET /api/admin/gesture-flow/{id}`
  - `POST /api/admin/gesture-flow`
  - `PUT /api/admin/gesture-flow/{id}`
  - `DELETE /api/admin/gesture-flow/{id}`
- 已完成一轮完整闭环冒烟：
  - 新增动作流成功
  - 详情回显成功
  - 更新动作流成功
  - 删除动作流成功

## 当前结论

- 二期第二批“动作流配置层”管理端页面已接通
- 当前版本已经满足“能配置、能保存、能回显、能删除”的第一版目标
- 下一阶段可以转入识别引擎消费层，或继续细化动作流页面体验
