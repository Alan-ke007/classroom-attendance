<template>
  <div class="student-bento">
    <!-- ====== Row 1: Welcome + Credit Score ====== -->
    <div class="bento-grid bento-row-1">
      <!-- Welcome Banner -->
      <div class="bento-card welcome-card" style="grid-area: welcome">
        <div class="welcome-bg" />
        <div class="welcome-content">
          <div class="welcome-avatar">{{ (userInfo.realName || '学')[0] }}</div>
          <div class="welcome-text">
            <h2>{{ greeting }}, {{ userInfo.realName || '同学' }}</h2>
            <p>{{ today }} · {{ schedule.weekDay || '' }}</p>
          </div>
        </div>
        <div class="welcome-deco">
          <div class="deco-circle c1" />
          <div class="deco-circle c2" />
          <div class="deco-circle c3" />
        </div>
      </div>

      <!-- Credit Score Card -->
      <div class="bento-card credit-card" :class="creditGradeClass" style="grid-area: credit" @click="$router.push('/student/credit')">
        <div class="credit-glow" />
        <div class="credit-header">
          <span class="credit-label">学风分</span>
          <span class="credit-grade-badge" :class="'badge-' + creditGrade.toLowerCase()">{{ creditGrade }}</span>
        </div>
        <div class="credit-body">
          <svg class="credit-ring" viewBox="0 0 100 100">
            <circle cx="50" cy="50" r="38" fill="none" stroke="var(--c-border)" stroke-width="6" />
            <circle cx="50" cy="50" r="38" fill="none" stroke="currentColor" stroke-width="6"
              :stroke-dasharray="239" :stroke-dashoffset="239 - 239 * creditPct / 100"
              stroke-linecap="round" transform="rotate(-90 50 50)" class="credit-ring-fill" />
          </svg>
          <div class="credit-center">
            <span class="credit-score">{{ creditScore }}</span>
          </div>
        </div>
        <div class="credit-footer">
          <span class="cf-item plus">+{{ creditEarned }}</span>
          <span class="cf-divider">/</span>
          <span class="cf-item minus">-{{ creditDeducted }}</span>
        </div>
      </div>
    </div>

    <!-- ====== Row 2: 4 Stat Cards + Today Schedule ====== -->
    <div class="bento-grid bento-row-2">
      <div class="bento-card stat-card" style="grid-area: courses">
        <div class="stat-icon sc-courses">
          <el-icon :size="20"><Calendar /></el-icon>
        </div>
        <span class="stat-val">{{ schedule.totalCourses || 0 }}</span>
        <span class="stat-lbl">今日课程</span>
      </div>

      <div class="bento-card stat-card" style="grid-area: checkin">
        <div class="stat-icon sc-checkin">
          <el-icon :size="20"><CircleCheckFilled /></el-icon>
        </div>
        <span class="stat-val green">{{ todayCheckedIn }}</span>
        <span class="stat-lbl">已签到</span>
      </div>

      <div class="bento-card stat-card" style="grid-area: pending">
        <div class="stat-icon sc-pending">
          <el-icon :size="20"><Clock /></el-icon>
        </div>
        <span class="stat-val orange">{{ pendingLeaves }}</span>
        <span class="stat-lbl">待审批</span>
        <span v-if="pendingLeaves > 0" class="stat-badge">!</span>
      </div>

      <div class="bento-card stat-card" style="grid-area: rate">
        <div class="stat-icon sc-rate">
          <el-icon :size="20"><TrendCharts /></el-icon>
        </div>
        <span class="stat-val" :class="attendanceColor">{{ attendanceRate }}%</span>
        <span class="stat-lbl">出勤率</span>
        <div class="mini-bar">
          <div class="mini-bar-fill" :class="attendanceColor" :style="{ width: attendanceRate + '%' }" />
        </div>
      </div>

      <!-- Today Schedule -->
      <div class="bento-card schedule-card" style="grid-area: schedule">
        <div class="card-head">
          <span class="card-head-title">今日课表</span>
          <el-button text type="primary" size="small" @click="$router.push('/student/qrscan')">
            <el-icon style="margin-right:4px"><Iphone /></el-icon>扫码签到
          </el-button>
        </div>
        <div v-if="!schedule.schedule || schedule.schedule.length === 0" class="empty-hint">
          <el-icon :size="32"><Sunny /></el-icon>
          <span>今天没有课程，休息一下</span>
        </div>
        <div v-else class="schedule-list">
          <div v-for="(item, i) in schedule.schedule" :key="i" class="sch-item">
            <div class="sch-time-block">
              <span class="sch-start">{{ item.startTime?.substring(0, 5) }}</span>
              <div class="sch-time-divider">
                <div class="sch-dot" :class="'dot-' + (i % 4)" />
                <div class="sch-line" />
              </div>
            </div>
            <div class="sch-body">
              <span class="sch-name">{{ item.courseName }}</span>
              <span class="sch-room">{{ item.classroom || '待定教室' }}</span>
            </div>
            <el-tag v-if="item.checkedIn > 0" :type="item.late > 0 ? 'warning' : 'success'" size="small" round>
              {{ item.late > 0 ? '迟到' : '已签到' }}
            </el-tag>
            <el-tag v-else type="info" size="small" round>待签到</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== Row 3: Quick Actions + Pending + Announcements ====== -->
    <div class="bento-grid bento-row-3">
      <!-- Quick Actions -->
      <div class="bento-card quick-card" style="grid-area: quick">
        <div class="card-head">
          <span class="card-head-title">快捷功能</span>
        </div>
        <div class="quick-grid">
          <div class="q-item" @click="$router.push('/student/qrscan')">
            <div class="q-icon q-cyan"><el-icon :size="20"><Iphone /></el-icon></div>
            <span>扫码签到</span>
          </div>
          <div class="q-item" @click="$router.push('/student/leave')">
            <div class="q-icon q-magenta"><el-icon :size="20"><Notebook /></el-icon></div>
            <span>请假申请</span>
          </div>
          <div class="q-item" @click="$router.push('/student/schedule')">
            <div class="q-icon q-yellow"><el-icon :size="20"><Calendar /></el-icon></div>
            <span>课程表</span>
          </div>
          <div class="q-item" @click="$router.push('/student/attendance')">
            <div class="q-icon q-green"><el-icon :size="20"><List /></el-icon></div>
            <span>考勤记录</span>
          </div>
          <div class="q-item" @click="$router.push('/student/behavior')">
            <div class="q-icon q-orange"><el-icon :size="20"><WarningFilled /></el-icon></div>
            <span>行为记录</span>
          </div>
          <div class="q-item" @click="$router.push('/student/weekly-report')">
            <div class="q-icon q-purple"><el-icon :size="20"><DataAnalysis /></el-icon></div>
            <span>课堂周报</span>
          </div>
        </div>
      </div>

      <!-- Pending Leave Status -->
      <div class="bento-card" style="grid-area: pleave" v-if="pendingLeaves > 0" @click="$router.push('/student/leave')">
        <div class="card-head">
          <span class="card-head-title">请假进度</span>
          <span class="pending-count">{{ pendingLeaves }}</span>
        </div>
        <div class="pending-prompt">
          <div class="pending-orb" />
          <span>有 {{ pendingLeaves }} 条请假申请审批中</span>
          <el-icon :size="14"><ArrowRight /></el-icon>
        </div>
      </div>
      <div class="bento-card" style="grid-area: pleave" v-else>
        <div class="card-head"><span class="card-head-title">请假进度</span></div>
        <div class="empty-hint small">
          <el-icon :size="20"><CircleCheckFilled /></el-icon>
          <span>无待审批请假</span>
        </div>
      </div>

      <!-- Announcements -->
      <div class="bento-card anno-card" style="grid-area: anno" v-if="announcements.length > 0">
        <div class="card-head">
          <span class="card-head-title">公告</span>
        </div>
        <div class="anno-list">
          <div v-for="a in announcements.slice(0, 2)" :key="a.id" class="anno-item">
            <el-tag v-if="a.isPinned" type="warning" size="small" round effect="dark">置顶</el-tag>
            <span class="a-title">{{ a.title }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, Iphone, List, Notebook, WarningFilled, DataAnalysis, CircleCheckFilled, Clock, TrendCharts, Sunny, ArrowRight } from '@element-plus/icons-vue'
