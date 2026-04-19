export const CAPTURE_PROVIDERS = {
  WEBVIEW_BRIDGE: 'webviewBridge',
  UNI_CAMERA: 'uniCamera',
  NATIVE_PLUGIN: 'nativePlugin',
}

export const CAPTURE_PROVIDER_LABELS = {
  [CAPTURE_PROVIDERS.WEBVIEW_BRIDGE]: 'web-view landmarks bridge',
  [CAPTURE_PROVIDERS.UNI_CAMERA]: 'uni-app camera provider',
  [CAPTURE_PROVIDERS.NATIVE_PLUGIN]: 'native camera plugin',
}

export const CAPTURE_MIGRATION_STAGES = {
  BRIDGE_LIVE: 'bridgeLive',
  UNI_CAMERA_POC: 'uniCameraPoc',
  NATIVE_STREAM: 'nativeStream',
}

export const CAPTURE_MIGRATION_LABELS = {
  [CAPTURE_MIGRATION_STAGES.BRIDGE_LIVE]: 'web-view landmarks live',
  [CAPTURE_MIGRATION_STAGES.UNI_CAMERA_POC]: 'uni camera proof of concept',
  [CAPTURE_MIGRATION_STAGES.NATIVE_STREAM]: 'native stream integration',
}

export const DEV_HOST_TARGETS = {
  LOCALHOST: 'localhost',
  EMULATOR: 'emulator',
  LAN_TEMPLATE: 'lanTemplate',
}

export const LAN_TEMPLATE_HOST = '192.168.0.100'
