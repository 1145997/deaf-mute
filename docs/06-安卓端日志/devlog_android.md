# 安卓端开发日志

日期：2026-04-16

## 记录说明

本文件用于记录当前项目在“安卓 uni-app 客户端”方向上的实际推进情况，重点保留：

- 页面结构规划
- 识别桥接验证过程
- 与后端接口联调情况
- 安卓端演示版收口记录

---

## 2026-04-16 瀹夊崜绔?gestureCode 鑱旇皟鏈€灏忛摼璺惤鍦?

### 1. uni-app 璇嗗埆娑堣垂灞傛帴鍏ュ畬鎴?

鏈疆宸插皢 Spring Boot 棣栨壒璇嗗埆娑堣垂灞傛帴鍙ｅ疄闄呭鎺ュ埌 uni-app 宸ョ▼锛屾柊澧炲苟钀藉湴锛?

- [mobile/uniapp-deafmute/src/config/recognition.js](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/config/recognition.js)
- [mobile/uniapp-deafmute/src/services/recognition.js](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/services/recognition.js)
- [mobile/uniapp-deafmute/src/stores/recognition.js](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/stores/recognition.js)

褰撳墠瀹夊崜绔凡鎺ラ€氱殑璋冪敤椤哄簭涓猴細

1. `GET /api/recognition/bootstrap`
2. `POST /api/recognition/session/start`
3. `POST /api/recognition/predict`锛?`gestureCode` 妯″紡锛?
4. `POST /api/recognition/session/reset`
5. `POST /api/recognition/session/close`
6. `GET /api/recognition-record/my`

### 2. 妗ユ帴绛栫暐璋冩暣

鏈疆宸插皢鍘熸潵鈥滃墠绔湰鍦板彞瀛愭嫾鎺モ€濈殑閫昏緫璋冩暣涓衡€滀互鍚庣杩斿洖涓哄噯鈥濓紝鍘熷洜鏄細

- 鍔ㄤ綔娴侀攣瀹氳妭鐐瑰拰鏈€缁堣緭鍑哄湪璇箟涓婁笉鏄竴鍥炰簨
- 濡傛灉缁х画鐢ㄦ湰鍦扮‖缂栫爜鍒ゅ畾锛屼細鎶婇攣瀹氳妭鐐瑰綋鎴愭渶缁堣瘝鏉″爢鍏ュ彞瀛?
- 鐜板湪鍙ユ爣缂撳啿鍙湪鍚庣杩斿洖鏈夋晥 `outputType` 鏃舵墠姝ｅ紡鍏ュ彞

H5 `blind` 椤典粛鏄?gestureCode` 婧愬ご锛屼絾鍙ュ瓙鍜岀姸鎬佷互 Spring Boot 杩斿洖缁撴灉涓轰富銆?

### 3. 椤甸潰绾у姛鑳借ˉ榻?

鏈疆宸叉洿鏂颁笁涓叧閿〉闈細

- [home](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/pages/home/index.vue)
  棣栭〉鐜板湪鍙洿鎺ユ墽琛?bootstrap / reset / close / 鏌ョ湅璁板綍` 锛屽苟灞曠ず session 鍜岄厤缃憳瑕併€?
- [bridge](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/pages/bridge/index.vue)
  鏂板 `blind` 鍦板潃銆?Spring Boot API 鍦板潃銆佺敤鎴?token` 閰嶇疆锛屽悓鏃舵彁渚?bootstrap / session/start / session/close / recognition-record/my` 鎸夐挳銆?
- [profile](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/pages/profile/index.vue)
  鏂板鈥滄垜鐨勮瘑鍒褰曗€濆睍绀猴紝鐢ㄤ簬鎵胯浇 `recognition-record/my` 鐨勭粨鏋溿€?

### 4. 鏋勫缓楠岃瘉

鏈疆宸插湪 [mobile/uniapp-deafmute](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute) 涓嬪畬鎴愶細

- `npm run build`

缁撴灉锛?

- H5 鏋勫缓缁х画閫氳繃
- 鏂板璇嗗埆娑堣垂灞傛帴鍏ュ悗鏈紩鍏ラ潤鎬佺紪璇戦敊璇?

### 5. 杩愯鏃堕獙璇佺姸鎬?

