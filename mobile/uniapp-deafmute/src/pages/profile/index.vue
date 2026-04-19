<script setup>
import { computed } from 'vue'
import AppTabBar from '@/components/AppTabBar.vue'
import { useRecognitionStore } from '@/stores/recognition'

const recognitionStore = useRecognitionStore()

const bridgeModeLabel = computed(() => recognitionStore.bridgeMode === 'blind-webview' ? 'blind web-view bridge' : 'mock')

function openBridgeConfig() {
  uni.navigateTo({ url: '/pages/bridge/index' })
}

async function refreshMyRecords() {
  try {
    await recognitionStore.loadMyRecords()
    uni.showToast({ title: '记录已刷新', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error.message || '刷新失败', icon: 'none' })
  }
}
</script>

<template>
  <view class="page">
    <view class="profile-card">
      <view class="avatar">手语</view>
      <view class="profile-info">
        <text class="profile-name">安卓端联调账号</text>
        <text class="profile-desc">当前优先完成识别消费层联调。这里集中展示桥接配置、识别会话状态，以及登录态用户的识别记录。</text>
      </view>
    </view>

    <view class="info-card">
      <text class="card-title">桥接配置</text>
      <text class="info-line">模式：{{ bridgeModeLabel }}</text>
      <text class="info-line">blind：{{ recognitionStore.bridgeOrigin }}</text>
      <text class="info-line">消费层：{{ recognitionStore.recognitionApiBase }}</text>
      <text class="info-line">bridge 状态：{{ recognitionStore.bridgeStatusText }}</text>
      <text class="info-line">backend 状态：{{ recognitionStore.backendStatusText }}</text>
      <button class="card-btn" @click="openBridgeConfig">打开 bridge 配置页</button>
    </view>

    <view class="info-card">
      <text class="card-title">识别会话</text>
      <text class="info-line">session：{{ recognitionStore.sessionStatusText }}</text>
      <text class="info-line">activeConfigId：{{ recognitionStore.sessionActiveConfigId || '-' }}</text>
      <text class="info-line">当前 traceId：{{ recognitionStore.currentTraceId || '-' }}</text>
      <text class="info-line">当前动作流：{{ recognitionStore.currentFlowCode || '-' }}</text>
      <text class="info-line">当前节点：{{ recognitionStore.currentNodeCode || '-' }}</text>
      <text class="info-line">配置摘要：{{ recognitionStore.configSummaryText }}</text>
    </view>

    <view class="info-card">
      <view class="records-header">
        <text class="card-title">我的识别记录</text>
        <button class="records-btn" @click="refreshMyRecords">刷新</button>
      </view>
      <text class="info-line">token 状态：{{ recognitionStore.authTokenConfigured ? '已配置' : '未配置' }}</text>
      <text v-if="recognitionStore.recordsError" class="info-line">{{ recognitionStore.recordsError }}</text>
      <text v-else class="info-line">共 {{ recognitionStore.recordTotal }} 条，当前展示 {{ recognitionStore.recordList.length }} 条</text>

      <view v-if="recognitionStore.recordList.length" class="record-list">
        <view
          v-for="record in recognitionStore.recordList"
          :key="record.id"
          class="record-item"
        >
          <text class="record-title">{{ record.outputText || record.matchedGestureCode || '未命名结果' }}</text>
          <text class="record-line">flow：{{ record.matchedFlowCode || '-' }}</text>
          <text class="record-line">path：{{ record.matchedNodePath || '-' }}</text>
          <text class="record-line">input：{{ record.inputMode || '-' }}</text>
          <text class="record-line">source：{{ record.source || '-' }}</text>
          <text class="record-line">session：{{ record.sessionId || '-' }}</text>
          <text class="record-line">time：{{ record.createTime || '-' }}</text>
        </view>
      </view>

      <text v-else class="info-line">当前还没有可展示的识别记录。</text>
    </view>

    <AppTabBar current="profile" />
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 36rpx 28rpx 220rpx;
}

.profile-card,
.info-card {
  margin-bottom: 24rpx;
  padding: 28rpx;
  border-radius: 30rpx;
  border: 1rpx solid var(--app-border);
  background: var(--app-card-strong);
  box-shadow: var(--app-shadow);
}

.profile-card {
  display: flex;
  gap: 22rpx;
  align-items: center;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 124rpx;
  height: 124rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, rgba(125, 158, 255, 0.26), rgba(255, 123, 84, 0.22));
  color: var(--app-text-primary);
  font-size: 30rpx;
  font-weight: 600;
}

.profile-info {
  flex: 1;
}

.profile-name,
.card-title {
  display: block;
  margin-bottom: 12rpx;
  font-size: 36rpx;
  font-weight: 600;
}

.profile-desc,
.info-line {
  display: block;
  font-size: 28rpx;
  line-height: 1.65;
  color: var(--app-text-secondary);
}

.card-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 88rpx;
  margin-top: 22rpx;
  border-radius: 24rpx;
  background: rgba(255, 123, 84, 0.12);
  color: var(--app-brand-strong);
  font-size: 28rpx;
}

.records-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.records-btn {
  min-height: 72rpx;
  padding: 0 22rpx;
  border-radius: 20rpx;
  background: rgba(255, 123, 84, 0.12);
  color: var(--app-brand-strong);
  font-size: 24rpx;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 18rpx;
}

.record-item {
  padding: 20rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.7);
}

.record-title,
.record-line {
  display: block;
}

.record-title {
  margin-bottom: 10rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: var(--app-text-primary);
}

.record-line {
  font-size: 24rpx;
  line-height: 1.6;
  color: var(--app-text-secondary);
}
</style>
