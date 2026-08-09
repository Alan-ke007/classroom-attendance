<template>
  <view class="page">
    <view class="top-bar">
      <text class="title">行为预警</text>
      <view class="filter-tabs">
        <text class="tab" :class="{ active: filter === 'all' }" @tap="filter = 'all'">全部</text>
        <text class="tab" :class="{ active: filter === 'unhandled' }" @tap="filter = 'unhandled'">未处理</text>
      </view>
    </view>

    <view v-if="filteredList.length === 0" class="empty">
      <text class="empty-icon">✓</text>
      <text class="empty-text">没有行为预警</text>
    </view>

    <view v-for="item in filteredList" :key="item.id" class="b-item">
      <view class="bi-head">
        <text class="bi-type" :class="'bt-'+item.behaviorType">{{ typeMap[item.behaviorType] || item.behaviorType }}</text>
        <text class="bi-time">{{ fmtTime(item.behaviorTime) }}</text>
      </view>
      <view class="bi-body">
        <view class="bi-row">
          <text class="bir-label">学生</text>
          <text class="bir-value">{{ item.studentName || '学生#' + item.studentId }}</text>
        </view>
        <view class="bi-row">
          <text class="bir-label">置信度</text>
          <text class="bir-value">{{ item.confidence ? (item.confidence * 100).toFixed(1) + '%' : '--' }}</text>
        </view>
        <view class="bi-row">
          <text class="bir-label">状态</text>
          <text class="bir-value" :class="item.handled === 1 ? 'c-green' : 'c-red'">
            {{ item.handled === 1 ? '已处理' : '未处理' }}
          </text>
        </view>
      </view>
      <view v-if="item.handled === 0" class="bi-footer">
        <button class="handle-btn" @tap="markHandled(item)">标记已处理</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getBehaviorList, markAsHandled } from '@/api/behavior'

const list = ref([])
const filter = ref('all')

const typeMap = {
  raising_hand: '举手', reading: '阅读', writing: '书写',
  using_phone: '玩手机', bowing_head: '低头', leaning_over: '趴桌',
  sleeping: '睡觉', phone: '玩手机', eating: '吃东西', talking: '讲话'
}

const filteredList = computed(() => {
  return filter.value === 'all' ? list.value : list.value.filter(b => b.handled === 0)
})

function fmtTime(t) { return t ? t.replace('T', ' ').substring(0, 16) : '' }

async function loadData() {
  try {
    const res = await getBehaviorList({ pageNum: 1, pageSize: 200 })
    list.value = res.records || []
  } catch (e) { console.error('加载行为记录失败', e) }
}

async function markHandled(item) {
  const res = await new Promise(r => uni.showModal({
    title: '标记处理',
    content: `确认将此${typeMap[item.behaviorType] || ''}行为标记为已处理？`,
    success: r
  }))
  if (!res.confirm) return
  try {
    await markAsHandled(item.id, '教师已确认')
    uni.showToast({ title: '已处理', icon: 'success' })
    loadData()
  } catch (e) { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

onMounted(loadData)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20rpx; }

.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20rpx 24rpx; background: #fff; border-bottom: 1rpx solid #ebeef5;
}
.title { font-size: 34rpx; font-weight: 600; color: #303133; }
.filter-tabs { display: flex; gap: 12rpx; }
.tab {
  font-size: 26rpx; padding: 8rpx 20rpx; border-radius: 20rpx; color: #909399;
  background: #f5f5f5;
}
.tab.active { background: #4A90D9; color: #fff; }

.empty { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 64rpx; display: block; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: #c0c4cc; }

.b-item {
  background: #fff; margin: 12rpx 20rpx; border-radius: 16rpx; padding: 24rpx;
}
.bi-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.bi-type { font-size: 26rpx; font-weight: 600; padding: 6rpx 16rpx; border-radius: 12rpx; }
.bt-raising_hand, .bt-reading, .bt-writing { background: #ecf5ff; color: #409EFF; }
.bt-using_phone, .bt-phone { background: #fdf6ec; color: #E6A23C; }
.bt-bowing_head, .bt-leaning_over, .bt-sleeping { background: #fef0f0; color: #F56C6C; }
.bt-eating, .bt-talking { background: #f4f4f5; color: #909399; }
.bi-time { font-size: 24rpx; color: #909399; }

.bi-body { margin-bottom: 12rpx; }
.bi-row { display: flex; padding: 6rpx 0; }
.bir-label { width: 100rpx; font-size: 26rpx; color: #909399; flex-shrink: 0; }
.bir-value { font-size: 26rpx; color: #606266; }
.c-green { color: #67C23A; }
.c-red { color: #F56C6C; }

.bi-footer { border-top: 1rpx solid #f5f5f5; padding-top: 16rpx; }
.handle-btn {
  background: #E6A23C; color: #fff; border: none;
  border-radius: 8rpx; padding: 12rpx 32rpx; font-size: 26rpx;
}
</style>