鏈疆灏濊瘯鍦ㄦ湰鏈哄啀娆℃媺璧?Spring Boot 鏈嶅姟鍋氬疄闄呭啋鐑燂紝缁撴灉鏄細

- 鏈湴 `8080` 褰撴椂骞舵湭鏈夎瘑鍒秷璐瑰眰鏈嶅姟鍦ㄨ繍琛?
- 灏濊瘯鐩存帴鍚姩 [deafmute-0.0.1-SNAPSHOT.jar](/D:/Downloads/deaf-mute/backend/deafmute/target/deafmute-0.0.1-SNAPSHOT.jar) 鏃跺け璐?
- 澶辫触鍘熷洜宸茬‘璁や负锛氭湰鏈?Java = 1.8`锛岃€屽悗绔墦鍖呬骇鐗╅渶瑕?Java 17`锛坈lass file version 61锛?

鏃ュ織鏂囦欢锛?

- [android-link.err.log](/D:/Downloads/deaf-mute/backend/deafmute/target/android-link.err.log)

杩欒鏄庯細

- 瀹夊崜绔唬鐮佸眰闈㈢殑鎺ュ彛瀵规帴宸插畬鎴?
- 褰撳墠鏈湴娌℃湁瀹屾垚鈥滈噸璧峰悗绔湇鍔?+ 鐪熷疄 web-view 鍔ㄦ€佸洖娴佲€濋獙璇侊紝闃荤鐐规槸杩愯鐜鐨?Java 鐗堟湰鑰屼笉鏄?uni-app 浠ｇ爜

## 2026-04-16 安卓端启动记录

### 1. 当前项目基线确认

已确认当前仓库状态：

- 一期内容平台后端已收口
- 一期管理端已完成联调
- 二期第一批、第二批识别配置后台已落地
- 当前仓库尚未建立独立的 uni-app 安卓端工程

### 2. 参考原型确认

已确认安卓端首页规划参考来源为：

- [blind/src/static/index.html](/D:/Downloads/deaf-mute/blind/src/static/index.html)
- 用户提供的首页效果图

从参考原型中抽取出的核心能力包括：

- 摄像头预览
- 识别状态展示
- 句子缓冲
- 删除 / 清空 / 重置
- 自动 TTS 播报

### 3. 关键技术判断

本轮已明确一个关键前提：

- `blind` 当前是浏览器侧 MediaPipe JS Demo
- 该方案不能直接等价视为 uni-app 安卓端原生页面能力
- 安卓端需要优先验证 `web-view / H5 bridge` 识别桥接方案

### 4. 本轮文档产出

已新增安卓端文档目录：

- [docs/06-安卓端日志](/D:/Downloads/deaf-mute/docs/06-安卓端日志)

已新增计划文档：

- [uniapp_android_plan.md](/D:/Downloads/deaf-mute/docs/06-安卓端日志/uniapp_android_plan.md)

### 5. 当前计划结论

安卓端第一阶段不直接追求完整识别效果，而是优先完成：

- uni-app 工程初始化
- 4 Tab 页面骨架
- 首页视觉壳子
- 识别桥接预案验证

### 6. 下一步建议

建议下一轮按以下顺序执行：

1. 新建 uni-app 安卓端工程
2. 落首页静态布局与底部导航
3. 准备识别桥接 PoC
4. 再接发布页与后端一期接口

---

## 2026-04-16 blind -> uni-app 代码级桥接验证

### 1. `blind` 当前识别链路确认

已确认 `blind` 当前链路为：

- 浏览器端 MediaPipe 采集手部 landmarks
- 前端调用 `POST /predict`
- FastAPI 返回 `gesture / code / label / locked / state`
- 页面侧负责句子缓冲、删除、清空、自动 TTS

也就是说，`blind` 当前并不是“前端纯识别”，而是“前端采集 + Python 后端判定 + 前端结果组织”模式。

### 2. Spring Boot 后端现状确认

已查看当前主后端代码，确认现阶段仍属于“识别配置后台”，当前公开能力主要是：

- 基础手势列表
- 短语模板列表
- 当前生效识别配置
- 动作流管理后台

当前尚未发现以下能力：

- `/predict`
- 识别会话接口
- landmarks 提交接口
- 识别记录接口

结论：

