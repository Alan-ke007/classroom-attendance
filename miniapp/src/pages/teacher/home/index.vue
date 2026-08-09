<template>
  <view class="home-page">
    <view class="header">
      <view class="h-top">
        <view class="avatar">{{ (user.realName || '教')[0] }}</view>
        <view class="h-info">
          <text class="h-name">{{ user.realName || '教师' }}</text>
          <text class="h-role">{{ user.role === 'admin' ? '管理员' : '教师' }}</text>
        </view>
        <text class="h-date">{{ today }}</text>
      </view>
    </view>

    <view class="stats-row">
      <view class="stat-card c-blue" @tap="goPage('/pages/teacher/course/index')">
        <text class="sc-num">{{ stats.courses }}</text>
        <text class="sc-label">课程数</text>
      </view>
      <view class="stat-card c-green" @tap="goPage('/pages/teacher/student/index')">
        <text class="sc-num">{{ stats.students }}</text>
        <text class="sc-label">学生数</text>
      </view>
      <view class="stat-card c-orange" @tap="goPage('/pages/teacher/attendance/index')">
        <text class="sc-num">{{ stats.todayAtt }}</text>
        <text class="sc-label">今日考勤</text>
      </view>
      <view class="stat-card c-red" @tap="goPage('/pages/teacher/behavior/index')">
        <text class="sc-num">{{ stats.behaviors }}</text>
        <text class="sc-label">待处理行为</text>
      </view>
    </view>

    <view class="menu-section">
      <view class="menu-item" v-for="m in menus" :key="m.path" @tap="goPage(m.path)">
        <text class="mi-icon">{{ m.icon }}</text>
        <view class="mi-body">
          <text class="mi-title">{{ m.title }}</text>
          <text class="mi-desc">{{ m.desc }}</text>
        </view>
        <text class="mi-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getDashboardStats } from '@/api/statistics'
import { switchTabBar } from '@/utils/tabBar'

const user = ref({})
const today = ref('')
const stats = ref({ courses: 0, students: 0, todayAtt: 0, behaviors: 0 })

const menus = [
  { path: '/pages/teacher/course/index', icon: '📚', title: '课程管理', desc: '管理课程信息，生成签到二维码' },
  { path: '/pages/teacher/attendance/index', icon: '📋', title: '考勤管理', desc: '查看和管理学生出勤记录' },
  { path: '/pages/teacher/behavior/index', icon: '⚠️', title: '行为预警', desc: '处理学生课堂异常行为' },
  { path: '/pages/teacher/student/index', icon: '👥', title: '学生管理', desc: '查看班级学生花名册' },
  { path: '/pages/teacher/leave/index', icon: '📝', title: '请假审批', desc: '审批学生请假申请' },
  { path: '/pages/notification/index', icon: '🔔', title: '通知中心', desc: '系统消息与考勤通知' },
]

function goPage(url) { uni.navigateTo({ url }) }

onShow(() => switchTabBar('teacher'))

onMounted(async () => {
  try { user.value = uni.getStorageSync('userInfo') || {} } catch { user.value = {} }
  const now = new Date()
  const w = ['日','一','二','三','四','五','六']
  today.value = `${now.getFullYear()}/${now.getMonth()+1}/${now.getDate()} 星期${w[now.getDay()]}`

  try {
    const res = await getDashboardStats()
    const d = res || {}
    stats.value = {
      courses: d.courseCount || 0,
      students: d.studentCount || 0,
      todayAtt: d.todayAttendance || 0,
      behaviors: d.unhandledBehavior || 0
    }
  } catch (e) { console.error('加载统计失败', e) }
})
</script>

<style scoped>
.home-page { min-height: 100vh; background: #f5f5f5; }

.header { background: linear-gradient(135deg, #4A90D9, #357ABD); padding: 40rpx 32rpx; color: #fff; }
.h-top { display: flex; align-items: center; }
.avatar {
  width: 80rpx; height: 80rpx; border-radius: 50%;
  background: rgba(255,255,255,0.25); display: flex;
  align-items: center; justify-content: center;
  font-size: 36rpx; font-weight: 700; margin-right: 20rpx;
}
.h-name { font-size: 34rpx; font-weight: 600; display: block; }
.h-role { font-size: 24rpx; opacity: 0.8; }
.h-date { margin-left: auto; font-size: 26rpx; opacity: 0.85; }

.stats-row { display: flex; padding: 20rpx; gap: 16rpx; }
.stat-card {
  flex: 1; background: #fff; border-radius: 16rpx;
  padding: 24rpx 8rpx; text-align: center;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.c-blue { border-top: 5rpx solid #409EFF; }
.c-green { border-top: 5rpx solid #67C23A; }
.c-orange { border-top: 5rpx solid #E6A23C; }
.c-red { border-top: 5rpx solid #F56C6C; }
.sc-num { font-size: 36rpx; font-weight: 700; color: #303133; display: block; }
.sc-label { font-size: 22rpx; color: #909399; margin-top: 4rpx; }

.menu-section { background: #fff; margin: 0 20rpx; border-radius: 16rpx; overflow: hidden; }
.menu-item {
  display: flex; align-items: center; padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
}
.menu-item:last-child { border-bottom: none; }
.menu-item:active { background: #f8f9fb; }
.mi-icon { font-size: 40rpx; margin-right: 20rpx; }
.mi-body { flex: 1; }
.mi-title { font-size: 30rpx; font-weight: 500; color: #303133; display: block; }
.mi-desc { font-size: 24rpx; color: #909399; }
.mi-arrow { font-size: 36rpx; color: #c0c4cc; }
</style>
