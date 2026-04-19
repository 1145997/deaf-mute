<script setup>
import AppTabBar from '@/components/AppTabBar.vue'

const lessons = [
  { title: '基础礼貌语', text: '你好、谢谢、请帮帮我', tag: '答辩演示优先' },
  { title: '社区发布常用语', text: '我画的、这个角色、请看看', tag: '与 blind 词库对齐' },
  { title: '控制手势说明', text: '删除、取消、重置等控制动作说明', tag: '待接识别消费层' },
]
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">Learning</text>
      <text class="title">学习页首版</text>
      <text class="desc">当前先用静态学习卡片承接演示，后续再接正式学习模块与识别配置数据。</text>
    </view>

    <view v-for="item in lessons" :key="item.title" class="lesson-card">
      <view class="tag">{{ item.tag }}</view>
      <text class="lesson-title">{{ item.title }}</text>
      <text class="lesson-text">{{ item.text }}</text>
    </view>

    <view class="tip-card">
      <text class="tip-title">当前策略</text>
      <text class="tip-text">学习页先保证可演示和风格统一，不阻塞首页识别桥接。后续可直接接学习文章、分类和基础手势示例。</text>
    </view>

    <AppTabBar current="learn" />
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 36rpx 28rpx 220rpx;
}

.hero,
.lesson-card,
.tip-card {
  margin-bottom: 24rpx;
  padding: 28rpx;
  border-radius: 30rpx;
  background: var(--app-card-strong);
  border: 1rpx solid var(--app-border);
  box-shadow: var(--app-shadow);
}

.eyebrow {
  display: inline-block;
  margin-bottom: 16rpx;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(125, 158, 255, 0.14);
  color: #5266c7;
  font-size: 22rpx;
  letter-spacing: 2rpx;
}

.title {
  display: block;
  margin-bottom: 12rpx;
  font-size: 42rpx;
  font-weight: 600;
}

.desc,
.lesson-text,
.tip-text {
  font-size: 28rpx;
  line-height: 1.65;
  color: var(--app-text-secondary);
}

.tag {
  display: inline-block;
  margin-bottom: 18rpx;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 123, 84, 0.12);
  color: var(--app-brand-strong);
  font-size: 22rpx;
}

.lesson-title,
.tip-title {
  display: block;
  margin-bottom: 12rpx;
  font-size: 34rpx;
  font-weight: 600;
}
</style>
