<script setup>
const props = defineProps({
  current: {
    type: String,
    default: 'home',
  },
})

const tabs = [
  { key: 'home', label: '首页', hint: '识别', url: '/pages/home/index' },
  { key: 'learn', label: '学习', hint: '手语', url: '/pages/learn/index' },
  { key: 'publish', label: '发布', hint: '发布', url: '/pages/publish/index', center: true },
  { key: 'profile', label: '个人', hint: '我的', url: '/pages/profile/index' },
]

function switchPage(tab) {
  if (tab.key === props.current) {
    return
  }
  uni.reLaunch({ url: tab.url })
}
</script>

<template>
  <view class="tabbar-shell">
    <view class="tabbar-card">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tabbar-item"
        :class="[{ active: tab.key === current }, { center: tab.center }]"
        @click="switchPage(tab)"
      >
        <view class="tabbar-icon">{{ tab.hint }}</view>
        <text class="tabbar-label">{{ tab.label }}</text>
      </button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.tabbar-shell {
  position: sticky;
  bottom: 20rpx;
  padding: 0 12rpx calc(env(safe-area-inset-bottom) + 4rpx);
}

.tabbar-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 18rpx 28rpx;
  border: 1rpx solid var(--app-border);
  border-radius: 38rpx;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: var(--app-shadow);
  backdrop-filter: blur(18rpx);
}

.tabbar-item {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  color: var(--app-text-secondary);
}

.tabbar-item.center {
  margin-top: -44rpx;
}

.tabbar-item.center .tabbar-icon {
  width: 108rpx;
  height: 108rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--app-brand) 0%, var(--app-brand-strong) 100%);
  color: #fff7f1;
  box-shadow: 0 20rpx 30rpx rgba(255, 123, 84, 0.28);
}

.tabbar-item.active .tabbar-label {
  color: var(--app-text-primary);
  font-weight: 600;
}

.tabbar-item.active:not(.center) .tabbar-icon {
  border-color: rgba(255, 123, 84, 0.4);
  background: rgba(255, 123, 84, 0.12);
  color: var(--app-brand-strong);
}

.tabbar-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 68rpx;
  height: 68rpx;
  border: 1rpx solid rgba(156, 169, 204, 0.28);
  border-radius: 22rpx;
  background: rgba(248, 250, 255, 0.92);
  font-size: 22rpx;
  letter-spacing: 2rpx;
}

.tabbar-label {
  font-size: 24rpx;
}
</style>
