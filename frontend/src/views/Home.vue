<template>
  <div class="bento-dashboard">
    <!-- ====== Row 1: Welcome + Date ====== -->
    <div class="bento-grid">
      <!-- Welcome Banner — spans 2 cols -->
      <div class="bento-card welcome-card" style="grid-area: welcome">
        <div class="welcome-bg" />
        <div class="welcome-content">
          <div class="welcome-greeting">
            <span class="welcome-wave">👋</span>
            <h2>{{ greeting }}, {{ userInfo?.realName || userInfo?.username }}</h2>
          </div>
          <p class="welcome-sub">{{ subtitle }}</p>
          <div class="welcome-tags">
            <span class="tag" v-for="t in quickTags" :key="t">{{ t }}</span>
          </div>
        </div>
        <div class="welcome-visual">
          <div class="orbit-ring ring-1" />
          <div class="orbit-ring ring-2" />
          <div class="orbit-ring ring-3" />
          <div class="orbit-core" />
        </div>
      </div>

      <!-- Date Card -->
      <div class="bento-card date-card" style="grid-area: date">
        <div class="date-stack">
          <span class="date-day">{{ dayNum }}</span>
          <span class="date-weekday">{{ weekDay }}</span>
          <span class="date-full">{{ dateFull }}</span>
        </div>
        <div class="date-divider" />
        <div class="date-stats">
          <div class="ds-item">
            <span class="ds-val">{{ schedule.totalCourses }}</span>
            <span class="ds-lbl">今日课程</span>
          </div>
          <div class="ds-item">
            <span class="ds-val accent">{{ todayCheckedIn }}</span>
            <span class="ds-lbl">已签到</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== Row 2: Stat Cards + Schedule ====== -->
    <div class="bento-grid bento-row-2">
      <!-- Today's Courses -->
      <div class="bento-card stat-card" style="grid-area: courses" @click="$router.push('/dashboard/course')">
        <div class="stat-icon courses-icon">
          <el-icon :size="22"><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-val">{{ schedule.totalCourses }}</span>
          <span class="stat-lbl">今日课程</span>
        </div>
        <div class="stat-bar"><div class="stat-bar-fill" :style="{ width: Math.min(schedule.totalCourses * 20, 100) + '%' }" /></div>
      </div>

      <!-- Checked In -->
      <div class="bento-card stat-card" style="grid-area: checkin" @click="$router.push('/dashboard/attendance/list')">
        <div class="stat-icon checkin-icon">
          <el-icon :size="22"><UserFilled /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-val green">{{ todayCheckedIn }}</span>
          <span class="stat-lbl">已签到</span>
        </div>
        <div class="stat-bar"><div class="stat-bar-fill green" :style="{ width: checkinPercent + '%' }" /></div>
      </div>

      <!-- Pending Leave -->
      <div class="bento-card stat-card" style="grid-area: leave" @click="$router.push('/dashboard/leave')">
        <div class="stat-icon leave-icon">
          <el-icon :size="22"><Notebook /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-val orange">{{ pendingTasks.pendingLeaveCount }}</span>
          <span class="stat-lbl">待审批请假</span>
        </div>
        <div v-if="pendingTasks.pendingLeaveCount > 0" class="stat-dot pulse" />
      </div>

      <!-- Warnings -->
      <div class="bento-card stat-card" style="grid-area: warn" @click="$router.push('/dashboard/behavior/list')">
        <div class="stat-icon warn-icon">
          <el-icon :size="22"><WarningFilled /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-val red">{{ lowAttendCount }}</span>
          <span class="stat-lbl">预警学生</span>
        </div>
        <div v-if="lowAttendCount > 0" class="stat-dot pulse red-dot" />
      </div>

      <!-- Today Schedule — tall card -->
      <div class="bento-card schedule-card" style="grid-area: schedule">
        <div class="card-head">
          <span class="card-head-title">今日课表</span>
          <span class="card-head-badge">{{ schedule.weekDay }}</span>
        </div>
        <div v-if="!schedule.schedule || schedule.schedule.length === 0" class="empty-hint">
          <el-icon :size="32"><Calendar /></el-icon>
          <span>今天没有课程安排</span>
        </div>
        <div v-else class="schedule-list">
          <div v-for="(item, i) in schedule.schedule" :key="i" class="schedule-item">
            <div class="sch-time">
              <span class="sch-start">{{ item.startTime }}</span>
              <span class="sch-end">{{ item.endTime }}</span>
            </div>
            <div class="sch-dot-line">
              <div class="sch-dot" :class="'dot-' + (i % 4)" />
              <div class="sch-line" />
            </div>
            <div class="sch-info">
              <span class="sch-name">{{ item.courseName }}</span>
              <span class="sch-meta">{{ item.className }} · {{ item.classroom }}</span>
            </div>
            <div class="sch-checkin">
              <div class="sch-progress-ring" :style="{ '--pct': item.studentCount > 0 ? Math.round(item.present / item.studentCount * 100) : 0 }">
                <svg viewBox="0 0 36 36">
                  <circle cx="18" cy="18" r="14" fill="none" stroke="var(--c-border)" stroke-width="3" />
                  <circle cx="18" cy="18" r="14" fill="none" stroke="var(--c-accent)" stroke-width="3"
                    stroke-dasharray="88" :stroke-dashoffset="88 - 88 * (item.studentCount > 0 ? Math.round(item.present / item.studentCount * 100) : 0) / 100"
                    stroke-linecap="round" transform="rotate(-90 18 18)" class="progress-ring" />
                </svg>
              </div>
              <span class="sch-ratio">{{ item.present }}/{{ item.studentCount }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== Row 3: Low Attendance + Pending Leave Detail + Announcements ====== -->
    <div class="bento-grid bento-row-3">
      <!-- Low Attendance Students -->
      <div class="bento-card warn-list-card" style="grid-area: lowatt">
        <div class="card-head">
          <span class="card-head-title">出勤率预警</span>
          <el-tag v-if="lowAttendList.length" type="danger" size="small" round>{{ lowAttendList.length }}人</el-tag>
        </div>
        <div v-if="!lowAttendList.length" class="empty-hint small">
          <el-icon :size="24"><CircleCheckFilled /></el-icon>
          <span>全部学生出勤正常</span>
        </div>
        <div v-else class="warn-grid">
          <div v-for="s in lowAttendList.slice(0, 6)" :key="s.studentId" class="warn-chip">
            <el-avatar :size="28" style="font-size:12px">{{ s.studentName?.charAt(0) }}</el-avatar>
            <div class="warn-chip-info">
              <span class="wci-name">{{ s.studentName }}</span>
              <el-progress :percentage="s.rate" :stroke-width="5" :color="s.rate < 60 ? '#FF1744' : '#FFD600'" :show-text="false" />
            </div>
            <span class="wci-pct" :class="{ danger: s.rate < 60 }">{{ s.rate }}%</span>
          </div>
        </div>
      </div>

      <!-- Pending Leave Quick View -->
      <div class="bento-card" style="grid-area: pendleave" @click="$router.push('/dashboard/leave')">
        <div class="card-head">
          <span class="card-head-title">待审批请假</span>
          <span v-if="pendingTasks.pendingLeaveCount" class="pending-count">{{ pendingTasks.pendingLeaveCount }}</span>
        </div>
        <div v-if="!pendingTasks.pendingLeaveCount" class="empty-hint small">
          <el-icon :size="24"><CircleCheckFilled /></el-icon>
          <span>暂无待审批</span>
        </div>
        <div v-else class="pending-prompt">
          <el-icon :size="20"><Bell /></el-icon>
          <span>有 {{ pendingTasks.pendingLeaveCount }} 条请假申请待审批</span>
          <el-icon :size="14"><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- Announcements -->
      <div class="bento-card anno-card" style="grid-area: anno" v-if="announcements.length > 0">
        <div class="card-head">
          <span class="card-head-title">公告</span>
          <el-button text type="primary" size="small" @click="$router.push('/dashboard/announcement')">全部</el-button>
        </div>
        <div class="anno-list">
          <div v-for="a in announcements.slice(0, 3)" :key="a.id" class="anno-item" @click="$router.push('/dashboard/announcement')">
            <el-tag v-if="a.isPinned" type="warning" size="small" round effect="dark">置顶</el-tag>
            <span class="a-title">{{ a.title }}</span>
            <span class="a-time">{{ a.createTime?.substring(0, 10) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, UserFilled, Notebook, WarningFilled, Bell, CircleCheckFilled, ArrowRight } from '@element-plus/icons-vue'
import { getDashboardStats, getTodaySchedule, getPendingTasks } from '@/api/statistics'
import { getActiveAnnouncements } from '@/api/announcement'

const router = useRouter()
const userInfo = ref({})
const today = ref('')
const greeting = ref('')
const subtitle = ref('')
const schedule = ref({ totalCourses: 0, weekDay: '', schedule: [] })
const pendingTasks = ref({ pendingLeaveCount: 0, lowAttendanceStudents: [] })
const announcements = ref([])

const todayCheckedIn = computed(() => {
  if (!schedule.value.schedule) return 0
  return schedule.value.schedule.reduce((sum, c) => sum + (c.present || 0), 0)
})

const totalStudents = computed(() => {
  if (!schedule.value.schedule) return 0
  return schedule.value.schedule.reduce((sum, c) => sum + (c.studentCount || 0), 0)
})

const checkinPercent = computed(() => {
  if (!totalStudents.value) return 0
  return Math.round(todayCheckedIn.value / totalStudents.value * 100)
})

const lowAttendList = computed(() => pendingTasks.value.lowAttendanceStudents || [])
const lowAttendCount = computed(() => lowAttendList.value.length)

const quickTags = computed(() => {
  const tags = []
  if (schedule.value.totalCourses) tags.push(`${schedule.value.totalCourses}节课`)
  if (pendingTasks.value.pendingLeaveCount) tags.push('有待审批')
  if (lowAttendCount.value) tags.push('有预警')
  return tags.length ? tags : ['今天也是元气满满的一天']
})

const d = new Date()
const dayNum = d.getDate()
const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const weekDay = '周' + weekDays[d.getDay()]
const dateFull = `${d.getFullYear()}.${d.getMonth() + 1}.${d.getDate()}`

const hour = d.getHours()
if (hour < 9) greeting.value = '早上好'
else if (hour < 12) greeting.value = '上午好'
else if (hour < 14) greeting.value = '中午好'
else if (hour < 18) greeting.value = '下午好'
else greeting.value = '晚上好'

onMounted(() => {
  const info = localStorage.getItem('userInfo')
  if (info) userInfo.value = JSON.parse(info)
  subtitle.value = `今天 ${weekDay}，共 ${schedule.value.totalCourses} 节课`
  loadData()
})

async function loadData() {
  try {
    const [sRes, tRes, aRes] = await Promise.all([
      getTodaySchedule(),
      getPendingTasks(),
      getActiveAnnouncements()
    ])
    schedule.value = sRes.data || { totalCourses: 0, weekDay: '', schedule: [] }
    pendingTasks.value = tRes.data || { pendingLeaveCount: 0, lowAttendanceStudents: [] }
    announcements.value = aRes.data || []
    subtitle.value = `今天 ${schedule.value.weekDay || weekDay}，共 ${schedule.value.totalCourses} 节课`
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
/* ========================================
   BENTO GRID + GLASSMORPHISM 2.0 DASHBOARD
   ======================================== */
.bento-dashboard {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ====== Grid System ====== */
.bento-grid {
  display: grid;
  gap: 16px;
}

/* Row 1: Welcome (2fr) + Date (1fr) */
.bento-row-1,
.bento-grid:first-child {
  grid-template-columns: 2fr 1fr;
  grid-template-areas: "welcome date";
}

/* Row 2: 4 small stat cards + tall schedule */
.bento-row-2 {
  grid-template-columns: 1fr 1fr 1fr 1fr 2fr;
  grid-template-areas: "courses checkin leave warn schedule";
}

/* Row 3: Low attendance + Pending leave + Announcements */
.bento-row-3 {
  grid-template-columns: 2fr 1fr 1.5fr;
  grid-template-areas: "lowatt pendleave anno";
}

/* ====== Bento Card Base ====== */
.bento-card {
  background: var(--c-glass-bg);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  border-radius: 18px;
  padding: 20px 22px;
  position: relative;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.25, 0.1, 0.25, 1);
  cursor: default;
}

.bento-card:hover {
  transform: translateY(-2px);
  border-color: var(--c-glass-border-strong);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2), 0 0 0 1px var(--c-glass-border-strong);
}

/* ====== Welcome Card ====== */
.welcome-card {
  background: linear-gradient(135deg, rgba(0, 229, 255, 0.12) 0%, rgba(224, 64, 251, 0.08) 50%, rgba(0, 229, 255, 0.04) 100%);
  border: 1px solid rgba(0, 229, 255, 0.15);
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 140px;
}

.welcome-content {
  position: relative;
  z-index: 1;
}

.welcome-greeting {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.welcome-wave {
  font-size: 28px;
  animation: wave 2s ease-in-out infinite;
  display: inline-block;
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(15deg); }
  75% { transform: rotate(-10deg); }
}

.welcome-greeting h2 {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  color: var(--c-text);
  letter-spacing: -0.02em;
}

.welcome-sub {
  font-size: 13px;
  color: var(--c-text-secondary);
  margin: 0 0 12px 38px;
}

.welcome-tags {
  display: flex;
  gap: 8px;
  margin-left: 38px;
}

.welcome-tags .tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  background: rgba(0, 229, 255, 0.1);
  border: 1px solid rgba(0, 229, 255, 0.2);
  color: var(--c-accent);
  font-weight: 600;
}

/* Welcome visual — orbital rings */
.welcome-visual {
  position: relative;
  width: 100px;
  height: 100px;
  flex-shrink: 0;
}

.orbit-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  border-radius: 50%;
  border: 1.5px solid rgba(0, 229, 255, 0.18);
  transform: translate(-50%, -50%);
}

.ring-1 { width: 80px; height: 80px; animation: orbit 4s linear infinite; }
.ring-2 { width: 60px; height: 40px; border-color: rgba(224, 64, 251, 0.15); animation: orbit 3s linear infinite reverse; border-radius: 50%; transform: translate(-50%, -50%) rotateX(60deg); }
.ring-3 { width: 50px; height: 50px; border-color: rgba(255, 214, 0, 0.12); animation: orbit 5s linear infinite; }

@keyframes orbit {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

.orbit-core {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--c-accent);
  box-shadow: 0 0 16px rgba(0, 229, 255, 0.5);
  transform: translate(-50%, -50%);
}

/* ====== Date Card ====== */
.date-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 12px;
  text-align: center;
}

