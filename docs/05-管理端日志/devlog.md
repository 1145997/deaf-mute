# 开发日志

## 2026-04-14 管理端一期首轮迁移

### 背景同步

- 项目已从旧项目业务形态迁移到“双创项目：智能手语翻译工具”一期形态。
- 后端当前默认公开的内容管理主线已经切到 `post / category / comment / notice / user / statistics`。
- 管理端实际工作目录确认为 `adminend/v3-admin-vite-main/v3-admin-vite-main`。

### 本轮改造范围

- 将原 `lostfound` 管理 API 迁移为 `post` 管理 API。
- 将原后台“失物招领管理”路由和菜单切换为“帖子管理”。
- 新增“帖子审核 / 帖子列表”页面，对接当前一期后端公开接口：
  - `GET /api/admin/post/pending`
  - `GET /api/admin/post/list`
  - `GET /api/admin/post/{id}`
  - `PUT /api/admin/post/{id}/approve`
  - `PUT /api/admin/post/{id}/reject`
  - `PUT /api/admin/post/{id}/status`
- 帖子页面的分类筛选通过 `GET /api/category/list?type=POST` 拉取启用分类。

### 页面与交互调整

- 帖子审核页：
  - 支持按分类、关键词筛选待审核帖子
  - 支持查看帖子详情、审核通过、填写驳回原因并驳回
- 帖子管理页：
  - 支持按分类、状态、来源、关键词筛选帖子
  - 支持查看帖子详情
  - 支持对已发布、已驳回、已下架帖子做状态切换
- 详情抽屉已按帖子模型展示：
  - 标题、正文、分类、发布者、来源
  - 封面图、图片列表
  - 浏览量、评论数、点赞数
  - 驳回原因、来源记录 ID

### 验证记录

- 已在 `adminend/v3-admin-vite-main/v3-admin-vite-main` 执行 `pnpm install --frozen-lockfile`。
- 已执行 `pnpm build`，构建通过。
- 构建过程中先修复了帖子页面 `el-tag` 的类型声明问题，最终 `vue-tsc + vite build` 已完成。

### 当前遗留项

- `comment` 模块仍存在旧 `infoId` 命名，需要继续对齐到 `postId` 语义。
- `category` 管理页当前只覆盖名称、排序、状态，后续仍需补齐一期需要的 `type / icon / description`。
- `statistics` 模块尚未接入一期内容平台指标。
- 旧 `router/index.ts` 仍有历史编码问题，当前已切换到新的 `router/app-router.ts` 作为管理端实际入口，后续可继续清理旧文件。
- 旧 `lostfound` 前端文件当前仍保留在仓库中，但已经不再被新路由入口引用，后续可以继续做物理清理。

## 2026-04-14 管理端一期第二轮收口

### 本轮改造范围

- `comment` 模块：
  - 前端查询参数和列表字段从旧 `infoId` 语义切换为 `postId`
  - 补充 `userId` 查询
  - 列表展示补充评论用户昵称
- `category` 模块：
  - 对齐后端真实字段 `type / icon / description`
  - 列表支持按类型筛选
  - 新增 / 编辑弹窗支持分类类型、图标、描述维护
- `statistics` / 首页仪表盘：
  - 首页总览字段切换到后端真实返回：
    - `totalUsers`
    - `totalPosts`
    - `pendingPosts`
    - `totalComments`
    - `totalCategories`
    - `totalNotices`
- 首页文案从旧后台名称切换为一期内容平台语义
  - 趋势图调整为“近 7 天帖子新增趋势”
  - 饼图调整为“平台内容构成”

### 核验结果

- `notice` 与 `user` 模块已复核，当前接口与页面语义基本匹配一期后端。
- 本轮改动后再次执行 `pnpm build`，构建通过。

### 当前剩余事项

- 旧 `lostfound` 前端 API、页面文件仍在仓库中但未被新入口使用，后续可做物理删除。
- 旧 `router/index.ts` 仍保留历史乱码内容，当前不再作为运行入口，后续可清理。
- 如果后续要扩到“学习内容”方向，分类管理和统计面板还可以继续细分 `POST / LEARNING` 维度。

## 2026-04-15 旧残留清理与一期联调验收

### 本轮清理

