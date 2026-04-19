# CameraX Recognition Demo

原生 Android 单页面 Demo，目标是打通：

- CameraX 相机预览与持续取帧
- MediaPipe Hand Landmarker 单手 21 点提取
- `GET /recognition/bootstrap`
- `POST /recognition/session/start`
- `POST /recognition/predict`
- 本地句子缓冲 / 倒计时 / TTS
- `POST /recognition/session/close`

## 运行前提

- Android Studio Hedgehog 及以上
- JDK 17
- 本地后端可达，例如：
  - 模拟器：`http://10.0.2.2:8080/api/`
  - 真机：改成宿主机局域网 IP

## 当前默认约定

- `clientType = android`
- `sceneCode = bridge`
- `engineType = mediapipe`
- `source = android-landmarks`
- landmarks 固定按 MediaPipe Hands 21 点顺序上传

## 目录说明

- `app/src/main/java/.../camera`
  CameraX 预览和帧分析
- `app/src/main/java/.../mediapipe`
  Hand Landmarker 初始化和 landmarks 映射
- `app/src/main/java/.../network/api`
  Retrofit 接口
- `app/src/main/java/.../repository`
  网络仓储与本地设置
- `app/src/main/java/.../domain`
  句子缓冲、控制动作、倒计时常量
- `app/src/main/java/.../tts`
  Android TextToSpeech 封装
- `app/src/main/java/.../ui`
  `ViewModel` 与 `StateFlow` UI 状态