import { getTodaySchedule, getPendingTasks } from '@/api/statistics'
import { getStudentDashboardStats, getCreditScore } from '@/api/student'
import { getActiveAnnouncements } from '@/api/announcement'

const router = useRouter()
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
const today = ref('')
const greeting = ref('')
const schedule = ref({ totalCourses: 0, weekDay: '', schedule: [] })
const pendingLeaves = ref(0)
const attendanceRate = ref(100)
const creditScore = ref(100)
const creditEarned = ref(0)
const creditDeducted = ref(0)
const announcements = ref([])

const todayCheckedIn = computed(() => {
  if (!schedule.value.schedule) return 0
  return schedule.value.schedule.filter(c => c.checkedIn > 0).length
})

const attendanceColor = computed(() => {
  if (attendanceRate.value >= 90) return 'green'
  if (attendanceRate.value >= 80) return 'orange'
  return 'red'
})

const creditPct = computed(() => Math.min(creditScore.value / 200 * 100, 100))

const creditGrade = computed(() => {
  const s = creditScore.value
  if (s >= 180) return 'S'
  if (s >= 150) return 'A'
  if (s >= 120) return 'B'
  if (s >= 80) return 'C'
  return 'D'
})

const creditGradeClass = computed(() => 'grade-' + creditGrade.value.toLowerCase())

