<template>
  <view class="home-page">
    <view class="header">
      <view class="h-top">
        <view class="avatar">{{ (user.realName || '学')[0] }}</view>
        <view class="h-info">
          <text class="h-name">{{ user.realName || '同学' }}</text>
          <text class="h-role">学生</text>
        </view>
        <text class="h-date">{{ today }}</text>
      </view>
      <view class="h-stats">
        <view class="hs-item">
          <text class="hs-num">{{ stats.totalCourses }}</text>
          <text class="hs-label">课程</text>
        </view>
        <view class="hs-item">
          <text class="hs-num">{{ stats.presentCount }}</text>
          <text class="hs-label">出勤</text>
        </view>
        <view class="hs-item">
          <text class="hs-num">{{ stats.absentCount }}</text>
          <text class="hs-label">缺勤</text>
        </view>
        <view class="hs-item">
          <text class="hs-num">{{ stats.rate }}%</text>
          <text class="hs-label">出勤率</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="s-header">
        <text class="s-title">今日课程</text>
        <text class="s-more" @tap="goPage('/pages/student/schedule/index')">完整课表 ›</text>
      </view>
      <view v-if="todayCourses.length === 0" class="empty">今日无课程安排</view>
      <view v-for="c in todayCourses" :key="c.id" class="course-card">
        <view class="cc-time">
          <text class="cct-start">{{ fmtTime(c.startTime) }}</text>
          <text class="cct-line">|</text>
          <text class="cct-end">{{ fmtTime(c.endTime) }}</text>
        </view>
        <view class="cc-body">
          <text class="cc-name">{{ c.courseName }}</text>
          <text class="cc-room">{{ c.classroom || '未指定教室' }}</text>
        </view>
        <view class="cc-action">
          <button v-if="c._status === 'pending'" class="cc-btn" @tap="goCheckin(c)">签到</button>
          <text v-else-if="c._status === 'present'" class="tag tag-green">已签到</text>
          <text v-else-if="c._status === 'late'" class="tag tag-orange">迟到</text>
          <text v-else class="tag tag-gray">已结束</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="s-header"><text class="s-title">快捷功能</text></view>
      <view class="quick-grid">
        <view class="q-item" v-for="q in quickItems" :key="q.path" @tap="goPage(q.path)">
          <text class="q-icon">{{ q.icon }}</text>
          <text class="q-label">{{ q.label }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCoursesByClassId } from '@/api/course'
import { getAttendanceList } from '@/api/attendance'
import { getStudentDashboardStats } from '@/api/statistics'
import { switchTabBar } from '@/utils/tabBar'

const user = ref({})
const today = ref('')
const todayCourses = ref([])
const stats = ref({ totalCourses: 0, presentCount: 0, absentCount: 0, rate: 100 })

const weekNames = ['周日','周一','周二','周三','周四','周五','周六']
const quickItems = [
  { path: '/pages/student/checkin/index', icon: '📷', label: '人脸签到' },
  { path: '/pages/student/schedule/index', icon: '📅', label: '课程表' },
  { path: '/pages/student/attendance/index', icon: '📋', label: '考勤记录' },
  { path: '/pages/student/qrscan/index', icon: '📱', label: '扫码签到' },
  { path: '/pages/student/leave/index', icon: '📝', label: '请假申请' },
  { path: '/pages/student/behavior/index', icon: '📊', label: '行为记录' },
]

function fmtTime(t) { return t ? t.substring(0, 5) : '--' }
function goPage(url) { uni.navigateTo({ url }) }
function goCheckin(c) {
  uni.navigateTo({ url: `/pages/student/checkin/index?courseId=${c.id}&courseName=${encodeURIComponent(c.courseName)}` })
}

onShow(() => switchTabBar('student'))

