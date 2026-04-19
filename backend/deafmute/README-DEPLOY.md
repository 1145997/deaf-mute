
---

# 智能手语翻译工具部署文档

## 1. 项目简介

本项目当前已迁移为“双创项目：智能手语翻译工具”，采用前后端分离架构，当前主要包含以下三个部分：

* **微信小程序端**：用户登录、发布寻物/招领信息、浏览列表、评论留言、查看公告
* **后台管理端**：管理员登录、信息审核、用户管理、评论管理、公告管理、分类管理、数据统计
* **后端服务**：提供 RESTful API，负责业务处理、数据存储、文件上传、身份认证

---

## 2. 技术栈

### 前端

* 微信小程序原生开发
* V3 Admin Vite 5.0

### 后端

* Spring Boot 3.5.x
* MyBatis-Plus
* JWT
* MySQL 8.0
* MinIO

---

## 3. 运行环境要求

### 3.1 软件环境

* JDK 17
* Maven 3.8+
* MySQL 8.0
* MinIO
* Node.js 18+（后台前端 V3 Admin Vite）
* 微信开发者工具（小程序端调试）

### 3.2 服务器环境

推荐 Linux 服务器，最低配置建议：

* CPU：2 核
* 内存：2 GB
* 磁盘：20 GB
* 操作系统：Ubuntu 20.04+/CentOS 7+

---

## 4. 项目结构说明

### 后端项目

```text
campuslostfound
├── src/main/java/dev/forint/campuslostfound
│   ├── common
│   ├── config
│   ├── modules
│   │   ├── admin
│   │   ├── auth
│   │   ├── category
│   │   ├── comment
│   │   ├── lostfound
│   │   ├── notice
│   │   ├── statistics
│   │   └── user
│   └── CampuslostfoundApplication.java
├── src/main/resources
│   ├── application.yml
│   └── application-dev.yml
└── pom.xml
```

---

## 5. 数据库部署

### 5.1 创建数据库

在 MySQL 中创建数据库：

```sql
CREATE DATABASE campus_lost_found DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 5.2 导入初始化脚本

执行项目提供的 SQL 初始化脚本，创建以下核心表：

* `admin`
* `user`
* `category`
* `lost_found`
* `comment`
* `notice`
* `claim_record`
* `operation_log`

### 5.3 初始化管理员账号

默认管理员账号示例：

```text
用户名：admin
密码：admin123
```

正式部署时建议修改默认密码。

---

## 6. MinIO 部署

### 6.1 启动 MinIO

以 Linux 为例：

```bash
./minio server /data/minio --console-address ":9001"
```

### 6.2 默认访问地址

* API 地址：`http://服务器IP:9000`
* 控制台地址：`http://服务器IP:9001`

### 6.3 创建 Bucket

登录 MinIO 控制台后，新建 Bucket：

```text
campus-lost-found
```

### 6.4 设置访问策略

开发阶段可将 Bucket 设置为公开读，用于图片直接访问。
生产环境建议结合业务权限控制进行更安全的访问配置。

---

## 7. 后端配置说明

## 7.1 `application.yml`

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: campus-lost-found
  profiles:
    active: dev
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: dev.forint.deafmutes.*.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto

jwt:
  secret: your-very-strong-secret-key-change-this-at-least-32-bytes
  expiration: 86400000
  header: Authorization
  token-prefix: Bearer 

minio:
  endpoint: http://服务器IP:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: campus-lost-found
  read-path: http://服务器IP:9000/campus-lost-found

wechat:
  miniapp:
    appid: 你的小程序AppID
    secret: 你的小程序AppSecret

logging:
  level:
    root: info
    dev.forint.deafmute: debug
```

## 7.2 `application-dev.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/campus_lost_found?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

根据实际部署环境修改：

* MySQL 地址
* 用户名密码
* MinIO 地址
* 微信小程序 appid/secret

---

## 8. 后端项目部署

### 8.1 本地运行

在项目根目录执行：

```bash
mvn clean package
```

打包成功后生成：

```text
target/campus-lost-found-0.0.1-SNAPSHOT.jar
```

运行命令：

```bash
java -jar target/campus-lost-found-0.0.1-SNAPSHOT.jar
```

### 8.2 后台运行

Linux 服务器可使用：