- 安卓端第一轮桥接不能直接接 Spring Boot 主后端做识别
- 第一轮只能先接 `blind` FastAPI 原型，或做前端 mock 适配层

### 3. 后端配置数据结构判断

已确认 Spring Boot 后端已为后续识别消费层准备好基础配置模型：

- `gesture_library`
  - 关键字段：`gestureCode / gestureName / detectionKey`
- `recognition_config`
  - 关键字段：`confidenceMin / holdMs / debounceMs / cooldownMs / requiredHits / maxIntervalMs / lockTimeoutMs / gestureOrderJson`
- `phrase_template`
  - 关键字段：`phraseCode / phraseText / ttsText / sceneType`
- `gesture_flow`
  - 节点侧已支持手势节点阈值配置
  - 输出侧已支持 `outputType / outputText / phraseTemplateId / controlAction / ttsText / displayText`

这说明当前主后端已经具备“规则存储层”，但还没有“规则执行层”。

### 4. uni-app 模板选择结论

本轮已确定安卓端模板选择为：

- 基础模板：`uni-app Vue3/Vite + TypeScript` 官方 CLI 模板
- 状态管理：`Pinia`
- 组件方案：在 CLI 工程中按需引入 `uni-ui`

不直接选择整套 `uni ui项目模板` 起步的原因：

- 当前仓库主前端已采用 `Vue3 + TypeScript + Pinia`
- 本地已具备 Node 与 pnpm 环境，适合走 CLI 工程路线
- 安卓端需要较多自定义识别桥接与业务壳层逻辑，CLI 工程更利于代码管理

### 5. 桥接方案结论

当前建议的第一轮桥接方案为：

- uni-app 页面负责移动端外壳、页面状态、发布承接
- `web-view` 内嵌 `blind` 识别页
- 识别结果通过消息桥接回 uni-app 页面
- Spring Boot 当前只作为配置查询和社区业务后端，不承担第一轮实时识别

### 6. 当前遗留项

本轮完成的是“代码级桥接验证”和“模板选型”，尚未完成：

- uni-app 工程实际初始化
- `web-view` 消息桥接 PoC
- 安卓端运行时联调

### 7. 下一步建议

建议下一轮直接执行：

1. 新建 `mobile/uniapp-deafmute` 工程
2. 按 CLI `Vue3/Vite + TypeScript` 模板初始化
3. 先做首页壳子
4. 再做 `blind` 页面嵌入与消息桥接 PoC

---

## 2026-04-16 安卓端工程骨架与桥接页初始化

### 1. 工程初始化结果

已在仓库中新增安卓端工程：

- [mobile/uniapp-deafmute](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute)

创建方式：

- 使用 `npm create uni@latest mobile/uniapp-deafmute -- --template vite-ts`

说明：

- 生成器拉下的是轻量基础模板
- 后续已按当前项目需求手动补齐页面结构与状态层

### 2. 本轮安卓端代码落地

已完成首轮工程改造：

- 新增 4 个主页面
  - [home](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/pages/home/index.vue)
  - [learn](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/pages/learn/index.vue)
  - [publish](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/pages/publish/index.vue)
  - [profile](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/pages/profile/index.vue)
- 新增 bridge 验证页
  - [bridge](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/pages/bridge/index.vue)
- 新增底部导航组件
  - [AppTabBar.vue](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/components/AppTabBar.vue)
- 新增识别状态 store
  - [recognition.js](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/stores/recognition.js)
- 新增 bridge 配置与消息规范
  - [bridge.js](/D:/Downloads/deaf-mute/mobile/uniapp-deafmute/src/config/bridge.js)

### 3. 依赖与构建验证

本轮已完成：

- `npm install`
- `npm run build`

结果：

- 工程依赖已安装完成
- H5 构建已通过

说明：

- `npm install` 过程中存在模板上游旧依赖与漏洞告警
- 当前属于模板依赖层问题，不影响本轮工程骨架与构建通过

### 4. 模板兼容处理

本轮已发现并处理一个模板兼容点：

- 生成器默认 `Vue` 版本与 `Pinia 3` 不兼容
- 已将 `Pinia` 调整为兼容当前模板的 `2.1.7`

### 5. 与后端的同步事项

已新增给后端的对接需求单：

- [后端+安卓端识别消费层接口需求.md](/D:/Downloads/deaf-mute/docs/交流/后端+安卓端识别消费层接口需求.md)

