<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import AppTabBar from '@/components/AppTabBar.vue'
import { buildBlindPreviewUrl, normalizeBridgeMessages } from '@/config/bridge'
import {
  CAPTURE_MIGRATION_STAGES,
  CAPTURE_PROVIDERS,
} from '@/modules/capture/constants'
import { RECOGNITION_INPUT_MODES } from '@/modules/recognition/constants'
import { useRecognitionStore } from '@/stores/recognition'

const recognitionStore = useRecognitionStore()

const previewKey = ref(0)
const inlinePreviewAvailable = ref(false)

const stateLabel = computed(() => (recognitionStore.locked ? 'locked' : recognitionStore.stateText))
const inlinePreviewUrl = computed(() => buildBlindPreviewUrl(
  recognitionStore.bridgeOrigin,
  recognitionStore.inputMode,
))
const liveSummary = computed(() => {
  if (recognitionStore.currentCode) {
    return `${recognitionStore.currentCode} / ${recognitionStore.currentLabel || recognitionStore.currentDisplayText || '-'}`
  }
  return recognitionStore.currentGesture
})
const heroStatusText = computed(() => (
  inlinePreviewAvailable.value
    ? 'Inline H5 preview is active in the camera slot.'
    : 'App runtime uses the bridge page because web-view is page-level there.'
))

function ensureHomeLiveMode(mode = RECOGNITION_INPUT_MODES.LANDMARKS) {
  recognitionStore.setInputMode(mode)
  recognitionStore.setBridgeMode('blind-webview')
  recognitionStore.setCaptureProvider(CAPTURE_PROVIDERS.WEBVIEW_BRIDGE)
  recognitionStore.setCaptureMigrationStage(CAPTURE_MIGRATION_STAGES.BRIDGE_LIVE)
}

function reloadInlinePreview(mode = recognitionStore.inputMode) {
  ensureHomeLiveMode(mode)
  previewKey.value += 1
}