.date-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.date-day {
  font-size: 52px;
  font-weight: 900;
  line-height: 1;
  background: linear-gradient(135deg, var(--c-accent), var(--c-accent-2));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.date-weekday {
  font-size: 16px;
  font-weight: 700;
  color: var(--c-text);
  margin-top: 2px;
}

.date-full {
  font-size: 11px;
  color: var(--c-text-tertiary);
  font-weight: 500;
}

.date-divider {
  width: 40px;
  height: 2px;
  border-radius: 1px;
  background: var(--c-glass-border);
}

.date-stats {
  display: flex;
  gap: 24px;
}

.ds-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.ds-val {
  font-size: 18px;
  font-weight: 800;
  color: var(--c-text);
  font-family: var(--font-mono);
}

.ds-val.accent { color: var(--c-accent); }

.ds-lbl {
  font-size: 11px;
  color: var(--c-text-tertiary);
  margin-top: 2px;
}

/* ====== Stat Cards (small) ====== */
.stat-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  cursor: pointer;
  min-height: 130px;
}

.stat-card:hover {
  border-color: var(--c-accent-border);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.courses-icon { background: rgba(0, 229, 255, 0.1); color: var(--c-accent); }
.checkin-icon { background: rgba(0, 230, 118, 0.1); color: var(--c-success); }
.leave-icon { background: rgba(255, 214, 0, 0.1); color: var(--c-accent-3); }
.warn-icon { background: rgba(255, 23, 68, 0.1); color: var(--c-danger); }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-val {
  font-size: 32px;
  font-weight: 900;
  color: var(--c-text);
  line-height: 1.1;
  font-family: var(--font-mono);
}

.stat-val.green { color: var(--c-success); }
.stat-val.orange { color: var(--c-accent-3); }
.stat-val.red { color: var(--c-danger); }

.stat-lbl {
  font-size: 12px;
  color: var(--c-text-tertiary);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-top: 2px;
}

.stat-bar {
  height: 4px;
  border-radius: 2px;
  background: var(--c-border-light);
  overflow: hidden;
  margin-top: auto;
}

.stat-bar-fill {
  height: 100%;
  border-radius: 2px;
  background: var(--c-accent);
  transition: width 0.6s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.stat-bar-fill.green { background: var(--c-success); }

.stat-dot {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--c-accent-3);
}

.stat-dot.pulse { animation: pulse-dot 2s ease-in-out infinite; }
.stat-dot.red-dot { background: var(--c-danger); }

@keyframes pulse-dot {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 214, 0, 0.5); }
  50% { box-shadow: 0 0 0 8px rgba(255, 214, 0, 0); }
}

