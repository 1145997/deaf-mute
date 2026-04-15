# 智能手语翻译工具

基于原“校园失物招领系统”二次开发的双创项目后端重构版本。当前仓库已经完成第一期后端骨架落地，主业务语义已从 `lostfound` 迁移到 `post`，并保留了登录、公告、评论、分类、用户管理、文件上传、统计等平台能力。

## 当前状态

当前已完成：
- 后端一期骨架可编译、可启动
- 数据库初始化脚本已收敛为第一期可执行版本
- 新增 `post` 模块并打通发布、列表、详情、我的发布、审核
- `comment` 已切换到 `postId` 语义
- `category` 已升级为通用分类，支持 `POST` / `LEARNING`
- `statistics` 已切到社区一期统计口径
- `post_image` 已接入帖子发布、修改、详情
- 旧 `lostfound` 控制器已默认下线
- 管理端一期页面已完成迁移、清理与联调验收

当前未完成：
- 学习模块、识别配置模块、规则引擎接口尚未正式编码
- 识别相关表已在草案中，但第一期初始化脚本未启用

## 技术栈

- 后端：Spring Boot 3.5.13
- ORM：MyBatis-Plus 3.5.14
- 数据库：MySQL 8.0
- 认证：JWT
- 文件存储：MinIO
- 接口风格：RESTful API
- 管理端：V3 Admin Vite

## 目录说明

```text
deaf-mute/
├─ backend/deafmute/                # Spring Boot 后端
├─ adminend/                        # 管理端前端
├─ blind/                           # 识别原型参考项目
├─ sql/init.sql                     # 第一期数据库初始化脚本
├─ docs/deafmute_schema_draft.sql   # 完整数据库设计草案
├─ docs/backend_api_dto_vo_checklist.md
├─ docs/api_phase1.md               # 当前一期接口文档
└─ kill_8080.bat                    # 清理 8080 端口占用
```

## 运行要求

- JDK 17+，本地已验证 `JDK 21`
- Maven Wrapper
- MySQL 8.0
- MinIO

说明：
- `pom.xml` 目标版本是 Java 17
- 本地实际编译和启动验证使用的是 JDK 21

## 开发环境配置

开发环境数据源在：

- [application-dev.yml](/D:/Downloads/deaf-mute/backend/deafmute/src/main/resources/application-dev.yml)

当前默认配置：
- 数据库名：`deafmute`
- 用户名：`root`
- 密码：`root`
- 服务上下文：`/api`
- 默认端口：`8080`

## 数据库初始化

执行下面这份脚本：

- [init.sql](/D:/Downloads/deaf-mute/sql/init.sql)

脚本内容覆盖第一期已启用模块：
- `user`
- `admin`
- `category`
- `post`
- `post_image`
- `comment`
- `notice`
- `operation_log`

如果你需要完整识别模块设计，请看：

- [deafmute_schema_draft.sql](/D:/Downloads/deaf-mute/docs/deafmute_schema_draft.sql)

## 启动后端

建议先确认 JDK：

```powershell
java -version
```

如果本机默认不是 JDK 17+，可以临时指定：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
$env:Path='C:\Program Files\Java\jdk-21\bin;' + $env:Path
```

编译：

```powershell
cd backend\deafmute
.\mvnw.cmd -q -DskipTests compile
```

启动：

```powershell
.\mvnw.cmd -DskipTests spring-boot:run
```

打包：

```powershell
.\mvnw.cmd -q -DskipTests package
```

## 端口占用处理

仓库已提供：

- [kill_8080.bat](/D:/Downloads/deaf-mute/kill_8080.bat)

如果 8080 被旧 Java 进程占用，先执行它，再启动后端。

## 已验证结果

本地已完成这些验证：
- `.\mvnw.cmd -q -DskipTests compile` 成功
- `.\mvnw.cmd -q -DskipTests package` 成功
- 使用 jar 在 `8081` 端口短启动成功
- `http://127.0.0.1:8081/api/actuator` 返回 `HTTP 200`

## 当前可用接口范围

一期当前主要可用模块：
- 管理员认证
- 用户登录
- 分类管理
- 帖子管理
- 评论管理
- 公告管理
- 用户管理
- 文件上传
- 仪表盘统计

详细接口见：

- [api_phase1.md](/D:/Downloads/deaf-mute/docs/api_phase1.md)
- [development_roadmap.md](/D:/Downloads/deaf-mute/docs/development_roadmap.md)
- [phase1_closeout.md](/D:/Downloads/deaf-mute/docs/phase1_closeout.md)
- [phase2_kickoff.md](/D:/Downloads/deaf-mute/docs/phase2_kickoff.md)

## 关于旧 lostfound 模块

旧 `lostfound` 代码仍保留在仓库中，便于迁移对照，但默认不会暴露接口。

当前处理方式：
- `LostFoundController`
- `AdminLostFoundController`

均已加上：

```java
@Profile("legacy-lostfound")
```

也就是说，默认启动时：
- `/api/lostfound/**` 不注册
- `/api/admin/lostfound/**` 不注册

只有显式启用 `legacy-lostfound` profile 时才会重新生效。

## 当前阶段

截至 2026-04-15：
- 第一期“内容平台与后台管理”已完成收口
- 项目正式进入第二期“识别配置后台”建设

## 下一步建议

推荐按这个顺序继续推进：

1. 抽取并固化第二期识别配置数据库表
2. 落地识别配置后台后端模块
3. 落地管理端二期菜单、页面和 API
4. 准备默认手势与演示规则初始化数据

## 文档归档

当前文档已按“总览 / 后端 / 管理端 / 阶段记录”整理，统一入口见：

- [docs/README.md](/D:/Downloads/deaf-mute/docs/README.md)

重点文档：

- 后端开发日志：[devlog_backend.md](/D:/Downloads/deaf-mute/docs/devlog_backend.md)
- 管理端二期第一批验收：[phase2_batch1_admin_acceptance.md](/D:/Downloads/deaf-mute/docs/phase2_batch1_admin_acceptance.md)