function openBridgeRuntime(mode = RECOGNITION_INPUT_MODES.LANDMARKS) {
  ensureHomeLiveMode(mode)
  if (inlinePreviewAvailable.value) {
    previewKey.value += 1
    uni.showToast({ title: 'Inline camera preview reloaded', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/bridge/index' })
}

function openRuntimeConsole() {
  ensureHomeLiveMode(recognitionStore.inputMode)
  uni.navigateTo({ url: '/pages/bridge/index' })
}

function pushToPublish() {
  recognitionStore.syncSentenceToDraft()
  uni.reLaunch({ url: '/pages/publish/index' })
}

function previewRecognition() {
  recognitionStore.seedMockRecognition()
  uni.showToast({ title: 'Mock result injected', icon: 'none' })
}

async function syncBootstrap() {
  try {
    await recognitionStore.ensureBootstrap({ force: true })
    uni.showToast({ title: 'Bootstrap synced', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error.message || 'Bootstrap failed', icon: 'none' })
  }
}

async function resetRecognitionSession() {
  try {
    await recognitionStore.resetRecognitionSession()
    uni.showToast({ title: 'Session reset', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error.message || 'Reset failed', icon: 'none' })
  }
}

async function closeRecognitionSession() {
  try {
    await recognitionStore.closeRecognitionSession()
    uni.showToast({ title: 'Session closed', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error.message || 'Close failed', icon: 'none' })
  }
}

async function refreshMyRecords() {
  try {
    await recognitionStore.loadMyRecords()
    uni.reLaunch({ url: '/pages/profile/index' })
  } catch (error) {
    uni.showToast({ title: error.message || 'Load failed', icon: 'none' })
  }
}

function handleWindowMessage(event) {
  const messages = normalizeBridgeMessages(event?.data ?? event)
  messages.forEach(item => {
    void recognitionStore.applyBridgeMessage(item)
  })
}

onShow(() => {
  ensureHomeLiveMode(recognitionStore.inputMode)
  void recognitionStore.ensureBootstrap().catch(() => {})
  void recognitionStore.ensureRecognitionSession().catch(() => {})
})

onMounted(() => {
  inlinePreviewAvailable.value = typeof window !== 'undefined' && !window.plus
  recognitionStore.hydrateLocalConfig()
  ensureHomeLiveMode(recognitionStore.inputMode)
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
    <view class="hero-glow hero-glow-left"></view>
    <view class="hero-glow hero-glow-right"></view>

    <view class="status-bar">
      <text class="status-time">5:20</text>
      <view class="status-pill">
        <view class="status-dot"></view>
        <text>{{ recognitionStore.locationName }}</text>
      </view>
    </view>

    <view class="hero-card">
      <view class="camera-orbit">
        <view class="camera-core">
          <!-- #ifdef H5 -->
          <iframe
            v-if="inlinePreviewAvailable"
            :key="previewKey"
            class="camera-preview-frame"
            :src="inlinePreviewUrl"
            allow="camera; microphone; autoplay"
          />
          <!-- #endif -->
          <text v-else class="camera-hint">{{ recognitionStore.currentGesture }}</text>
        </view>
      </view>
      <text class="guide-text">Point the camera at a gesture</text>
      <text class="bridge-text">{{ heroStatusText }}</text>
      <text class="bridge-text">{{ recognitionStore.bridgeStatusText }}</text>
      <text class="bridge-text">{{ recognitionStore.backendStatusText }}</text>
      <text class="bridge-text">{{ recognitionStore.captureProviderLabel }} / {{ recognitionStore.inputModeLabel }}</text>
    </view>

    <view class="result-card">
      <view class="result-header">
        <text class="result-title">Live recognition</text>
        <view class="state-chip">
          <text>{{ stateLabel }}</text>
        </view>
      </view>

      <text class="result-content">{{ recognitionStore.displayText }}</text>
      <text class="result-sub">{{ liveSummary }}</text>

      <view class="result-meta">
        <view class="meta-block">
          <text class="meta-label">Session</text>
          <text class="meta-value">{{ recognitionStore.sessionId || '-' }}</text>
        </view>
        <view class="meta-block">
          <text class="meta-label">Last update</text>
          <text class="meta-value">{{ recognitionStore.lastUpdatedAt || '-' }}</text>
        </view>
      </view>

      <view class="result-meta">
        <view class="meta-block">
          <text class="meta-label">Recognition config</text>
          <text class="meta-value">{{ recognitionStore.configSummaryText }}</text>
        </view>
        <view class="meta-block">
          <text class="meta-label">Capture migration</text>
          <text class="meta-value">{{ recognitionStore.captureMigrationLabel }}</text>
        </view>
      </view>
    </view>

    <view class="toolbar-card">
      <button class="action-btn primary" @click="openBridgeRuntime()">Start landmarks live</button>
      <button class="action-btn warm" @click="openBridgeRuntime(RECOGNITION_INPUT_MODES.GESTURE_CODE)">Fallback gestureCode</button>
      <button class="action-btn muted" @click="reloadInlinePreview()">Reload preview</button>
      <button class="action-btn muted" @click="openRuntimeConsole">Runtime console</button>
      <button class="action-btn muted" @click="pushToPublish">Send to publish</button>
      <button class="action-btn muted" @click="syncBootstrap">Refresh bootstrap</button>
      <button class="action-btn muted" @click="resetRecognitionSession">Reset session</button>
      <button class="action-btn muted" @click="refreshMyRecords">My records</button>
      <button class="action-btn muted" @click="closeRecognitionSession">Close session</button>
      <button class="action-btn muted full" @click="previewRecognition">Inject mock result</button>
    </view>

    <view class="panel-card">
      <view class="panel-header">
        <text class="panel-title">Home feedback loop</text>
        <text class="panel-tag">live debug</text>
      </view>
      <text class="message-item">{{ recognitionStore.predictDebugText }}</text>
      <text class="message-item">{{ recognitionStore.inputMetaText }}</text>
      <text class="message-item">{{ recognitionStore.landmarkContractText }}</text>
      <text class="message-item">{{ recognitionStore.landmarkSupportText }}</text>
    </view>

    <view class="panel-card">
      <view class="panel-header">
        <text class="panel-title">Sentence buffer</text>
        <text class="panel-tag">{{ recognitionStore.sentenceBuffer.length }} token(s)</text>
      </view>
      <text class="sentence-text">{{ recognitionStore.sentenceText || 'Confirmed outputs will accumulate here and can be pushed into publish.' }}</text>
    </view>

    <view class="panel-card compact">
      <view class="panel-header">
        <text class="panel-title">Runtime log</text>
        <text class="panel-tag">bridge log</text>
      </view>
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

    <AppTabBar current="home" />
  </view>
</template>

<style scoped lang="scss">
.page {
  position: relative;
  min-height: 100vh;
  padding: 28rpx 28rpx 220rpx;
  overflow: hidden;
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(40rpx);
  opacity: 0.7;
}

.hero-glow-left {
  top: 80rpx;
  left: -60rpx;
  width: 240rpx;
  height: 240rpx;
  background: rgba(125, 158, 255, 0.34);
}

.hero-glow-right {
  top: 280rpx;
  right: -80rpx;
  width: 280rpx;
  height: 280rpx;
  background: rgba(255, 198, 186, 0.4);
}

.status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 48rpx;
}

.status-time {
  font-size: 38rpx;
  font-weight: 600;
  color: rgba(15, 22, 38, 0.82);
}

.status-pill {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 16rpx 24rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.72);
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 12rpx 24rpx rgba(143, 157, 193, 0.12);
}

.status-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: var(--app-brand-strong);
  box-shadow: 0 0 0 8rpx rgba(255, 123, 84, 0.12);
}

.hero-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 28rpx;
}

.camera-orbit {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 280rpx;
  height: 280rpx;
  border: 3rpx solid rgba(219, 210, 255, 0.72);
  border-radius: 50%;
  background: radial-gradient(circle at top, rgba(244, 221, 255, 0.55), rgba(194, 214, 255, 0.18));
  box-shadow:
    inset 0 0 0 12rpx rgba(255, 255, 255, 0.26),
    0 20rpx 40rpx rgba(157, 166, 214, 0.18);
}

.camera-core {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 220rpx;
  height: 220rpx;
  overflow: hidden;
  border-radius: 50%;
  background:
    linear-gradient(180deg, rgba(235, 242, 255, 0.96), rgba(192, 214, 255, 0.42)),
    rgba(255, 255, 255, 0.82);
  border: 2rpx solid rgba(255, 255, 255, 0.65);
  text-align: center;
}

.camera-preview-frame {
  width: 100%;
  height: 100%;
  border: none;
  background: #0d111c;
}

.camera-hint {
  width: 160rpx;
  font-size: 26rpx;
  line-height: 1.5;
  color: var(--app-text-secondary);
}

.guide-text {
  margin-top: 20rpx;
  font-size: 32rpx;
  font-weight: 500;
}

.bridge-text {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: var(--app-text-secondary);
  text-align: center;
}

.result-card,
.toolbar-card,
.panel-card {
  position: relative;
  margin-bottom: 24rpx;
  padding: 28rpx;
  border: 1rpx solid var(--app-border);
  border-radius: 32rpx;
  background: var(--app-card);
  box-shadow: var(--app-shadow);
  backdrop-filter: blur(20rpx);
}

.result-header,
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.result-title,
.panel-title {
  font-size: 34rpx;
  font-weight: 600;
}

.state-chip,
.panel-tag {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.84);
  color: var(--app-text-secondary);
  font-size: 22rpx;
}

