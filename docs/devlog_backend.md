# 后端开发日志

日期：2026-04-15

## 记录说明

本文件用于记录当前项目在“后端主线”上的实际推进情况，重点保留：

- 已完成的模块改造
- 已执行的数据库与启动验证
- 与管理端联调相关的后端交付状态
- 二期各批次后端能力落地情况

---

## 一期后端收口记录

### 1. 主业务语义迁移

已完成从“校园失物招领系统”到“智能手语翻译工具内容平台”的一期后端迁移，核心调整如下：

- 新增 `post` 模块，承接原 `lostfound` 的发布、列表、详情、我的发布、审核流程
- `comment` 从 `infoId` 语义切换为 `postId`
- `category` 升级为通用分类，支持 `POST / LEARNING`
- `statistics` 统计口径从失物招领切换为内容平台一期口径
- 文件上传目录前缀从 `lostfound/` 调整为 `deafmute/`

### 2. 旧业务入口处理

- 原 `LostFoundController`
- 原 `AdminLostFoundController`

默认已不再对外暴露，处理方式为：

```java
@Profile("legacy-lostfound")
```

即默认启动时不注册旧接口，仅在显式启用 `legacy-lostfound` profile 时保留兼容能力。

### 3. 一期数据库与运行验证

已完成：

- 初始化脚本收敛到 [`sql/init.sql`](/D:/Downloads/deaf-mute/sql/init.sql)
- 开发环境数据源切换到 `deafmute`
- `JDK 21` 下 `mvnw -DskipTests compile` 通过
- `mvnw -DskipTests package` 通过
- 应用短启动成功
- `GET /api/actuator` 返回 `HTTP 200`

### 4. 一期文档整理

已产出：

- [api_phase1.md](/D:/Downloads/deaf-mute/docs/api_phase1.md)
- [development_roadmap.md](/D:/Downloads/deaf-mute/docs/development_roadmap.md)
- [phase1_closeout.md](/D:/Downloads/deaf-mute/docs/phase1_closeout.md)
- [README.md](/D:/Downloads/deaf-mute/README.md)

---

## 二期启动记录

### 1. 二期范围确认

依据以下文档确认二期主线为“识别配置后台建设”：

- [phase2_kickoff.md](/D:/Downloads/deaf-mute/docs/phase2_kickoff.md)
- [backend_api_dto_vo_checklist.md](/D:/Downloads/deaf-mute/docs/backend_api_dto_vo_checklist.md)
- [deafmute_schema_draft.sql](/D:/Downloads/deaf-mute/docs/deafmute_schema_draft.sql)

二期第一批优先模块确定为：

- `gesture_library`
- `phrase_template`
- `recognition_config`

### 2. 二期第一批后端落地内容

本轮后端已新增以下模块：

#### 基础手势库 `gesture_library`

已落地：

- 实体
- Mapper
- DTO
- Service / ServiceImpl
- 客户端 Controller
- 管理端 Controller

接口范围：

- `GET /api/gesture-library/enabled`
- `GET /api/gesture-library/list`
- `GET /api/admin/gesture-library/list`
- `GET /api/admin/gesture-library/{id}`
- `POST /api/admin/gesture-library`
- `PUT /api/admin/gesture-library/{id}`
- `PUT /api/admin/gesture-library/{id}/status`

#### 短语模板 `phrase_template`

已落地：

- 实体
- Mapper
- DTO
- Service / ServiceImpl
- 客户端 Controller
- 管理端 Controller

接口范围：

- `GET /api/phrase-template/list`
- `GET /api/admin/phrase-template/list`
- `GET /api/admin/phrase-template/{id}`
- `POST /api/admin/phrase-template`
- `PUT /api/admin/phrase-template/{id}`
- `PUT /api/admin/phrase-template/{id}/status`

#### 全局识别配置 `recognition_config`

已落地：

- 实体
- Mapper
- DTO
- Service / ServiceImpl
- 客户端 Controller
- 管理端 Controller

接口范围：

- `GET /api/recognition-config/active`
- `GET /api/admin/recognition-config/list`
- `GET /api/admin/recognition-config/active`
- `GET /api/admin/recognition-config/{id}`
- `POST /api/admin/recognition-config`
- `PUT /api/admin/recognition-config/{id}`
- `PUT /api/admin/recognition-config/{id}/activate`

### 3. 二期第一批数据库更新

