<template>
  <div class="home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>欢迎回来 👋</h2>
        <p>智课考勤 · 课堂考勤管理平台</p>
      </div>
      <div class="welcome-date">{{ today }}</div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card blue" @click="goToPage('/dashboard/student')">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ statistics.totalStudents }}</div>
            <div class="stat-label">学生总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card green" @click="goToPage('/dashboard/class')">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><School /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ statistics.totalClasses }}</div>
            <div class="stat-label">班级总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card orange" @click="goToPage('/dashboard/attendance/list')">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><Calendar /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ statistics.todayAttendance }}</div>
            <div class="stat-label">今日考勤</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card red" @click="goToPage('/dashboard/behavior/list')">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><Warning /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ statistics.unhandledBehaviors }}</div>
            <div class="stat-label">待处理行为</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 课堂质量评分卡片 -->
    <el-card shadow="never" class="section-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span><el-icon style="vertical-align: middle; margin-right: 6px"><DataAnalysis /></el-icon>课堂质量评估</span>
          <el-tag v-if="quality.score > 0" :type="quality.score >= 80 ? 'success' : quality.score >= 60 ? 'warning' : 'danger'" effect="dark" size="large">
            综合评分 {{ quality.score }} 分
          </el-tag>
        </div>
      </template>
      <div v-if="quality.score === 0" class="empty-hint">暂无足够数据生成课堂质量评分</div>
      <el-row v-else :gutter="20">
        <el-col :span="6">
          <div class="quality-item">
            <div class="q-value blue">{{ quality.attendanceScore }}</div>
            <div class="q-label">出勤率得分</div>
            <div class="q-weight">权重 30% · 出勤 {{ quality.presentCount }}/{{ quality.totalAttendance }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="quality-item">
            <div class="q-value green">{{ quality.handRaiseScore }}</div>
            <div class="q-label">举手率得分</div>
            <div class="q-weight">权重 25% · 课堂互动指标</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="quality-item">
            <div class="q-value orange">{{ quality.violationScore }}</div>
            <div class="q-label">违规反向得分</div>
            <div class="q-weight">权重 25% · 违规 {{ quality.violationBehaviors }} 次</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="quality-item">
            <div class="q-value purple">{{ quality.focusScore }}</div>
            <div class="q-label">专注度得分</div>
            <div class="q-weight">权重 20% · 积极行为 {{ quality.positiveBehaviors }} 次</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 下半部分 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 快捷操作 -->
      <el-col :span="12">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header"><span>快捷操作</span></div>
          </template>
          <div class="quick-grid">
            <div class="quick-item" @click="goToPage('/dashboard/qrcheckin')">
              <div class="quick-icon-wrap c-green">
                <el-icon :size="22"><Iphone /></el-icon>
              </div>
              <span>二维码签到</span>
            </div>
            <div class="quick-item" @click="goToPage('/dashboard/class')">
              <div class="quick-icon-wrap c-orange">
                <el-icon :size="22"><Plus /></el-icon>
              </div>
              <span>添加班级</span>
            </div>
            <div class="quick-item" @click="goToPage('/dashboard/student')">
              <div class="quick-icon-wrap c-purple">
                <el-icon :size="22"><User /></el-icon>
              </div>
              <span>添加学生</span>
            </div>
            <div class="quick-item" @click="goToPage('/dashboard/statistics')">
              <div class="quick-icon-wrap c-teal">
                <el-icon :size="22"><DataAnalysis /></el-icon>
              </div>
              <span>数据统计</span>
            </div>
            <div class="quick-item" @click="goToPage('/dashboard/import')">
              <div class="quick-icon-wrap c-red">
                <el-icon :size="22"><Upload /></el-icon>
              </div>
              <span>批量导入</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 最近考勤 -->
      <el-col :span="12">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span>最近考勤记录</span>
              <el-button link type="primary" @click="goToPage('/dashboard/attendance/list')">全部</el-button>
            </div>
          </template>
          <div v-if="recentAttendances.length === 0" class="empty-hint">暂无考勤记录</div>
          <div v-else class="recent-list">
            <div v-for="item in recentAttendances" :key="item.id" class="recent-item">
              <div class="recent-info">
                <text class="recent-name">{{ item.studentName || '学生#' + item.studentId }}</text>
                <text class="recent-class">{{ item.className || '' }}</text>
              </div>
              <el-tag :type="getStatusType(item.status)" size="small" effect="plain">
                {{ getStatusText(item.status) }}
              </el-tag>
              <span class="recent-time">{{ item.checkInTime || item.attendanceDate }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { User, School, Calendar, Warning, Plus, View, Iphone, DataAnalysis, Upload } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/statistics'
import { getAttendanceList } from '@/api/attendance'

const router = useRouter()
const today = ref('')

const statistics = ref({
  totalStudents: 0,
  totalClasses: 0,
  todayAttendance: 0,
  unhandledBehaviors: 0
})
const recentAttendances = ref([])
const quality = ref({
  score: 0, overallScore: 0, attendanceScore: 0, handRaiseScore: 0,
  violationScore: 0, focusScore: 0, totalAttendance: 0, presentCount: 0,
  totalBehaviors: 0, positiveBehaviors: 0, violationBehaviors: 0
})

onMounted(() => {
  const d = new Date()
  const w = ['日','一','二','三','四','五','六']
  today.value = `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${w[d.getDay()]}`
  loadStatistics()
  loadRecentAttendances()
  loadClassQuality()
})

const loadStatistics = async () => {
  try {
    const res = await getDashboardStats()
    const data = res.data
    statistics.value = {
      totalStudents: data.studentCount || 0,
      totalClasses: data.classCount || data.courseCount || 0,
      todayAttendance: data.todayAttendance || 0,
      unhandledBehaviors: data.unhandledBehavior || 0
    }
  } catch (e) { console.error(e) }
}

const loadRecentAttendances = async () => {
  try {
    const res = await getAttendanceList({ page: 1, size: 10 })
    recentAttendances.value = (res.data?.records || []).slice(0, 5)
  } catch (e) { console.error(e) }
}

const goToPage = (path) => router.push(path)

const loadClassQuality = async () => {
  try {
    const res = await request.get('/statistics/class-quality')
    if (res.data) {
      const d = res.data
      quality.value = {
        ...d,
        score: d.overallScore || 0
      }
    }
  } catch (e) { console.error(e) }
}

const getStatusType = (s) => ({ present:'success', absent:'danger', late:'warning', leave:'info' }[s] || 'info')
const getStatusText = (s) => ({ present:'出勤', absent:'缺勤', late:'迟到', leave:'请假' }[s] || s)
</script>

<style scoped>
.home { padding: 20px; }

.welcome-banner {
  display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  border-radius: 16px; padding: 28px 32px; margin-bottom: 24px;
  color: #fff; box-shadow: var(--shadow-glow);
}
.welcome-text h2 { margin: 0 0 4px; font-size: 22px; font-weight: 600; }
.welcome-text p { margin: 0; font-size: 13px; opacity: 0.85; }
.welcome-date { font-size: 14px; opacity: 0.9; }

/* 统计卡片 */
.stats-row { margin-bottom: 4px; }
.stat-card {
  display: flex; align-items: center; gap: 16px;
  padding: 20px; border-radius: 14px; cursor: pointer;
  background: var(--c-glass-bg);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  box-shadow: var(--c-glass-shadow);
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
}
.stat-card:hover {
  transform: translateY(-4px) rotateX(2deg);
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
  border-color: var(--c-primary);
}
.stat-icon-wrap {
  width: 56px; height: 56px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
}
.stat-card.blue .stat-icon-wrap { background: var(--c-primary-bg); color: var(--c-primary); }
.stat-card.green .stat-icon-wrap { background: var(--c-success-bg); color: var(--c-success); }
.stat-card.orange .stat-icon-wrap { background: var(--c-warning-bg); color: var(--c-warning); }
.stat-card.red .stat-icon-wrap { background: var(--c-danger-bg); color: var(--c-danger); }
.stat-value { font-size: 28px; font-weight: 700; color: var(--c-text); line-height: 1.2; }
.stat-label { font-size: 13px; color: var(--c-text-tertiary); margin-top: 2px; }

/* 卡片 */
.section-card {
  border-radius: 14px; border: 1px solid var(--c-glass-border);
  background: var(--c-glass-bg);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  box-shadow: var(--c-glass-shadow);
  transition: all 0.3s ease;
}
.section-card:hover {
  box-shadow: 0 4px 20px var(--c-shadow);
  border-color: var(--c-primary);
}
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header span { font-size: 16px; font-weight: 600; color: var(--c-text); }

/* 快捷操作网格 */
.quick-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px;
}
.quick-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 16px 8px; border-radius: 12px; cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  background: var(--c-glass-bg);
  backdrop-filter: blur(8px) saturate(180%);
  -webkit-backdrop-filter: blur(8px) saturate(180%);
  border: 1px solid transparent;
}
.quick-item:hover {
  background: var(--c-primary-bg);
  border-color: var(--c-primary);
  transform: translateY(-3px);
  box-shadow: 0 4px 12px var(--c-shadow);
}
.quick-item span { font-size: 13px; color: var(--c-text-secondary); font-weight: 500; }
.quick-icon-wrap {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}
.c-blue { background: var(--c-primary-bg); color: var(--c-primary); }
.c-green { background: var(--c-success-bg); color: var(--c-success); }
.c-orange { background: var(--c-warning-bg); color: var(--c-warning); }
.c-purple { background: rgba(179,127,235,0.12); color: var(--c-purple, #b37feb); }
.c-teal { background: rgba(56,178,172,0.1); color: #38b2ac; }
.c-red { background: var(--c-danger-bg); color: var(--c-danger); }

/* 最近考勤列表 */
.empty-hint { text-align: center; padding: 40px 0; color: var(--c-text-secondary); font-size: 14px; }
.recent-list { display: flex; flex-direction: column; }
.recent-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 0; border-bottom: 1px solid var(--c-border-light);
}
.recent-item:last-child { border-bottom: none; }
.recent-info { flex: 1; }
.recent-name { font-size: 14px; font-weight: 500; color: var(--c-text); display: block; }
.recent-class { font-size: 12px; color: var(--c-text-tertiary); display: block; margin-top: 2px; }
.recent-time { font-size: 12px; color: var(--c-text-tertiary); }

/* 课堂质量评分卡片 - glass morphism */
.quality-item {
  text-align: center; padding: 16px 8px;
  background: var(--c-glass-bg);
  backdrop-filter: blur(8px) saturate(180%);
  -webkit-backdrop-filter: blur(8px) saturate(180%);
  border-radius: 12px;
  border: 1px solid var(--c-glass-border);
  transition: all 0.3s ease;
}
.quality-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--c-shadow);
}
.q-value { font-size: 30px; font-weight: 700; line-height: 1.3; }
.q-value.blue { color: var(--c-primary); }
.q-value.green { color: var(--c-green, #52c41a); }
.q-value.orange { color: var(--c-orange, #fa8c16); }
.q-value.purple { color: var(--c-purple, #b37feb); }
.q-label { font-size: 14px; color: var(--c-text-secondary); margin-top: 4px; }
.q-weight { font-size: 11px; color: var(--c-text); opacity: 0.7; margin-top: 2px; }
</style>
