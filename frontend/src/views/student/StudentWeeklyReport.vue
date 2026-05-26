<template>
  <div class="weekly-report">
    <el-card class="report-header">
      <template #header>
        <div class="card-header">
          <span><el-icon style="vertical-align: middle; margin-right: 6px"><DataAnalysis /></el-icon>我的课堂周报</span>
          <el-tag type="info">{{ report.weekStart }} ~ {{ report.weekEnd }}</el-tag>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value blue">{{ report.attendanceRate }}%</div>
            <div class="stat-label">本周出勤率</div>
            <div class="stat-sub">出勤 {{ report.presentCount }} / 总课 {{ report.totalClasses }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value green">{{ report.handRaiseCount }}</div>
            <div class="stat-label">本周举手次数</div>
            <div class="stat-sub">课堂参与度指标</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value orange">{{ report.violationCount }}</div>
            <div class="stat-label">违规行为次数</div>
            <div class="stat-sub">含玩手机、低头、趴桌</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value purple">{{ report.totalClasses }}</div>
            <div class="stat-label">本周课程总数</div>
            <div class="stat-sub">缺勤 {{ report.absentCount }} 次</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="14">
        <el-card>
          <template #header><span>课堂参与度趋势</span></template>
          <div ref="trendChartRef" style="height: 240px" />
          <div v-if="report.trend?.every(t => t.count === 0)" class="chart-empty">本周暂无举手记录</div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header><span>违规行为明细</span></template>
          <div class="violation-list">
            <div class="v-item">
              <span class="v-icon">📱</span>
              <span class="v-label">玩手机</span>
              <span class="v-count">{{ report.violationDetails?.using_phone || 0 }} 次</span>
            </div>
            <div class="v-item">
              <span class="v-icon">😴</span>
              <span class="v-label">低头</span>
              <span class="v-count">{{ report.violationDetails?.bowing_head || 0 }} 次</span>
            </div>
            <div class="v-item">
              <span class="v-icon">🪟</span>
              <span class="v-label">趴桌</span>
              <span class="v-count">{{ report.violationDetails?.leaning_over || 0 }} 次</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <template #header>
        <span><el-icon style="vertical-align: middle; margin-right: 4px"><MagicStick /></el-icon>AI 学习建议</span>
      </template>
      <div class="suggestion-box">
        <div class="suggestion-text">{{ report.suggestion }}</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis, MagicStick } from '@element-plus/icons-vue'
import request from '@/utils/request'
import * as echarts from 'echarts'
import { useTheme } from '@/composables/useTheme'

const { isDark } = useTheme()

const report = ref({
  attendanceRate: 0, totalClasses: 0, presentCount: 0, lateCount: 0, absentCount: 0,
  handRaiseCount: 0, violationCount: 0, violationDetails: {},
  trend: [], suggestion: '加载中...', weekStart: '', weekEnd: ''
})
const trendChartRef = ref(null)

onMounted(async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const studentId = userInfo.studentId
  if (!studentId) { ElMessage.warning('无法获取学生信息'); return }

  try {
    const res = await request.get(`/statistics/weekly-report/${studentId}`)
    report.value = res.data || report.value
    await nextTick()
    renderChart()
  } catch (e) {
    console.error('加载周报失败', e)
    ElMessage.error('加载周报失败')
  }
})

function renderChart() {
  if (!trendChartRef.value || !report.value.trend?.length) return
  const chart = echarts.init(trendChartRef.value, isDark.value ? 'dark' : undefined)
  const dates = report.value.trend.map(t => {
    const d = t.date.split('-')
    return `${d[1]}/${d[2]}`
  })
  const counts = report.value.trend.map(t => t.count)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', minInterval: 1, name: '举手次数' },
    series: [{
      type: 'line',
      data: counts,
      smooth: true,
      lineStyle: { color: '#409eff', width: 3 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.3)' },
          { offset: 1, color: 'rgba(64,158,255,0.02)' }
        ])
      },
      itemStyle: { color: '#409eff' },
      symbol: 'circle',
      symbolSize: 6
    }]
  })
}
</script>

<style scoped>
.weekly-report { max-width: 1000px; margin: 0 auto; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header span { color: var(--c-text); font-weight: 600; }

.stat-card {
  background: var(--c-card);
  border: 1px solid var(--c-glass-border);
  border-radius: 12px; padding: 20px;
  text-align: center; box-shadow: var(--c-glass-shadow);
  transition: all 0.3s ease;
}
.stat-card:hover {
  transform: translateY(-4px) rotateX(2deg);
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
}
.stat-value { font-size: 32px; font-weight: 700; color: var(--c-text); }
.stat-value.blue { color: var(--c-primary); }
.stat-value.green { color: var(--c-success); }
.stat-value.orange { color: var(--c-warning); }
.stat-value.purple { color: var(--c-purple, #b37feb); }
.stat-label { font-size: 14px; color: var(--c-text-secondary); margin-top: 4px; }
.stat-sub { font-size: 11px; color: var(--c-text-tertiary); margin-top: 2px; }

.chart-empty { text-align: center; padding: 40px 0; color: var(--c-text-tertiary); font-size: 13px; }

.violation-list { display: flex; flex-direction: column; gap: 8px; }
.v-item {
  display: flex; align-items: center; padding: 12px;
  background: var(--c-fill-color, var(--c-bg-alt)); border-radius: 8px;
}
.v-icon { font-size: 20px; margin-right: 10px; }
.v-label { flex: 1; font-size: 14px; color: var(--c-text); }
.v-count { font-size: 14px; font-weight: 600; color: var(--c-warning); }

.suggestion-box {
  background: linear-gradient(135deg, var(--c-primary-bg), rgba(0,122,255,0.05));
  border-radius: 12px; padding: 20px; border: 1px solid var(--c-primary);
}
.suggestion-text {
  font-size: 15px; color: var(--c-text); line-height: 1.8;
  white-space: pre-wrap;
}
</style>