/* ====== Schedule Card (tall) ====== */
.schedule-card {
  grid-row: span 2;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-head-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--c-text);
}

.card-head-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 20px;
  background: var(--c-accent-bg);
  color: var(--c-accent);
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.schedule-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--c-border-light);
  transition: background 0.2s;
}

.schedule-item:last-child { border-bottom: none; }

.sch-time {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  min-width: 48px;
}

.sch-start { font-size: 14px; font-weight: 700; color: var(--c-text); font-family: var(--font-mono); }
.sch-end { font-size: 11px; color: var(--c-text-tertiary); font-family: var(--font-mono); }

.sch-dot-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  align-self: stretch;
}

.sch-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 4px;
  flex-shrink: 0;
}

.dot-0 { background: var(--c-accent); }
.dot-1 { background: var(--c-accent-2); }
.dot-2 { background: var(--c-accent-3); }
.dot-3 { background: var(--c-success); }

.sch-line {
  width: 1.5px;
  flex: 1;
  background: var(--c-border-light);
  margin-top: 4px;
}

.sch-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.sch-name { font-size: 14px; font-weight: 600; color: var(--c-text); }
.sch-meta { font-size: 12px; color: var(--c-text-tertiary); margin-top: 2px; }

.sch-checkin {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sch-progress-ring {
  width: 36px;
  height: 36px;
}

.sch-progress-ring svg { width: 100%; height: 100%; }

.progress-ring {
  transition: stroke-dashoffset 0.6s ease;
}

.sch-ratio {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-text-secondary);
  font-family: var(--font-mono);
  white-space: nowrap;
}