onMounted(async () => {
  const now = new Date()
  const hour = now.getHours()
  if (hour < 9) greeting.value = '早上好'
  else if (hour < 12) greeting.value = '上午好'
  else if (hour < 14) greeting.value = '中午好'
  else if (hour < 18) greeting.value = '下午好'
  else greeting.value = '晚上好'

  const w = ['日', '一', '二', '三', '四', '五', '六']
  today.value = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${w[now.getDay()]}`

  try {
    const [scheduleRes, tasksRes, statsRes, creditRes, aRes] = await Promise.all([
      getTodaySchedule(),
      getPendingTasks(),
      getStudentDashboardStats(userInfo.value.studentId),
      getCreditScore(userInfo.value.studentId),
      getActiveAnnouncements()
    ])
    schedule.value = scheduleRes.data || { totalCourses: 0, weekDay: '', schedule: [] }
    pendingLeaves.value = tasksRes.data?.pendingLeaveCount || 0
    attendanceRate.value = statsRes.data?.attendanceRate || 100
    announcements.value = aRes.data || []
    if (creditRes.data) {
      creditScore.value = creditRes.data.creditScore ?? 100
      creditEarned.value = creditRes.data.creditEarned ?? 0
      creditDeducted.value = creditRes.data.creditDeducted ?? 0
    }
  } catch (e) {
    console.error('加载数据失败', e)
  }
})
</script>

<style scoped>
/* ========================================
   STUDENT BENTO DASHBOARD
   ======================================== */
.student-bento {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bento-grid {
  display: grid;
  gap: 14px;
}

/* Row 1: Welcome (2fr) + Credit (1fr) */
.bento-row-1 {
  grid-template-columns: 2fr 1fr;
  grid-template-areas: "welcome credit";
}

/* Row 2: 4 stats + tall schedule */
.bento-row-2 {
  grid-template-columns: 1fr 1fr 1fr 1fr 2fr;
  grid-template-areas: "courses checkin pending rate schedule";
}

/* Row 3: Quick actions + Pending leave + Announcements */
.bento-row-3 {
  grid-template-columns: 2fr 1fr 1fr;
  grid-template-areas: "quick pleave anno";
}

/* ====== Bento Card Base ====== */
.bento-card {
  background: var(--c-glass-bg);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  border-radius: 18px;
  padding: 18px 20px;
  position: relative;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.bento-card:hover {
  transform: translateY(-2px);
  border-color: var(--c-glass-border-strong);
  box-shadow: 0 8px 32px rgba(0,0,0,0.2), 0 0 0 1px var(--c-glass-border-strong);
}

/* ====== Welcome Card ====== */
.welcome-card {
  background: linear-gradient(135deg, rgba(0,229,255,0.10) 0%, rgba(224,64,251,0.06) 50%, rgba(0,229,255,0.02) 100%);
  border-color: rgba(0,229,255,0.12);
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 120px;
  overflow: hidden;
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 14px;
  z-index: 1;
}

.welcome-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--c-accent), var(--c-accent-2));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 800;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 16px rgba(0,229,255,0.3);
}

.welcome-text h2 {
  font-size: 19px;
  font-weight: 700;
  margin: 0;
  color: var(--c-text);
}

.welcome-text p {
  font-size: 13px;
  color: var(--c-text-secondary);
  margin: 4px 0 0;
}

.welcome-deco {
  position: absolute;
  right: -10px;
  top: -10px;
  width: 120px;
  height: 120px;
  opacity: 0.5;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  border: 1.5px solid rgba(0,229,255,0.2);
}

.deco-circle.c1 { width: 100px; height: 100px; top: 0; right: 0; }
.deco-circle.c2 { width: 70px; height: 70px; top: 30px; right: 30px; border-color: rgba(224,64,251,0.15); }
.deco-circle.c3 { width: 40px; height: 40px; top: 45px; right: 45px; border-color: rgba(255,214,0,0.12); }

/* ====== Credit Card ====== */
.credit-card {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-left: 3px solid var(--c-accent);
}

.credit-card.grade-s {
  border-left-color: #FFD700;
  background: linear-gradient(135deg, rgba(255,215,0,0.06), var(--c-glass-bg));
}
.credit-card.grade-a { border-left-color: #67C23A; }
.credit-card.grade-b { border-left-color: #409EFF; }
.credit-card.grade-c { border-left-color: #E6A23C; }
.credit-card.grade-d { border-left-color: #F56C6C; }

.credit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 4px;
}

.credit-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.credit-grade-badge {
  font-size: 16px;
  font-weight: 900;
  font-family: var(--font-mono);
  padding: 2px 8px;
  border-radius: 6px;
}

.badge-s { color: #FFD700; background: rgba(255,215,0,0.15); }
.badge-a { color: #67C23A; background: rgba(103,194,58,0.12); }
.badge-b { color: #409EFF; background: rgba(64,158,255,0.12); }
.badge-c { color: #E6A23C; background: rgba(230,162,60,0.12); }
.badge-d { color: #F56C6C; background: rgba(245,108,108,0.12); }

.credit-body {
  position: relative;
  width: 80px;
  height: 80px;
}

.credit-ring {
  width: 100%;
  height: 100%;
}

.credit-ring-fill {
  transition: stroke-dashoffset 0.8s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.credit-card.grade-s .credit-ring-fill { stroke: #FFD700; }
.credit-card.grade-a .credit-ring-fill { stroke: #67C23A; }
.credit-card.grade-b .credit-ring-fill { stroke: #409EFF; }
.credit-card.grade-c .credit-ring-fill { stroke: #E6A23C; }
.credit-card.grade-d .credit-ring-fill { stroke: #F56C6C; }

.credit-center {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.credit-score {
  font-size: 22px;
  font-weight: 900;
  font-family: var(--font-mono);
  color: var(--c-text);
}

.credit-footer {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
}

.cf-item {
  font-size: 13px;
  font-weight: 700;
  font-family: var(--font-mono);
}

.cf-item.plus { color: var(--c-success); }
.cf-item.minus { color: var(--c-danger); }
.cf-divider { color: var(--c-text-tertiary); font-size: 12px; }

/* ====== Stat Cards ====== */
.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sc-courses { background: rgba(0,229,255,0.10); color: var(--c-accent); }
.sc-checkin { background: rgba(0,230,118,0.10); color: var(--c-success); }
.sc-pending { background: rgba(255,214,0,0.10); color: var(--c-accent-3); }
.sc-rate { background: rgba(0,229,255,0.08); color: var(--c-accent); }

.stat-val {
  font-size: 30px;
  font-weight: 900;
  color: var(--c-text);
  font-family: var(--font-mono);
  line-height: 1;
}

.stat-val.green { color: var(--c-success); }
.stat-val.orange { color: var(--c-accent-3); }
.stat-val.red { color: var(--c-danger); }

.stat-lbl {
  font-size: 11px;
  color: var(--c-text-tertiary);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.stat-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--c-danger);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: pulse-badge 2s ease-in-out infinite;
}

@keyframes pulse-badge {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255,23,68,0.5); }
  50% { box-shadow: 0 0 0 8px rgba(255,23,68,0); }
}

.mini-bar {
  width: 100%;
  height: 3px;
  border-radius: 2px;
  background: var(--c-border-light);
  overflow: hidden;
  margin-top: 4px;
}

.mini-bar-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.6s cubic-bezier(0.25, 0.1, 0.25, 1);
  background: var(--c-accent);
}

.mini-bar-fill.green { background: var(--c-success); }
.mini-bar-fill.orange { background: var(--c-accent-3); }
.mini-bar-fill.red { background: var(--c-danger); }

/* ====== Schedule Card ====== */
.schedule-card {
  grid-row: span 2;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.card-head-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--c-text);
}

.schedule-list {
  display: flex;
  flex-direction: column;
}

.sch-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--c-border-light);
  transition: background 0.2s;
}

.sch-item:last-child { border-bottom: none; }

.sch-time-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 44px;
}

.sch-start {
  font-size: 14px;
  font-weight: 700;
  color: var(--c-text);
  font-family: var(--font-mono);
}

.sch-time-divider {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 3px;
}

.sch-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot-0 { background: var(--c-accent); }
.dot-1 { background: var(--c-accent-2); }
.dot-2 { background: var(--c-accent-3); }
.dot-3 { background: var(--c-success); }

.sch-line {
  width: 1.5px;
  height: 16px;
  background: var(--c-border-light);
  margin-top: 2px;
}

.sch-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.sch-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
}

.sch-room {
  font-size: 12px;
  color: var(--c-text-tertiary);
  margin-top: 1px;
}

/* ====== Quick Actions Card ====== */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.q-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 8px;
  border-radius: 12px;
  cursor: pointer;
  background: var(--c-bg-alt);
  border: 1px solid transparent;
  transition: all 0.25s ease;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-text-secondary);
}

.q-item:hover {
  border-color: var(--c-accent-border);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,229,255,0.08);
}

.q-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.q-cyan { background: rgba(0,229,255,0.10); color: var(--c-accent); }
.q-magenta { background: rgba(224,64,251,0.10); color: var(--c-accent-2); }
.q-yellow { background: rgba(255,214,0,0.10); color: var(--c-accent-3); }
.q-green { background: rgba(0,230,118,0.10); color: var(--c-success); }
.q-orange { background: rgba(255,152,0,0.10); color: #FF9800; }
.q-purple { background: rgba(156,39,176,0.10); color: #9C27B0; }

/* ====== Pending Leave Card ====== */
.pending-count {
  font-size: 14px;
  font-weight: 700;
  font-family: var(--font-mono);
  color: var(--c-accent-3);
}

.pending-prompt {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 0;
  color: var(--c-accent-3);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.pending-orb {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--c-accent-3);
  animation: pulse-badge 2s ease-in-out infinite;
}

/* ====== Announcements ====== */
.anno-list {
  display: flex;
  flex-direction: column;
}

.anno-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--c-border-light);
}

.anno-item:last-child { border-bottom: none; }

.a-title {
  flex: 1;
  font-size: 14px;
  color: var(--c-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ====== Empty ====== */
.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 28px 0;
  color: var(--c-text-tertiary);
  font-size: 13px;
}

.empty-hint.small { padding: 16px 0; font-size: 12px; }

/* ====== Responsive ====== */
@media (max-width: 1100px) {
  .bento-row-2 {
    grid-template-columns: 1fr 1fr 1fr 1fr;
    grid-template-areas:
      "courses checkin pending rate"
      "schedule schedule schedule schedule";
  }
  .bento-row-3 {
    grid-template-columns: 1fr 1fr;
    grid-template-areas: "quick pleave" "anno anno";
  }
}

@media (max-width: 768px) {
  .student-bento { gap: 10px; }
  .bento-grid { gap: 10px; }

  .bento-row-1 {
    grid-template-columns: 1fr;
    grid-template-areas: "welcome" "credit";
  }
  .bento-row-2 {
    grid-template-columns: 1fr 1fr;
    grid-template-areas:
      "courses checkin"
      "pending rate"
      "schedule schedule";
  }
  .bento-row-3 {
    grid-template-columns: 1fr;
    grid-template-areas: "quick" "pleave" "anno";
  }

  .bento-card { padding: 14px 16px; border-radius: 14px; }

  .welcome-text h2 { font-size: 16px; }
  .welcome-deco { display: none; }
  .welcome-avatar { width: 40px; height: 40px; font-size: 18px; }

  .credit-body { width: 64px; height: 64px; }
  .credit-score { font-size: 18px; }

  .stat-val { font-size: 24px; }
  .quick-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>