当前结论：

- 安卓端工程骨架已具备继续做 bridge PoC 的基础
- 在 Spring Boot 消费层接口落地前，bridge 页仍先接 `blind` FastAPI 原型

### 6. bridge 消息协议补充

本轮已继续补齐 bridge PoC 的消息链路：

- `blind` 识别页已增加向宿主页面发送识别消息的逻辑
- uni-app bridge 页已同时兼容：
  - `web-view @message`
  - H5 预览下的 `window.postMessage`

当前约定的消息类型为：

- `bridge-status`
- `gesture-predict`
- `bridge-error`

### 7. 当前阶段结论

截至本轮：

- 安卓端 4 页面骨架已落地
- bridge 页已具备消息接收入口
- `blind` 页已具备消息发送入口
- uni-app 工程安装依赖并完成构建通过

下一步可直接进入：

1. 实机或模拟器下的 bridge 联调
2. 将首页假状态逐步替换为真实 bridge 回流数据
3. 对接一期 `post` 发布接口

---

## 2026-04-16 blind 服务与 bridge 运行时验证

### 1. `blind` 原型服务启动验证

本轮已实际启动 `blind/src` 下的 FastAPI 服务，并确认：

- `http://127.0.0.1:8000/health` 返回成功
- `GET /gesture-library` 返回当前手势顺序
- `GET /text-library` 返回当前词库
- `GET /settings` 返回当前识别参数

说明：

- 当前 `blind` 运行端口已固定在 `8000`
- 后续 bridge 页默认地址继续指向 `http://127.0.0.1:8000`

### 2. `/predict` 冒烟验证

本轮使用构造 landmarks 对 `/predict` 做了实际请求验证，已确认：

- `is_v_sign` 连续 3 帧后可稳定返回
  - `gesture = is_v_sign`
  - `code = chr_h`
  - `label = 我画的`
- `is_four_sign -> is_v_sign` 锁定链可返回
  - `gesture = is_v_sign`
  - `code = chr_c`
  - `label = 请再改一下`

### 3. 原型逻辑修复

本轮发现并修复一个原型层问题：

- `is_four_sign` 进入锁定时，原先会返回 `locked=true`，但 `code / label` 为空
- 已在 [hand_role.py](/D:/Downloads/deaf-mute/blind/src/core/hand_role.py) 中补上单动作解析回退

修复后结果：

- `is_four_sign` 连续 3 帧后返回
  - `gesture = is_four_sign`
  - `code = chr_b`
  - `label = 好的`
  - `locked = true`

这使得移动端首页在锁定触发瞬间也能拿到可展示的语义结果。

### 4. bridge 消息补充验证

本轮已继续增强 [blind/src/static/index.html](/D:/Downloads/deaf-mute/blind/src/static/index.html)：

- 页面加载时主动发送 `bridge-status`
- 识别成功时发送 `gesture-predict`
- 运行异常时发送 `bridge-error`
- 同时兼容：
  - `window.parent.postMessage`
  - `window.uni.postMessage`

### 5. uni-app H5 开发服务验证

本轮已实际启动：

- `npm run dev`

并确认：

- `http://127.0.0.1:5173/` 返回 `HTTP 200`
- uni-app H5 开发服务可正常拉起

同时已处理一个基础兼容问题：

- 生成器默认 `vue 3.4.21`
- 与当前 `@dcloudio/uni-app` 运行时不完全兼容
- 已升级为：
  - `vue 3.5.13`
  - `@vue/runtime-core 3.5.13`

### 6. 工程收尾清理

已清理 uni-app 模板默认残留且不再使用的文件：

- `src/pages/index.vue`
- `src/components/AppFooter.vue`
- `src/components/AppLogos.vue`
- `src/components/InputEntry.vue`

### 7. 当前阶段结论

截至本轮：

- `blind` 服务已可真实运行
- `/predict` 已完成实际冒烟
- 锁定触发手势返回口径已修复
- uni-app H5 开发服务已可访问
- 安卓端工程已达到“可继续做 bridge 联调”的状态

下一步建议继续：

1. 在 bridge 页中接真实 `blind` 回流消息做页面联调
2. 实机或模拟器验证 `web-view` 对本地服务地址的可达性
3. 开始接一期发布接口