/* ====== Warning List Card ====== */
.warn-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.warn-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--c-bg-alt);
  border: 1px solid var(--c-border-light);
}

.warn-chip-info {
  flex: 1;
  min-width: 0;
}

.wci-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text);
  display: block;
  margin-bottom: 4px;
}

.wci-pct {
  font-size: 14px;
  font-weight: 700;
  font-family: var(--font-mono);
  color: var(--c-text-secondary);
}

.wci-pct.danger { color: var(--c-danger); }

/* ====== Pending Leave Card ====== */
.pending-count {
  font-size: 14px;
  font-weight: 700;
  color: var(--c-accent-3);
  font-family: var(--font-mono);
}

.pending-prompt {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 0;
  color: var(--c-accent-3);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.pending-prompt:hover {
  color: var(--c-accent);
}

/* ====== Announcement Card ====== */
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
  cursor: pointer;
  transition: padding-left 0.2s;
}

.anno-item:last-child { border-bottom: none; }
.anno-item:hover { padding-left: 4px; }

.a-title {
  flex: 1;
  font-size: 14px;
  color: var(--c-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.a-time {
  font-size: 11px;
  color: var(--c-text-tertiary);
  font-family: var(--font-mono);
}

/* ====== Empty State ====== */
.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px 0;
  color: var(--c-text-tertiary);
  font-size: 13px;
}

.empty-hint.small {
  padding: 20px 0;
  font-size: 12px;
}

/* ====== Responsive ====== */
@media (max-width: 1200px) {
  .bento-row-2 {
    grid-template-columns: 1fr 1fr 1fr 1fr;
    grid-template-areas:
      "courses checkin leave warn"
      "schedule schedule schedule schedule";
  }
  .bento-row-3 {
    grid-template-columns: 1fr 1fr;
    grid-template-areas:
      "lowatt pendleave"
      "anno anno";
  }
}

@media (max-width: 768px) {
  .bento-dashboard { gap: 10px; }
  .bento-grid { gap: 10px; }

  .bento-grid:first-child {
    grid-template-columns: 1fr;
    grid-template-areas: "welcome" "date";
  }
  .bento-row-2 {
    grid-template-columns: 1fr 1fr;
    grid-template-areas:
      "courses checkin"
      "leave warn"
      "schedule schedule";
  }
  .bento-row-3 {
    grid-template-columns: 1fr;
    grid-template-areas: "lowatt" "pendleave" "anno";
  }

  .bento-card { padding: 14px 16px; border-radius: 14px; }

  .welcome-card { min-height: auto; }
  .welcome-visual { display: none; }
  .welcome-greeting h2 { font-size: 17px; }
  .welcome-tags { margin-left: 0; flex-wrap: wrap; }
  .welcome-sub { margin-left: 0; }

  .date-day { font-size: 40px; }
  .stat-val { font-size: 24px; }

  .warn-grid { grid-template-columns: 1fr; }
}
</style>
