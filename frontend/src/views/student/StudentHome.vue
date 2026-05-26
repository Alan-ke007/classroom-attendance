<template>
  <div class="student-home" style="position: relative;">
    <ParticleBackground mode="floating" color="#007AFF" />
    <div class="welcome-banner">
      <div class="user-info">
        <div class="avatar">{{ (userInfo.realName || '学')[0] }}</div>
        <div>
          <div class="name">{{ userInfo.realName || '同学' }}</div>
          <div class="role">学生 · {{ userInfo.username }}</div>
        </div>
      </div>
      <div class="date-text">{{ today }}</div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <div class="stat-card blue">
          <div class="stat-num">{{ stats.totalCourses }}</div>
          <div class="stat-label">我的课程</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card green">
          <div class="stat-num">{{ stats.presentCount }}</div>
          <div class="stat-label">已出勤</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card orange">
          <div class="stat-num">{{ stats.absentCount }}</div>
          <div class="stat-label">缺勤</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card red">
          <div class="stat-num">{{ stats.attendanceRate }}%</div>
          <div class="stat-label">出勤率</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>今日课程 ({{ todayCourses.length }})</span>
              <el-button text type="primary" @click="$router.push('/student/schedule')">完整课表</el-button>
            </div>
          </template>
          <el-empty v-if="todayCourses.length === 0" description="今日无课程" />
          <div v-else class="course-list">
            <div v-for="c in todayCourses" :key="c.id" class="course-item">
              <div class="course-time">
                <span class="time-start">{{ c.startTime?.substring(0,5) }}</span>
                <span class="time-sep">-</span>
                <span class="time-end">{{ c.endTime?.substring(0,5) }}</span>
              </div>
              <div class="course-info">
                <span class="course-name">{{ c.courseName }}</span>
                <span class="course-room">{{ c.classroom || '待定' }}</span>
              </div>
              <div class="course-action">
                <el-tag v-if="c._status === 'pending'" type="info" size="small">未签到</el-tag>
                <el-tag v-else-if="c._status === 'present'" type="success" size="small">已签到</el-tag>
                <el-tag v-else-if="c._status === 'late'" type="warning" size="small">迟到</el-tag>
                <el-tag v-else-if="c._status === 'absent'" type="danger" size="small">缺勤</el-tag>
                <el-tag v-else type="info" size="small">已结束</el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never" style="margin-bottom: 12px">
          <template #header><span style="font-weight:600">快捷功能</span></template>
          <div class="quick-grid">
            <div class="q-item" @click="$router.push('/student/schedule')">
              <span class="q-icon">📅</span><span>课程表</span>
            </div>
            <div class="q-item" @click="$router.push('/student/attendance')">
              <span class="q-icon">📋</span><span>考勤记录</span>
            </div>
            <div class="q-item" @click="$router.push('/student/qrscan')">
              <span class="q-icon">📱</span><span>扫码签到</span>
            </div>
            <div class="q-item" @click="$router.push('/student/leave')">
              <span class="q-icon">📝</span><span>请假申请</span>
            </div>
            <div class="q-item" @click="$router.push('/student/behavior')">
              <span class="q-icon">📊</span><span>行为记录</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="never">
          <template #header><span style="font-weight:600">最近考勤</span></template>
          <el-empty v-if="recentList.length === 0" description="暂无记录" />
          <div v-else class="recent-list">
            <div v-for="a in recentList" :key="a.id" class="recent-item">
              <div class="r-left">
                <span class="r-course">{{ a.courseName || '未知课程' }}</span>
                <span class="r-date">{{ a.attendanceDate }}</span>
              </div>
              <el-tag :type="statusType(a.status)" size="small">{{ statusText(a.status) }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCoursesByClassId, getAttendanceList, getStudentDashboardStats } from '@/api/student'
import ParticleBackground from '@/components/sci-fi/ParticleBackground.vue'

const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
const today = ref('')
const todayCourses = ref([])
const recentList = ref([])
const stats = ref({ totalCourses: 0, presentCount: 0, absentCount: 0, attendanceRate: 100 })

const weekNames = ['周日','周一','周二','周三','周四','周五','周六']

function statusType(s) {
  return { present: 'success', absent: 'danger', late: 'warning', leave: 'info' }[s] || 'info'
}
function statusText(s) {
  return { present: '出勤', absent: '缺勤', late: '迟到', leave: '请假' }[s] || s
}