onMounted(async () => {
  try { user.value = uni.getStorageSync('userInfo') || {} } catch { user.value = {} }
  const now = new Date()
  const dateStr = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')}`
  const weekDay = weekNames[now.getDay()]
  today.value = `${dateStr} ${weekDay}`

  const studentId = user.value.studentId
  const classId = user.value.classId
  try {
    const [cRes, aRes, sRes] = await Promise.all([
      classId ? getCoursesByClassId(classId) : Promise.resolve([]),
      getAttendanceList({ pageNum: 1, pageSize: 500 }),
      studentId ? getStudentDashboardStats(studentId) : Promise.resolve({})
    ])
    const allCourses = cRes || []
    const attendances = aRes.records || []
    const ss = sRes || {}
    const todayAtt = {}
    attendances.forEach(a => { if (a.attendanceDate === dateStr) todayAtt[a.courseId] = a })

    todayCourses.value = allCourses
      .filter(c => c.weekDay === weekDay)
      .sort((a, b) => (a.startTime || '').localeCompare(b.startTime || ''))
      .map(c => ({ ...c, _status: (todayAtt[c.id] || {}).status || 'pending' }))

    stats.value = {
      totalCourses: ss.totalCourses || allCourses.length,
      presentCount: ss.presentCount || 0,
      absentCount: ss.absentCount || 0,
      rate: ss.attendanceRate || 100
    }
  } catch (e) { console.error('加载数据失败', e) }
})
</script>

<style scoped>
.home-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20rpx; }

.header { background: linear-gradient(135deg, #4A90D9, #357ABD); padding: 40rpx 32rpx 32rpx; color: #fff; }
.h-top { display: flex; align-items: center; margin-bottom: 28rpx; }
.avatar {
  width: 80rpx; height: 80rpx; border-radius: 50%;
  background: rgba(255,255,255,0.25); display: flex;
  align-items: center; justify-content: center;
  font-size: 36rpx; font-weight: 700; margin-right: 20rpx;
}
.h-name { font-size: 34rpx; font-weight: 600; display: block; }
.h-role { font-size: 24rpx; opacity: 0.8; }
.h-date { margin-left: auto; font-size: 26rpx; opacity: 0.85; }

.h-stats { display: flex; }
.hs-item { flex: 1; text-align: center; }
.hs-num { font-size: 36rpx; font-weight: 700; display: block; }
.hs-label { font-size: 22rpx; opacity: 0.8; }

.section { background: #fff; margin: 20rpx; border-radius: 16rpx; padding: 24rpx; }
.s-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.s-title { font-size: 30rpx; font-weight: 600; color: #303133; }
.s-more { font-size: 26rpx; color: #4A90D9; }

.empty { text-align: center; padding: 60rpx 0; color: #c0c4cc; font-size: 28rpx; }

.course-card {
  display: flex; align-items: center; padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.course-card:last-child { border-bottom: none; }
.cc-time { min-width: 100rpx; text-align: center; margin-right: 16rpx; }
.cct-start { font-size: 30rpx; font-weight: 600; color: #409EFF; display: block; }
.cct-line { font-size: 20rpx; color: #ccc; }
.cct-end { font-size: 26rpx; color: #666; display: block; }
.cc-body { flex: 1; }
.cc-name { font-size: 28rpx; font-weight: 500; color: #303133; display: block; }
.cc-room { font-size: 24rpx; color: #909399; }

.cc-btn {
  background: linear-gradient(135deg, #4A90D9, #357ABD);
  color: #fff; font-size: 24rpx; padding: 10rpx 28rpx;
  border-radius: 30rpx; border: none; line-height: 1.4;
}
.tag { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx; }
.tag-green { background: #f0f9eb; color: #67C23A; }
.tag-orange { background: #fdf6ec; color: #E6A23C; }
.tag-gray { background: #f4f4f5; color: #909399; }

.quick-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16rpx; }
.q-item {
  display: flex; flex-direction: column; align-items: center;
  padding: 24rpx 8rpx; background: #f8f9fb; border-radius: 16rpx;
}
.q-item:active { background: #ecf5ff; }
.q-icon { font-size: 40rpx; margin-bottom: 8rpx; }
.q-label { font-size: 24rpx; color: #606266; }
</style>
