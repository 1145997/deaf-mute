# 文档索引

更新时间：2026-04-17

`docs` 目录按“项目规划 / 协议接口 / 公用 / 后端日志 / 管理端日志 / 安卓端日志 / 交流”归档，后续增量文档继续按这个结构追加。

---

## 项目速览

- `backend/deafmute`
  Spring Boot 后端主工程，承接一期内容平台和二期识别配置、识别消费层。
- `adminend/v3-admin-vite-main/v3-admin-vite-main`
  管理端前端工程，负责帖子、分类、公告、识别配置和动作流页面。
- `mobile/uniapp-deafmute`
  安卓 uni-app 客户端工程，当前已进入识别桥接与 landmarks 直传联调阶段。
- `blind`
  识别原型参考项目，二期部分识别规则和动作流设计从这里抽象迁移。
- `sql/init.sql`
  当前真实可执行的数据库初始化脚本，应与后端现状保持一致。

---

## 01-项目规划

- [development_roadmap.md](/D:/Downloads/deaf-mute/docs/01-项目规划/development_roadmap.md)
  项目整体排期、阶段目标与推进建议。
- [phase1_closeout.md](/D:/Downloads/deaf-mute/docs/01-项目规划/phase1_closeout.md)
  一期“内容平台 + 管理后台”正式收口文档。
- [phase2_kickoff.md](/D:/Downloads/deaf-mute/docs/01-项目规划/phase2_kickoff.md)
  二期正式启动文档，定义识别配置后台建设目标。
- [project_status_gap_2026-04-18.md](/D:/Downloads/deaf-mute/docs/01-项目规划/project_status_gap_2026-04-18.md)
  当前项目进度、目标差距与下一步建议。

## 02-协议接口

- [api_phase1.md](/D:/Downloads/deaf-mute/docs/02-协议接口/api_phase1.md)
  一期后端真实可用接口说明。
- [backend_api_dto_vo_checklist.md](/D:/Downloads/deaf-mute/docs/02-协议接口/backend_api_dto_vo_checklist.md)
  后端接口、DTO、VO 设计清单。
- [api_full_manual_2026-04-18.md](/D:/Downloads/deaf-mute/docs/02-协议接口/api_full_manual_2026-04-18.md)
  当前后端全量接口手册，覆盖认证、内容平台、识别配置与识别消费层。

## 03-公用

- [deafmute_schema_draft.sql](/D:/Downloads/deaf-mute/docs/03-公用/deafmute_schema_draft.sql)
  完整数据库设计草案。

## 04-后端日志

- [devlog_backend.md](/D:/Downloads/deaf-mute/docs/04-后端日志/devlog_backend.md)
  后端主线开发日志，记录一期收口、二期批次落地、识别消费层和 landmarks 联调补充。

## 05-管理端日志

- [devlog.md](/D:/Downloads/deaf-mute/docs/05-管理端日志/devlog.md)
  早期管理端迁移与一期联调记录。
- [phase2_batch1_admin_acceptance.md](/D:/Downloads/deaf-mute/docs/05-管理端日志/phase2_batch1_admin_acceptance.md)
  二期第一批管理端验收记录。
- [phase2_batch2_admin_acceptance.md](/D:/Downloads/deaf-mute/docs/05-管理端日志/phase2_batch2_admin_acceptance.md)
  二期第二批动作流管理页验收记录。

## 06-安卓端日志

- [uniapp_android_plan.md](/D:/Downloads/deaf-mute/docs/06-安卓端日志/uniapp_android_plan.md)
  安卓 uni-app 客户端开发计划。
- [devlog_android.md](/D:/Downloads/deaf-mute/docs/06-安卓端日志/devlog_android.md)
  安卓端开发日志。
- [landmarks_refactor_2026-04-17.md](/D:/Downloads/deaf-mute/docs/06-安卓端日志/landmarks_refactor_2026-04-17.md)
  安卓端本轮“功能层解耦 + landmarks 直传推进”记录。

## 交流

- [后端+安卓端识别消费层接口需求.md](/D:/Downloads/deaf-mute/docs/交流/后端+安卓端识别消费层接口需求.md)
  安卓端发给后端的首批识别消费层接口需求。
- [给安卓端+识别消费层首批接口可联调说明.md](/D:/Downloads/deaf-mute/docs/交流/给安卓端+识别消费层首批接口可联调说明.md)
  后端发给安卓端的首批联调说明。
- [给后端+landmarks直传联调补充需求.md](/D:/Downloads/deaf-mute/docs/交流/给后端+landmarks直传联调补充需求.md)
  安卓端发给后端的 landmarks 直传补充需求。
- [给安卓端+landmarks直传联调口径补充说明.md](/D:/Downloads/deaf-mute/docs/交流/给安卓端+landmarks直传联调口径补充说明.md)
  后端发给安卓端的 landmarks 契约与调试字段补充说明。

交流目录命名约定：

- 格式：`给谁+主题`
- 示例：
  - `给安卓端+识别消费层首批接口可联调说明.md`
  - `给后端+landmarks直传联调补充需求.md`

---

## 推荐阅读顺序

如果是新加入项目，建议按下面顺序阅读：

1. [development_roadmap.md](/D:/Downloads/deaf-mute/docs/01-项目规划/development_roadmap.md)
2. [phase1_closeout.md](/D:/Downloads/deaf-mute/docs/01-项目规划/phase1_closeout.md)
3. [phase2_kickoff.md](/D:/Downloads/deaf-mute/docs/01-项目规划/phase2_kickoff.md)
4. [api_phase1.md](/D:/Downloads/deaf-mute/docs/02-协议接口/api_phase1.md)
5. [backend_api_dto_vo_checklist.md](/D:/Downloads/deaf-mute/docs/02-协议接口/backend_api_dto_vo_checklist.md)
6. [devlog_backend.md](/D:/Downloads/deaf-mute/docs/04-后端日志/devlog_backend.md)
7. [phase2_batch1_admin_acceptance.md](/D:/Downloads/deaf-mute/docs/05-管理端日志/phase2_batch1_admin_acceptance.md)
8. [phase2_batch2_admin_acceptance.md](/D:/Downloads/deaf-mute/docs/05-管理端日志/phase2_batch2_admin_acceptance.md)
9. [uniapp_android_plan.md](/D:/Downloads/deaf-mute/docs/06-安卓端日志/uniapp_android_plan.md)
10. [devlog_android.md](/D:/Downloads/deaf-mute/docs/06-安卓端日志/devlog_android.md)
11. [landmarks_refactor_2026-04-17.md](/D:/Downloads/deaf-mute/docs/06-安卓端日志/landmarks_refactor_2026-04-17.md)
12. [给安卓端+landmarks直传联调口径补充说明.md](/D:/Downloads/deaf-mute/docs/交流/给安卓端+landmarks直传联调口径补充说明.md)

---

## 当前阶段状态

- 管理端一期已完成
- 后端一期已收口
- 二期第一批和第二批前后端已完成
- 安卓端已进入 landmarks 直传联调阶段
- 当前主线是继续收紧识别引擎消费层、handedness/mirror 规则和客户端联调闭环
## 2026-04-17 补充

- [native_capture_prep_2026-04-17.md](/D:/Downloads/deaf-mute/docs/06-安卓端日志/native_capture_prep_2026-04-17.md)
  安卓端为原生摄像头采集迁移做的代码与运行时准备记录。