.result-content {
  display: block;
  min-height: 100rpx;
  font-size: 40rpx;
  line-height: 1.45;
  color: var(--app-text-primary);
}

.result-sub {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: var(--app-text-secondary);
}

.result-meta {
  display: flex;
  gap: 18rpx;
  margin-top: 22rpx;
}

.meta-block {
  flex: 1;
  padding: 18rpx 20rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.72);
}

.meta-label {
  display: block;
  margin-bottom: 8rpx;
  font-size: 22rpx;
  color: var(--app-text-tertiary);
}

.meta-value {
  font-size: 26rpx;
  color: var(--app-text-primary);
}

.toolbar-card {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 92rpx;
  border-radius: 24rpx;
  font-size: 28rpx;
  color: var(--app-text-primary);
  background: rgba(255, 255, 255, 0.72);
}

.action-btn.primary {
  background: linear-gradient(135deg, var(--app-brand) 0%, var(--app-brand-strong) 100%);
  color: #fffaf6;
}

.action-btn.warm {
  background: linear-gradient(135deg, rgba(255, 202, 174, 0.84), rgba(255, 240, 196, 0.84));
}

.action-btn.muted {
  color: var(--app-text-secondary);
}

.action-btn.full {
  grid-column: 1 / -1;
}

.sentence-text {
  font-size: 30rpx;
  line-height: 1.7;
  color: var(--app-text-secondary);
}

.compact {
  padding-bottom: 24rpx;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.message-item {
  display: block;
  padding: 18rpx 20rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.72);
  color: var(--app-text-secondary);
  font-size: 24rpx;
  line-height: 1.5;
}
</style>