onMounted(async () => {
  const now = new Date()
  const dateStr = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')}`
  const weekDay = weekNames[now.getDay()]
  today.value = `${dateStr} ${weekDay}`

  const studentId = userInfo.value.studentId
  const classId = userInfo.value.classId
  try {
    // 并行加载数据
    const [courseRes, attendRes, statsRes] = await Promise.all([
      classId ? getCoursesByClassId(classId) : Promise.resolve({ data: [] }),
      getAttendanceList({ pageNum: 1, pageSize: 500 }),
      studentId ? getStudentDashboardStats(studentId) : Promise.resolve({ data: {} })
    ])

    const allCourses = courseRes.data || []
    const attendances = attendRes.data?.records || []
    const studentStats = statsRes.data || {}

    // 筛选今日课程
    const todayAttMap = {}
    attendances.forEach(a => {
      if (a.attendanceDate === dateStr) todayAttMap[a.courseId] = a
    })

    todayCourses.value = allCourses
      .filter(c => c.weekDay === weekDay)
      .sort((a, b) => (a.startTime || '').localeCompare(b.startTime || ''))
      .map(c => {
        const att = todayAttMap[c.id]
        return { ...c, _status: att ? att.status : 'pending' }
      })

    // 最近考勤记录(最近10条)
    recentList.value = attendances
      .sort((a, b) => (b.attendanceDate || '').localeCompare(a.attendanceDate || ''))
      .slice(0, 6)

    // 使用学生专属统计数据
    stats.value = {
      totalCourses: studentStats.totalCourses || allCourses.length,
      presentCount: studentStats.presentCount || 0,
      absentCount: studentStats.absentCount || 0,
      attendanceRate: studentStats.attendanceRate || 100
    }
  } catch (e) {
    console.error('加载数据失败', e)
  }
})
</script>

<style scoped>
.student-home { max-width: 1200px; margin: 0 auto; }

.welcome-banner {
  display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  border-radius: 16px; padding: 24px 28px; margin-bottom: 20px;
  color: #fff; box-shadow: var(--shadow-glow);
}
.user-info { display: flex; align-items: center; gap: 12px; }
.avatar {
  width: 48px; height: 48px; border-radius: 50%;
  background: rgba(255,255,255,0.25); display: flex;
  align-items: center; justify-content: center;
  font-size: 22px; font-weight: 700;
}
.name { font-size: 18px; font-weight: 600; }
.role { font-size: 12px; opacity: 0.8; margin-top: 2px; }
.date-text { font-size: 14px; opacity: 0.85; }

.stats-row { margin-bottom: 0; }
.stat-card {
  background: var(--c-glass-bg);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  border-radius: 12px; padding: 20px; text-align: center;
  box-shadow: var(--c-glass-shadow);
  border-left: 4px solid var(--c-primary);
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
}
.stat-card:hover {
  transform: translateY(-4px) rotateX(2deg);
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
}
.stat-card.green { border-left-color: var(--c-success); }
.stat-card.orange { border-left-color: var(--c-warning); }
.stat-card.red { border-left-color: var(--c-danger); }
.stat-num { font-size: 28px; font-weight: 700; color: var(--c-text); }
.stat-label { font-size: 13px; color: var(--c-text-tertiary); margin-top: 4px; }

.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header span { color: var(--c-text); font-weight: 600; }
:deep(.el-card) {
  background: var(--c-glass-bg);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--c-glass-shadow);
  transition: all 0.3s ease;
}
:deep(.el-card:hover) {
  box-shadow: 0 4px 20px var(--c-shadow);
  border-color: var(--c-primary);
}
:deep(.el-card__header) {
  border-bottom: 1px solid var(--c-border-light);
  color: var(--c-text);
}

.course-list { display: flex; flex-direction: column; }
.course-item {
  display: flex; align-items: center; padding: 12px 0;
  border-bottom: 1px solid var(--c-border-light);
}
.course-item:last-child { border-bottom: none; }
.course-time {
  min-width: 70px; text-align: center; margin-right: 12px;
}
.time-start { font-size: 15px; font-weight: 600; color: var(--c-primary); }
.time-sep { color: var(--c-text-tertiary, #ccc); margin: 0 2px; }
.time-end { font-size: 13px; color: var(--c-text-tertiary); }
.course-info { flex: 1; }
.course-name { font-size: 14px; font-weight: 500; display: block; color: var(--c-text); }
.course-room { font-size: 12px; color: var(--c-text-tertiary); }

.quick-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.q-item {
  display: flex; flex-direction: column; align-items: center;
  padding: 14px 4px; border-radius: 10px; cursor: pointer;
  background: var(--c-glass-bg);
  backdrop-filter: blur(8px) saturate(180%);
  -webkit-backdrop-filter: blur(8px) saturate(180%);
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  font-size: 12px; color: var(--c-text-secondary);
  border: 1px solid transparent;
}
.q-item:hover {
  background: var(--c-primary-bg);
  border-color: var(--c-primary);
  transform: translateY(-3px);
  box-shadow: 0 4px 12px var(--c-shadow);
}
.q-icon { font-size: 22px; margin-bottom: 4px; }

.recent-list { display: flex; flex-direction: column; }
.recent-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; border-bottom: 1px solid var(--c-border-light);
}
.recent-item:last-child { border-bottom: none; }
.r-course { font-size: 13px; font-weight: 500; display: block; color: var(--c-text); }
.r-date { font-size: 11px; color: var(--c-text-tertiary); }
</style>
