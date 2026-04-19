<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { BRIDGE_INPUT_MODES, buildBlindBridgeUrl, normalizeBridgeMessages } from '@/config/bridge'
import {
  CAPTURE_MIGRATION_STAGES,
  CAPTURE_PROVIDERS,
  DEV_HOST_TARGETS,
} from '@/modules/capture/constants'
import {
  buildEndpointPresets,
  collectRuntimePreflight,
  describeCameraPermission,
} from '@/modules/capture/preflight'
import { useRecognitionStore } from '@/stores/recognition'

const recognitionStore = useRecognitionStore()

const bridgeOrigin = ref(recognitionStore.bridgeOrigin)
const recognitionApiBase = ref(recognitionStore.recognitionApiBase)
const authToken = ref(recognitionStore.authToken)
const inputMode = ref(recognitionStore.inputMode)
const reloadKey = ref(0)
const preflight = ref(collectRuntimePreflight())

const bridgeUrl = computed(() => buildBlindBridgeUrl(bridgeOrigin.value, inputMode.value))
const endpointPresets = computed(() => buildEndpointPresets({
  bridgeOrigin: bridgeOrigin.value,
  recognitionApiBase: recognitionApiBase.value,
}))
const cameraPermissionText = computed(() => describeCameraPermission(preflight.value.cameraPermission))

const inputModeOptions = [
  { value: BRIDGE_INPUT_MODES.LANDMARKS, label: 'landmarks direct' },
  { value: BRIDGE_INPUT_MODES.GESTURE_CODE, label: 'gestureCode bridge' },
]

function applyBridgeConfig() {
  recognitionStore.setBridgeOrigin(String(bridgeOrigin.value || '').trim())
  recognitionStore.setRecognitionApiBase(String(recognitionApiBase.value || '').trim())
  recognitionStore.setAuthToken(String(authToken.value || '').trim())
  recognitionStore.setInputMode(inputMode.value)
  recognitionStore.setBridgeMode('blind-webview')
  recognitionStore.setCaptureProvider(CAPTURE_PROVIDERS.WEBVIEW_BRIDGE)
  recognitionStore.setCaptureMigrationStage(CAPTURE_MIGRATION_STAGES.BRIDGE_LIVE)

  bridgeOrigin.value = recognitionStore.bridgeOrigin
  recognitionApiBase.value = recognitionStore.recognitionApiBase
  authToken.value = recognitionStore.authToken
  inputMode.value = recognitionStore.inputMode
  reloadKey.value += 1

  uni.showToast({ title: 'Bridge config saved', icon: 'none' })
}

function selectInputMode(mode) {
  inputMode.value = mode
}

function applyEndpointPreset(target) {
  const preset = endpointPresets.value[target]
  if (!preset) {
    return
  }
  bridgeOrigin.value = preset.bridgeOrigin
  recognitionApiBase.value = preset.recognitionApiBase
}

function reloadBridge() {
  applyBridgeConfig()
}

function refreshPreflight() {
  preflight.value = collectRuntimePreflight()
}

function goHome() {
  uni.reLaunch({ url: '/pages/home/index' })
}

function useMockData() {
  recognitionStore.seedMockRecognition()
  uni.showToast({ title: 'Mock result injected', icon: 'none' })
}

function handleBridgeMessage(event) {
  const messages = normalizeBridgeMessages(event)
  messages.forEach(item => {
    void recognitionStore.applyBridgeMessage(item)
  })
}

function handleWindowMessage(event) {
  if (!event?.data) {
    return
  }
  void recognitionStore.applyBridgeMessage(event.data)
}

async function bootstrapRecognition() {
  try {
    await recognitionStore.ensureBootstrap({ force: true })
    uni.showToast({ title: 'Bootstrap ready', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error.message || 'Bootstrap failed', icon: 'none' })
  }
}

async function startRecognitionRuntime() {
  try {
    await recognitionStore.startRecognitionSession({ force: true })
    uni.showToast({ title: 'Session started', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error.message || 'Start failed', icon: 'none' })
  }
}

async function resetRecognitionRuntime() {
  try {
    await recognitionStore.resetRecognitionSession()
    uni.showToast({ title: 'Session reset', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error.message || 'Reset failed', icon: 'none' })
  }
}

