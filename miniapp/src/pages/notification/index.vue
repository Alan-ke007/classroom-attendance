<template>
  <view class="page">
    <view class="header-bar">
      <text class="title">消息通知</text>
      <text class="mark-all" @tap="handleMarkAllRead">全部已读</text>
    </view>

    <view class="filter-tabs">
      <text class="tab" :class="{ active: activeTab === '' }" @tap="activeTab = ''; loadList(true)">全部</text>
      <text class="tab" :class="{ active: activeTab === 'attendance' }" @tap="activeTab = 'attendance'; loadList(true)">考勤</text>
      <text class="tab" :class="{ active: activeTab === 'behavior' }" @tap="activeTab = 'behavior'; loadList(true)">行为</text>
      <text class="tab" :class="{ active: activeTab === 'leave' }" @tap="activeTab = 'leave'; loadList(true)">请假</text>
      <text class="tab" :class="{ active: activeTab === 'system' }" @tap="activeTab = 'system'; loadList(true)">系统</text>
    </view>

    <view v-if="list.length === 0 && !loading" class="empty">
      <text class="empty-icon">📭</text>
      <text class="empty-text">暂无通知</text>
    </view>

    <view v-for="item in list" :key="item.id" class="n-item" :class="{ unread: item.isRead === 0 }" @tap="handleClick(item)">
      <view class="ni-icon">
        <text>{{ iconMap[item.type] || '🔔' }}</text>
      </view>
      <view class="ni-body">
        <view class="ni-title-row">
          <text class="ni-title">{{ item.title }}</text>
          <view v-if="item.isRead === 0" class="unread-dot"></view>
        </view>
        <text class="ni-content">{{ item.content }}</text>
        <text class="ni-time">{{ item.createTime }}</text>
      </view>
    </view>

    <view v-if="loading" class="load-tip">加载中...</view>
    <view v-if="!hasMore && list.length > 0" class="no-more">— 没有更多了 —</view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { getNotificationList, markRead, markAllRead } from '@/api/notification'

const activeTab = ref('')
const list = ref([])
const pageNum = ref(1)
const hasMore = ref(true)
const loading = ref(false)
const iconMap = { attendance: '📋', behavior: '⚠️', leave: '📝', system: '🔔' }

async function loadList(reset = false) {
  if (loading.value) return
  if (reset) { pageNum.value = 1; list.value = []; hasMore.value = true }
  if (!hasMore.value) return

  loading.value = true
  try {
    const userInfo = uni.getStorageSync('userInfo') || {}
    const reqParams = { pageNum: pageNum.value, pageSize: 20 }
    if (activeTab.value) reqParams.type = activeTab.value
    if (userInfo.userId) reqParams.userId = userInfo.userId
    const res = await getNotificationList(reqParams)
    const records = res.records || []
    if (pageNum.value === 1) list.value = records
    else list.value = [...list.value, ...records]
    hasMore.value = records.length >= 20
    pageNum.value++
  } catch (e) { console.error('加载通知失败', e) }
  finally { loading.value = false }
}

async function handleClick(item) {
  if (item.isRead === 1) return
  try {
    await markRead(item.id)
    item.isRead = 1
  } catch (e) { console.error(e) }
}

async function handleMarkAllRead() {
  const res = await new Promise(r => uni.showModal({
    title: '全部已读', content: '确定将所有通知标记为已读？', success: r
  }))
  if (!res.confirm) return
  try {
    await markAllRead()
    list.value.forEach(n => n.isRead = 1)
    uni.showToast({ title: '已全部标记为已读', icon: 'success' })
  } catch (e) { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

onMounted(() => loadList(true))
onReachBottom(() => loadList())
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }

.header-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20rpx 24rpx; background: #fff; border-bottom: 1rpx solid #ebeef5;
}
.title { font-size: 34rpx; font-weight: 600; color: #303133; }
.mark-all { font-size: 26rpx; color: #4A90D9; }

.filter-tabs {
  display: flex; gap: 8rpx; padding: 16rpx 20rpx; background: #fff;
  border-bottom: 1rpx solid #ebeef5; flex-wrap: wrap;
}
.tab {
  font-size: 24rpx; padding: 8rpx 20rpx; border-radius: 20rpx; color: #909399; background: #f5f5f5;
}
.tab.active { background: #4A90D9; color: #fff; }

.empty { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 64rpx; display: block; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: #c0c4cc; }

.n-item { display: flex; padding: 24rpx; background: #fff; border-bottom: 1rpx solid #f5f5f5; }
.n-item.unread { background: #ecf5ff; }
.ni-icon { margin-right: 16rpx; font-size: 40rpx; width: 56rpx; text-align: center; }
.ni-body { flex: 1; }
.ni-title-row { display: flex; align-items: center; margin-bottom: 8rpx; }
.ni-title { font-size: 28rpx; font-weight: 500; color: #303133; flex: 1; }
.unread-dot { width: 14rpx; height: 14rpx; border-radius: 50%; background: #F56C6C; margin-left: 12rpx; }
.ni-content { font-size: 26rpx; color: #606266; display: block; margin-bottom: 6rpx; }
.ni-time { font-size: 22rpx; color: #909399; display: block; }

.load-tip { text-align: center; padding: 24rpx; color: #909399; font-size: 26rpx; }
.no-more { text-align: center; padding: 24rpx; color: #c0c4cc; font-size: 24rpx; }
</style>
