<script setup>
import { computed, ref } from 'vue'
import AppTabBar from '@/components/AppTabBar.vue'
import { useRecognitionStore } from '@/stores/recognition'

const recognitionStore = useRecognitionStore()
const title = ref('识别结果草稿')
const category = ref('POST')

const content = computed({
  get: () => recognitionStore.publishDraft,
  set: value => recognitionStore.setPublishDraft(value),
})

function useRecognitionResult() {
  recognitionStore.syncSentenceToDraft()
  uni.showToast({ title: '已带入当前识别内容', icon: 'none' })
}

function saveDraft() {
  uni.showToast({ title: '已暂存本地草稿', icon: 'none' })
}
</script>

<template>
  <view class="page">
    <view class="header-card">
      <text class="header-title">发布页首版</text>
      <text class="header-desc">本页先承接“识别结果转帖子”的闭环，后续再对接真实分类、上传和发布接口。</text>
    </view>

    <view class="form-card">
      <text class="field-label">标题</text>
      <input v-model="title" class="field-input" placeholder="请输入帖子标题" />

      <text class="field-label">分类</text>
      <view class="chips">
        <view class="chip" :class="{ active: category === 'POST' }" @click="category = 'POST'">社区帖子</view>
        <view class="chip" :class="{ active: category === 'LEARNING' }" @click="category = 'LEARNING'">学习内容</view>
      </view>

      <text class="field-label">内容</text>
      <textarea v-model="content" class="textarea" placeholder="识别结果或手动输入内容会显示在这里" />

      <view class="btn-row">
        <button class="btn primary" @click="useRecognitionResult">带入最新识别</button>
        <button class="btn" @click="saveDraft">暂存草稿</button>
      </view>
    </view>

    <view class="helper-card">
      <text class="helper-title">当前规划</text>
      <text class="helper-text">下一步会接一期 `post` 相关接口，包括分类拉取、图片上传、正式发布和我的发布。</text>
    </view>

    <AppTabBar current="publish" />
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 36rpx 28rpx 220rpx;
}

.header-card,
.form-card,
.helper-card {
  margin-bottom: 24rpx;
  padding: 28rpx;
  border-radius: 30rpx;
  border: 1rpx solid var(--app-border);
  background: var(--app-card-strong);
  box-shadow: var(--app-shadow);
}

.header-title,
.helper-title {
  display: block;
  margin-bottom: 12rpx;
  font-size: 38rpx;
  font-weight: 600;
}

.header-desc,
.helper-text {
  font-size: 28rpx;
  line-height: 1.65;
  color: var(--app-text-secondary);
}

.field-label {
  display: block;
  margin: 20rpx 0 14rpx;
  font-size: 26rpx;
  color: var(--app-text-secondary);
}

.field-input,
.textarea {
  width: 100%;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.84);
  border: 1rpx solid rgba(167, 184, 221, 0.28);
  color: var(--app-text-primary);
}

.field-input {
  height: 92rpx;
  padding: 0 24rpx;
}

.textarea {
  min-height: 280rpx;
  padding: 22rpx 24rpx;
  line-height: 1.7;
}

.chips {
  display: flex;
  gap: 14rpx;
}

.chip {
  padding: 16rpx 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.76);
  color: var(--app-text-secondary);
  font-size: 24rpx;
}

.chip.active {
  background: rgba(255, 123, 84, 0.14);
  color: var(--app-brand-strong);
}

.btn-row {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}

.btn {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  min-height: 92rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.78);
  color: var(--app-text-primary);
  font-size: 28rpx;
}

.btn.primary {
  background: linear-gradient(135deg, var(--app-brand) 0%, var(--app-brand-strong) 100%);
  color: #fff9f5;
}
</style>