async function closeRecognitionRuntime() {
  try {
    await recognitionStore.closeRecognitionSession()
    uni.showToast({ title: 'Session closed', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error.message || 'Close failed', icon: 'none' })
  }
}

async function loadMyRecords() {
  try {
    await recognitionStore.loadMyRecords()
    uni.showToast({ title: 'Records refreshed', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error.message || 'Load failed', icon: 'none' })
  }
}

onLoad(() => {
  recognitionStore.setBridgeMode('blind-webview')
  recognitionStore.setCaptureProvider(CAPTURE_PROVIDERS.WEBVIEW_BRIDGE)
  recognitionStore.setCaptureMigrationStage(CAPTURE_MIGRATION_STAGES.BRIDGE_LIVE)
  recognitionStore.hydrateLocalConfig()
  refreshPreflight()

  bridgeOrigin.value = recognitionStore.bridgeOrigin
  recognitionApiBase.value = recognitionStore.recognitionApiBase
  authToken.value = recognitionStore.authToken
  inputMode.value = recognitionStore.inputMode
})

onMounted(() => {
  refreshPreflight()
  if (typeof window !== 'undefined') {
    window.addEventListener('message', handleWindowMessage)
  }
})

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('message', handleWindowMessage)
  }
})
</script>

<template>
  <view class="page">
    <view class="section-card">
      <text class="title">Recognition bridge runtime</text>
      <text class="desc">
        This page is only the runtime shell. UI can be replaced later without touching
        the recognition pipeline, bridge protocol, or backend session flow.
      </text>
      <view class="btn-row">
        <button class="btn primary" @click="goHome">Return home</button>
        <button class="btn" @click="refreshPreflight">Refresh preflight</button>
      </view>
    </view>

    <view class="section-card">
      <text class="title compact-title">Device preflight</text>
      <text class="line">capture provider: {{ recognitionStore.captureProviderLabel }}</text>
      <text class="line">migration stage: {{ recognitionStore.captureMigrationLabel }}</text>
      <text class="line">platform: {{ preflight.platform }} / {{ preflight.uniPlatform }}</text>
      <text class="line">device: {{ preflight.deviceType }} / {{ preflight.osName }} {{ preflight.osVersion }}</text>
      <text class="line">camera permission: {{ cameraPermissionText }}</text>
      <text class="line">{{ preflight.hostHint }}</text>
    </view>

    <view class="section-card">
      <text class="title compact-title">Endpoint presets</text>
      <view class="btn-row">
        <button class="btn" @click="applyEndpointPreset(DEV_HOST_TARGETS.LOCALHOST)">localhost</button>
        <button class="btn" @click="applyEndpointPreset(DEV_HOST_TARGETS.EMULATOR)">emulator</button>
        <button class="btn" @click="applyEndpointPreset(DEV_HOST_TARGETS.LAN_TEMPLATE)">LAN template</button>
      </view>
      <text class="hint">{{ endpointPresets[DEV_HOST_TARGETS.LOCALHOST].hint }}</text>
      <text class="hint">{{ endpointPresets[DEV_HOST_TARGETS.EMULATOR].hint }}</text>
      <text class="hint">{{ endpointPresets[DEV_HOST_TARGETS.LAN_TEMPLATE].hint }}</text>
    </view>

    <view class="section-card">
      <text class="field-label">Input mode</text>
      <view class="mode-row">
        <button
          v-for="option in inputModeOptions"
          :key="option.value"
          class="mode-btn"
          :class="{ active: inputMode === option.value }"
          @click="selectInputMode(option.value)"
        >
          {{ option.label }}
        </button>
      </view>

      <text class="field-label">blind origin</text>
      <input v-model="bridgeOrigin" class="field-input" placeholder="http://127.0.0.1:8000" />

      <text class="field-label">Recognition API base</text>
      <input v-model="recognitionApiBase" class="field-input" placeholder="http://127.0.0.1:8080/api" />

      <text class="field-label">User token</text>
      <input
        v-model="authToken"
        class="field-input"
        password
        placeholder="Optional. Used by recognition-record/my."
      />

      <view class="btn-row">
        <button class="btn primary" @click="applyBridgeConfig">Save config</button>
        <button class="btn" @click="reloadBridge">Reload web-view</button>
      </view>

      <text class="hint">bridge URL: {{ bridgeUrl }}</text>
      <text class="hint">input mode: {{ recognitionStore.inputModeLabel }}</text>
      <text class="hint">config summary: {{ recognitionStore.configSummaryText }}</text>
      <text class="hint">landmark contract: {{ recognitionStore.landmarkContractText }}</text>
      <text class="hint">landmark support: {{ recognitionStore.landmarkSupportText }}</text>
      <text class="hint">bridge defaults: camera front / mirrored false / source android-landmarks</text>
      <text class="hint">Android emulator usually needs `10.0.2.2` instead of `127.0.0.1`.</text>
    </view>

    <view class="section-card">
      <text class="title compact-title">Backend tools</text>

      <view class="btn-row">
        <button class="btn primary" @click="bootstrapRecognition">bootstrap</button>
        <button class="btn" @click="startRecognitionRuntime">session/start</button>
      </view>

      <view class="btn-row">
        <button class="btn" @click="resetRecognitionRuntime">session/reset</button>
        <button class="btn" @click="closeRecognitionRuntime">session/close</button>
      </view>

      <view class="btn-row">
        <button class="btn" @click="loadMyRecords">recognition-record/my</button>
        <button class="btn" @click="useMockData">mock result</button>
      </view>
    </view>

    <view class="section-card">
      <text class="title compact-title">Runtime status</text>
      <text class="line">bridge: {{ recognitionStore.bridgeStatusText }}</text>
      <text class="line">backend: {{ recognitionStore.backendStatusText }}</text>
      <text class="line">session: {{ recognitionStore.sessionStatusText }}</text>
      <text class="line">last input: {{ recognitionStore.lastInputKind || '-' }}</text>
      <text class="line">last input at: {{ recognitionStore.lastInputAt || '-' }}</text>
      <text class="line">predict debug: {{ recognitionStore.predictDebugText }}</text>
      <text class="line">input meta: {{ recognitionStore.inputMetaText }}</text>
      <text class="line">handedness supported: {{ recognitionStore.supportsHandedness ? 'yes' : 'no' }}</text>
      <text class="line">mirrored supported: {{ recognitionStore.supportsMirrored ? 'yes' : 'no' }}</text>
      <text class="line">catalog: {{ recognitionStore.gestureCatalog.length }} / {{ recognitionStore.phraseCatalog.length }} / {{ recognitionStore.flowCatalog.length }}</text>
    </view>

    <view class="section-card">
      <text class="title compact-title">Runtime log</text>
      <view class="message-list">
        <text
          v-for="item in recognitionStore.recentMessages"
          :key="item"
          class="message-item"
        >
          {{ item }}
        </text>
      </view>
    </view>

    <view class="webview-card">
      <web-view :key="reloadKey" :src="bridgeUrl" @message="handleBridgeMessage" />
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 36rpx 28rpx 40rpx;
}

