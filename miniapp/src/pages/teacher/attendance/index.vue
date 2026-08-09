<template>
  <view class="page">
    <view class="top-bar">
      <text class="title">考勤管理</text>
      <picker mode="selector" :range="courseNames" @change="onCourseChange">
        <view class="course-pick">{{ selectedCourseName || '全部课程' }} ▼</view>
      </picker>
    </view>

    <view class="filter-row">
      <picker mode="date" @change="e => { filterDate = e.detail.value; loadData() }">
        <view class="date-pick">{{ filterDate || '选择日期' }} ▼</view>
      </picker>
    </view>

    <view class="stats-row">
      <view class="stat-card"><text class="sc-num">{{ summary.total }}</text><text class="sc-label">总人数</text></view>
      <view class="stat-card c-green"><text class="sc-num">{{ summary.present }}</text><text class="sc-label">出勤</text></view>
      <view class="stat-card c-orange"><text class="sc-num">{{ summary.late }}</text><text class="sc-label">迟到</text></view>
      <view class="stat-card c-red"><text class="sc-num">{{ summary.absent }}</text><text class="sc-label">缺勤/请假</text></view>
    </view>

    <view v-if="list.length === 0" class="empty">暂无考勤记录</view>
    <view v-for="item in list" :key="item.id" class="att-item">
      <view class="ai-left">
        <text class="ai-name">{{ item.studentName || '学生#' + item.studentId }}</text>
        <text class="ai-date">{{ item.attendanceDate }}</text>
      </view>
      <view class="ai-center">
        <text class="ai-status" :class="'as-'+item.status">{{ statusMap[item.status] || item.status }}</text>
      </view>
      <text class="ai-time" v-if="item.checkInTime">{{ fmtTime(item.checkInTime) }}</text>
    </view>

    <view class="bottom-bar">
      <button class="refresh-btn" @tap="refreshData">刷新数据</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAttendanceList } from '@/api/attendance'
import { getCourseList } from '@/api/course'

const list = ref([])
const courses = ref([])
const courseNames = ref([])
const selectedCourseId = ref('')
const selectedCourseName = ref('')
const filterDate = ref('')

const statusMap = { present: '出勤', late: '迟到', absent: '缺勤', leave: '请假' }

const summary = computed(() => {
  const items = list.value
  return {
    total: items.length,
    present: items.filter(a => a.status === 'present').length,
    late: items.filter(a => a.status === 'late').length,
    absent: items.filter(a => a.status === 'absent' || a.status === 'leave').length
  }
})

function fmtTime(t) { return t ? t.substring(11, 16) : '' }

function onCourseChange(e) {
  const c = courses.value[e.detail.value]
  if (c) { selectedCourseId.value = c.id; selectedCourseName.value = c.courseName }
  else { selectedCourseId.value = ''; selectedCourseName.value = '' }
  loadData()
}

async function loadData() {
  try {
    const params = {}
    if (filterDate.value) { params.startDate = filterDate.value; params.endDate = filterDate.value }
    if (selectedCourseId.value) params.courseId = selectedCourseId.value
    const res = await getAttendanceList({ pageNum: 1, pageSize: 500, ...params })
    list.value = res.records || []
  } catch (e) { console.error('加载考勤失败', e) }
}

async function refreshData() {
  uni.showLoading({ title: '刷新中...' })
  await loadData()
  uni.hideLoading()
  uni.showToast({ title: '刷新完成', icon: 'success' })
}

onMounted(async () => {
  try {
    const res = await getCourseList({ pageNum: 1, pageSize: 100 })
    courses.value = res.records || []
    courseNames.value = courses.value.map(c => c.courseName)
  } catch (e) { console.error(e) }
  loadData()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20rpx; }

.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20rpx 24rpx; background: #fff; border-bottom: 1rpx solid #ebeef5;
}
.title { font-size: 34rpx; font-weight: 600; color: #303133; }
.course-pick {
  padding: 10rpx 20rpx; border: 1rpx solid #4A90D9; border-radius: 8rpx;
  font-size: 26rpx; color: #4A90D9;
}

.filter-row { padding: 16rpx 20rpx; background: #fff; }
.date-pick {
  display: inline-block; padding: 10rpx 24rpx; background: #f5f5f5;
  border-radius: 30rpx; font-size: 26rpx; color: #606266;
}

.stats-row { display: flex; margin: 16rpx 20rpx; gap: 12rpx; }
.stat-card { flex: 1; background: #fff; border-radius: 12rpx; padding: 20rpx 8rpx; text-align: center; }
.sc-num { font-size: 32rpx; font-weight: 700; color: #303133; display: block; }
.sc-label { font-size: 22rpx; color: #909399; margin-top: 4rpx; }
.c-green .sc-num { color: #67C23A; }
.c-orange .sc-num { color: #E6A23C; }
.c-red .sc-num { color: #F56C6C; }

.empty { text-align: center; padding: 120rpx 0; color: #c0c4cc; font-size: 28rpx; }

.att-item {
  display: flex; align-items: center; background: #fff;
  margin: 0 20rpx 8rpx; padding: 24rpx; border-radius: 12rpx;
}
.ai-left { flex: 1; }
.ai-name { font-size: 28rpx; font-weight: 500; color: #303133; display: block; }
.ai-date { font-size: 24rpx; color: #909399; }
.ai-status { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx; }
.as-present { background: #f0f9eb; color: #67C23A; }
.as-late { background: #fdf6ec; color: #E6A23C; }
.as-absent { background: #fef0f0; color: #F56C6C; }
.as-leave { background: #f4f4f5; color: #909399; }
.ai-time { font-size: 26rpx; color: #909399; margin-left: 16rpx; }

.bottom-bar { padding: 20rpx; }
.refresh-btn {
  width: 100%; height: 88rpx; background: linear-gradient(135deg, #4A90D9, #357ABD);
  color: #fff; border: none; border-radius: 44rpx; font-size: 30rpx; font-weight: 500;
}
</style>