- 已物理删除旧前端残留：
  - `src/common/apis/lostfound.ts`
  - `src/pages/lostfound/list.vue`
  - `src/pages/lostfound/pending.vue`
  - `src/pages/lostfound/`
  - `src/router/app-router.ts`
- 已重建 `src/router/index.ts`，将路由入口统一回标准路径。
- `main.ts`、`pinia/stores/permission.ts`、`pinia/stores/user.ts` 已全部切回 `@/router` 导入。

### 清理结果

- 前端源码中已无 `lostfound / LostFound / app-router` 的主线引用。
- 一期管理端路由入口已收敛为单一的 `src/router/index.ts`。

### 一期联调验收

- 前端执行 `pnpm build`，构建通过。
- 后端临时启动到 `http://127.0.0.1:8081/api` 后，已使用管理员账号 `admin / admin123` 完成登录验证。
- 已完成以下关键页面接口冒烟：
  - `GET /api/auth/admin/info`
  - `GET /api/admin/statistics/overview`
  - `GET /api/admin/statistics/trend/recent7days`
  - `GET /api/admin/post/pending`
  - `GET /api/admin/post/list`
  - `GET /api/admin/post/{id}`（基于列表首条数据）
  - `GET /api/admin/category/list`
  - `GET /api/category/list?type=POST`
  - `GET /api/admin/comment/list`
  - `GET /api/admin/notice/list`
  - `GET /api/admin/user/list`
  - `GET /api/admin/user/{id}`（基于列表首条数据）

### 验收结论

- 一期管理端当前主线页面已经具备联调条件，首页、帖子、分类、评论、公告、用户页面对应接口均返回成功。
- 控制台在 PowerShell 中查看中文字段时仍存在终端编码显示乱码，但接口调用本身成功，不影响页面实际联调。

## 2026-04-15 一期收尾与二期启动

### 本轮文档收口

- 新增一期正式收尾文档：
  - [phase1_closeout.md](/D:/Downloads/deaf-mute/docs/01-项目规划/phase1_closeout.md)
- 新增二期正式启动文档：
  - [phase2_kickoff.md](/D:/Downloads/deaf-mute/docs/01-项目规划/phase2_kickoff.md)
- 已同步更新：
  - [development_roadmap.md](/D:/Downloads/deaf-mute/docs/01-项目规划/development_roadmap.md)
  - [README.md](/D:/Downloads/deaf-mute/README.md)

### 文档口径调整

- 正式确认一期“内容平台与后台管理”已完成收口
- 将项目下一阶段主线切换为“识别配置后台”
- 清理 README 与路线图中关于一期前端仍未迁移的过时描述

### 当前阶段结论

- 项目已完成一期收尾
- 项目自 2026-04-15 起正式进入第二期开发

## 2026-04-16 管理端品牌文案清理

### 本轮调整

- 清理管理端当前仍在展示的旧品牌文案
- 浏览器标签标题统一切换为“智能手语翻译工具管理端”
- 登录页主标题统一切换为“智能手语翻译工具后台管理系统”
- 同步清理仓库说明与日志中的旧品牌表述，统一改为“旧项目 / 旧业务”语境

### 影响范围

- [adminend/v3-admin-vite-main/v3-admin-vite-main/.env](/D:/Downloads/deaf-mute/adminend/v3-admin-vite-main/v3-admin-vite-main/.env)
- [adminend/v3-admin-vite-main/v3-admin-vite-main/src/pages/login/index.vue](/D:/Downloads/deaf-mute/adminend/v3-admin-vite-main/v3-admin-vite-main/src/pages/login/index.vue)
- [README.md](/D:/Downloads/deaf-mute/README.md)
- [backend/deafmute/README-DEPLOY.md](/D:/Downloads/deaf-mute/backend/deafmute/README-DEPLOY.md)
- [docs/01-项目规划/development_roadmap.md](/D:/Downloads/deaf-mute/docs/01-项目规划/development_roadmap.md)
- [docs/01-项目规划/phase1_closeout.md](/D:/Downloads/deaf-mute/docs/01-项目规划/phase1_closeout.md)
- [docs/04-后端日志/devlog_backend.md](/D:/Downloads/deaf-mute/docs/04-后端日志/devlog_backend.md)

### 验证结果

- 全仓搜索旧品牌关键词已无残留命中
- 管理端重新执行 `pnpm build` 通过
