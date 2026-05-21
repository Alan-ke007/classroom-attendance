<template>
  <view class="page">
    <view class="filter-bar">
      <picker mode="selector" :range="statusOpts" range-key="label" @change="onStatusChange">
        <view class="filter-chip">{{ statusOpts[statusIdx].label }} ▼</view>
      </picker>
      <view class="filter-chip" @tap="showDatePicker = true">📅</view>
    </view>

    <view class="stats-row">
      <view class="sr-item sr-blue"><text class="srn">{{ summary.total }}</text><text class="srl">总考勤</text></view>
      <view class="sr-item sr-green"><text class="srn">{{ summary.present }}</text><text class="srl">出勤</text></view>
      <view class="sr-item sr-orange"><text class="srn">{{ summary.late }}</text><text class="srl">迟到</text></view>
      <view class="sr-item sr-red"><text class="srn">{{ summary.absent }}</text><text class="srl">缺勤</text></view>
    </view>

    <view v-if="list.length === 0" class="empty">暂无考勤记录</view>
    <view v-for="item in list" :key="item.id" class="att-item">
      <view class="ai-left">
        <view class="ai-dot" :class="'dot-'+item.status"></view>
        <view class="ai-info">
          <text class="ai-course">{{ item.courseName || '未知课程' }}</text>
          <text class="ai-date">{{ item.attendanceDate }} {{ fmtTime(item.checkInTime) }}</text>
        </view>
      </view>
      <text class="ai-tag" :class="'tag-'+item.status">{{ statusMap[item.status] || item.status }}</text>
    </view>

    <view v-if="hasMore" class="load-more" @tap="loadMore">加载更多</view>
    <view v-else class="no-more">— 没有更多了 —</view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAttendanceList } from '@/api/attendance'

const statusOpts = [
  { label: '全部', value: '' },
  { label: '出勤', value: 'present' },
  { label: '迟到', value: 'late' },
  { label: '缺勤', value: 'absent' },
  { label: '请假', value: 'leave' },
]
const statusMap = { present: '出勤', late: '迟到', absent: '缺勤', leave: '请假' }
const statusIdx = ref(0)
const showDatePicker = ref(false)
const list = ref([])
const pageNum = ref(1)
const hasMore = ref(true)

const summary = computed(() => {
  const all = list.value
  return {
    total: all.length,
    present: all.filter(a => a.status === 'present').length,
    late: all.filter(a => a.status === 'late').length,
    absent: all.filter(a => a.status === 'absent').length,
  }
})

function fmtTime(t) { return t ? t.substring(0, 16).replace('T', ' ') : '' }

function onStatusChange(e) {
  statusIdx.value = e.detail.value
  pageNum.value = 1
  list.value = []
  loadData()
}

async function loadData() {
  try {
    const status = statusOpts[statusIdx.value].value
    const res = await getAttendanceList({
      pageNum: pageNum.value,
      pageSize: 20,
      ...(status ? { status } : {})
    })
    const records = res.records || []
    if (pageNum.value === 1) list.value = records
    else list.value = [...list.value, ...records]
    hasMore.value = records.length >= 20
  } catch (e) { console.error('加载考勤失败', e) }
}

function loadMore() {
  pageNum.value++
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20rpx; }

.filter-bar { display: flex; padding: 16rpx 20rpx; gap: 12rpx; background: #fff; }
.filter-chip {
  background: #f5f5f5; padding: 10rpx 24rpx;
  border-radius: 30rpx; font-size: 26rpx; color: #606266;
}

.stats-row { display: flex; margin: 16rpx 20rpx; gap: 12rpx; }
.sr-item { flex: 1; background: #fff; border-radius: 12rpx; padding: 20rpx 8rpx; text-align: center; }
.srn { font-size: 32rpx; font-weight: 700; display: block; color: #303133; }
.srl { font-size: 22rpx; color: #909399; }
.sr-blue { border-bottom: 4rpx solid #409EFF; }
.sr-green { border-bottom: 4rpx solid #67C23A; }
.sr-orange { border-bottom: 4rpx solid #E6A23C; }
.sr-red { border-bottom: 4rpx solid #F56C6C; }

.empty { text-align: center; padding: 120rpx 0; color: #c0c4cc; font-size: 28rpx; }

.att-item {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; margin: 0 20rpx 8rpx; padding: 24rpx;
  border-radius: 12rpx;
}
.ai-left { display: flex; align-items: center; }
.ai-dot { width: 16rpx; height: 16rpx; border-radius: 50%; margin-right: 16rpx; }
.dot-present { background: #67C23A; }
.dot-late { background: #E6A23C; }
.dot-absent { background: #F56C6C; }
.dot-leave { background: #909399; }
.ai-course { font-size: 28rpx; font-weight: 500; color: #303133; display: block; }
.ai-date { font-size: 24rpx; color: #909399; }
.ai-tag { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx; }
.tag-present { background: #f0f9eb; color: #67C23A; }
.tag-late { background: #fdf6ec; color: #E6A23C; }
.tag-absent { background: #fef0f0; color: #F56C6C; }
.tag-leave { background: #f4f4f5; color: #909399; }

.load-more { text-align: center; padding: 24rpx; color: #4A90D9; font-size: 26rpx; }
.no-more { text-align: center; padding: 24rpx; color: #c0c4cc; font-size: 24rpx; }
</style>