已将 [`sql/init.sql`](/D:/Downloads/deaf-mute/sql/init.sql) 更新为“当前真实后端版本”，新增：

- `gesture_library`
- `phrase_template`
- `recognition_config`

并补充默认演示数据：

- 基础手势 5 条
- 短语模板 4 条
- 默认识别配置 1 条

### 4. 二期第一批后端验证结果

已完成实际验证：

- 新增代码编译通过
- 新版 `init.sql` 已成功导入数据库
- 应用二期版本短启动成功
- `GET /api/actuator` 返回 `HTTP 200`
- 以下二期公开接口已冒烟通过：
  - `GET /api/gesture-library/enabled`
  - `GET /api/phrase-template/list`
  - `GET /api/recognition-config/active`

---

## 管理端对接归档

管理端当前状态：二期第一批已完成。

已知管理端交付内容：

- 3 组管理端 API 已落地
  - `gesture-library.ts`
  - `phrase-template.ts`
  - `recognition-config.ts`
- 3 个管理页已落地
  - `gesture-library/list.vue`
  - `phrase-template/list.vue`
  - `recognition-config/list.vue`
- 菜单入口已挂到“识别配置”分组
- `gestureOrderJson` 已支持更友好的录入方式
  - 支持 JSON 数组
  - 支持逗号输入
  - 支持换行输入
- `pnpm build` 已通过
- 已使用 `admin / admin123` 完成带 token 的后台联调

已确认正常返回的管理端接口能力：

- 列表
- 详情
- 当前生效配置

管理端本轮验收文档：

- [phase2_batch1_admin_acceptance.md](/D:/Downloads/deaf-mute/docs/phase2_batch1_admin_acceptance.md)

---

## 二期第二批后端记录

### 1. 本批目标

二期第二批后端聚焦“动作流配置能力”落地，核心范围为：

- `gesture_flow`
- `gesture_flow_node`
- `gesture_flow_output`

### 2. 本批已落地内容

已新增 `gestureflow` 模块，包含：

- 实体
- Mapper
- DTO
- VO
- Service / ServiceImpl
- 管理端 Controller

已提供管理端接口：

- `GET /api/admin/gesture-flow/list`
- `GET /api/admin/gesture-flow/{id}`
- `POST /api/admin/gesture-flow`
- `PUT /api/admin/gesture-flow/{id}`
- `DELETE /api/admin/gesture-flow/{id}`

### 3. 本批实现说明

本批后端已支持：

- 动作流主表维护
- 节点列表整体提交与覆盖更新
- 输出列表整体提交与覆盖更新
- 根据 `nodeCode` 或节点临时 ID 回填父节点关系
- 根据 `endNodeCode` 或节点临时 ID 回填输出绑定的结束节点
- 详情接口返回 `nodeList` 与 `outputList`
- 详情接口补充基础手势名称、结束节点名称、短语文本等展示字段

### 4. 本批数据库更新

已同步更新 [`sql/init.sql`](/D:/Downloads/deaf-mute/sql/init.sql)，新增：

- `gesture_flow`
- `gesture_flow_node`
- `gesture_flow_output`

并补充演示数据：

- `single_hello`
- `flow_thanks`

### 5. 本批验证结果

已完成实际验证：

- `JDK 21 + mvnw -DskipTests compile` 通过
- 新版 `init.sql` 导入成功
- 清理旧打包产物后，`mvnw -DskipTests package` 成功
- 可执行 jar 短启动成功
- `GET /api/actuator` 返回 `HTTP 200`
- 使用 `admin / admin123` 获取 token 后，以下接口已验证通过：
  - `GET /api/admin/gesture-flow/list`
  - `GET /api/admin/gesture-flow/1`

---

## 当前阶段结论

截至 2026-04-15：

- 一期内容平台后端已收口
- 管理端一期已完成
- 二期第一批“识别配置后台基础能力”前后端均已落地
- 二期第二批后端已落地并完成基础联调验证
- 下一步可进入二期第二批管理端页面对接，或继续推进识别引擎消费层

---

## 下一步建议

建议按以下顺序推进二期后端：

1. 对接二期第二批管理端动作流页面
2. 补充动作流新增/修改接口的管理端联调验收
3. 进入识别引擎读取配置的消费层开发
4. 新增 `recognition_record`
5. 为客户端识别联调准备运行时状态模型