.section-card,
.webview-card {
  margin-bottom: 24rpx;
  padding: 28rpx;
  border-radius: 30rpx;
  border: 1rpx solid var(--app-border);
  background: var(--app-card-strong);
  box-shadow: var(--app-shadow);
}

.title {
  display: block;
  margin-bottom: 12rpx;
  font-size: 36rpx;
  font-weight: 600;
}

.compact-title {
  margin-bottom: 18rpx;
}

.desc,
.hint,
.line {
  display: block;
  font-size: 26rpx;
  line-height: 1.7;
  color: var(--app-text-secondary);
}

.field-label {
  display: block;
  margin: 18rpx 0 12rpx;
  font-size: 26rpx;
  color: var(--app-text-secondary);
}

.field-input {
  width: 100%;
  height: 92rpx;
  padding: 0 24rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.82);
  border: 1rpx solid rgba(167, 184, 221, 0.28);
}

.mode-row,
.btn-row {
  display: flex;
  gap: 16rpx;
  margin: 18rpx 0;
}

.mode-btn,
.btn {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  min-height: 88rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.78);
  font-size: 26rpx;
  color: var(--app-text-primary);
}

.mode-btn.active,
.btn.primary {
  background: linear-gradient(135deg, var(--app-brand) 0%, var(--app-brand-strong) 100%);
  color: #fff9f5;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.message-item {
  padding: 18rpx 20rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.72);
  color: var(--app-text-secondary);
  font-size: 24rpx;
  line-height: 1.5;
}

.webview-card {
  min-height: 960rpx;
  overflow: hidden;
}
</style>