```bash
nohup java -jar campus-lost-found-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

查看日志：

```bash
tail -f app.log
```

---

## 9. 后台管理端部署

### 9.1 安装依赖

进入后台前端项目目录：

```bash
npm install
```

### 9.2 配置接口地址

在前端环境配置中，将后端 API 地址改为：

```text
http://服务器IP:8080/api
```

### 9.3 启动开发环境

```bash
npm run dev
```

### 9.4 构建生产环境

```bash
npm run build
```

构建完成后会生成 `dist` 目录，可部署到 Nginx。

---

## 10. 微信小程序端部署

### 10.1 修改接口地址

在小程序配置文件中设置：

```js
const BASE_URL = "http://服务器IP:8080/api"
```

### 10.2 配置登录模式

开发阶段可使用 mock 登录，正式阶段使用真实微信登录：

```js
const USE_MOCK_LOGIN = false
```

### 10.3 微信真实登录流程

小程序端调用：

```js
wx.login({
  success(res) {
    if (res.code) {
      wx.request({
        url: BASE_URL + "/auth/user/wx-login",
        method: "POST",
        data: {
          code: res.code,
          nickname: "小程序用户",
          avatar: ""
        }
      })
    }
  }
})
```

### 10.4 真机调试

使用微信开发者工具或真机进行测试。

---

## 11. Nginx 部署建议

可通过 Nginx 统一代理后台前端和后端接口。

### 示例配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /www/admin/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

## 12. 系统访问说明

### 后端接口地址

```text
http://服务器IP:8080/api
```

### 后台管理端地址

```text
http://服务器IP/
```

### MinIO 控制台地址

```text
http://服务器IP:9001
```

---

## 13. 主要功能接口说明

### 管理员认证

* `POST /api/auth/admin/login`
* `GET /api/auth/admin/info`
* `POST /api/auth/admin/logout`

### 小程序用户认证

* `POST /api/auth/user/login`（开发 mock 登录）
* `POST /api/auth/user/wx-login`（真实微信登录）

### 信息管理

* `POST /api/lostfound`
* `GET /api/lostfound/list`
* `GET /api/lostfound/{id}`
* `GET /api/lostfound/my`
* `PUT /api/lostfound/{id}`
* `DELETE /api/lostfound/{id}`

### 后台审核

* `GET /api/admin/lostfound/pending`
* `GET /api/admin/lostfound/list`
* `PUT /api/admin/lostfound/{id}/approve`
* `PUT /api/admin/lostfound/{id}/reject`

### 分类管理

* `GET /api/category/list`
* `GET /api/admin/category/list`
* `POST /api/admin/category`
* `PUT /api/admin/category/{id}`
* `DELETE /api/admin/category/{id}`

### 评论管理

* `GET /api/comment/list`
* `POST /api/comment`
* `GET /api/admin/comment/list`
* `PUT /api/admin/comment/{id}/hide`
* `PUT /api/admin/comment/{id}/show`
* `DELETE /api/admin/comment/{id}`

### 用户管理

* `GET /api/admin/user/list`
* `GET /api/admin/user/{id}`
* `PUT /api/admin/user/{id}/status`

### 公告管理

* `GET /api/notice/public/list`
* `GET /api/notice/public/{id}`
* `GET /api/admin/notice/list`
* `POST /api/admin/notice`
* `PUT /api/admin/notice/{id}`
* `DELETE /api/admin/notice/{id}`

### 统计接口

* `GET /api/admin/statistics/overview`
* `GET /api/admin/statistics/trend/recent7days`

---

## 14. 常见问题排查

### 14.1 后端启动成功但接口 401/403

检查：

* 是否带了正确的 JWT Token
* 管理员接口是否使用管理员 token
* 用户接口是否使用用户 token

### 14.2 图片上传成功但无法访问

检查：

* MinIO Bucket 是否存在
* Bucket 是否设置公开读
* `read-path` 是否配置正确

### 14.3 微信登录失败

检查：

* `appid` 和 `secret` 是否正确
* 小程序端 `wx.login()` 获取的 `code` 是否有效
* 后端是否能正常访问微信 `jscode2session` 接口

### 14.4 数据库连接失败

检查：

* MySQL 是否启动
* 数据库名是否为 `campus_lost_found`
* 用户名密码是否正确
* 端口是否开放

### 14.5 分类删除失败

可能原因：

* 该分类已被失物招领信息使用
* 存在外键约束或业务保护逻辑

---

## 15. 生产环境建议

* 修改默认管理员密码
* JWT 密钥使用高强度随机字符串
* 不在前端暴露 `AppSecret`
* 数据库定期备份
* MinIO 文件定期备份
* 使用 Nginx 反向代理
* 后续可加入 HTTPS

---

## 16. 部署完成后的验证流程

部署完成后建议依次验证：

1. 后端接口是否能正常访问
2. 管理员登录是否正常
3. 后台仪表盘统计是否正常显示
4. 小程序用户登录是否正常
5. 图片上传是否可访问
6. 发布信息是否能成功写入数据库
7. 审核流程是否正常
8. 公告、评论、分类、用户管理是否正常使用

---
