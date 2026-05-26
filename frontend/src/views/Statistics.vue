<template>
  <div class="statistics">
    <div style="display: flex; justify-content: flex-end; margin-bottom: 16px;">
      <el-button type="danger" @click="handleExportPdf">
        <el-icon><Document /></el-icon>
        导出PDF报表
      </el-button>
    </div>
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value data-readout">{{ statistics.totalStudents }}</div>
              <div class="stat-label">学生总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, var(--c-success), #2db84d);">
              <el-icon :size="40"><School /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalClasses }}</div>
              <div class="stat-label">班级总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, var(--c-warning), #d48806);">
              <el-icon :size="40"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.todayAttendance }}</div>
              <div class="stat-label">今日考勤</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, var(--c-danger), #d32f2f);">
              <el-icon :size="40"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.unhandledBehaviors }}</div>
              <div class="stat-label">待处理行为</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 课堂质量评分卡片 -->
    <el-card shadow="never" class="sci-fi-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span><el-icon style="vertical-align: middle; margin-right: 6px"><DataAnalysis /></el-icon>课堂质量评估</span>
          <el-tag v-if="quality.score > 0" :type="quality.score >= 80 ? 'success' : quality.score >= 60 ? 'warning' : 'danger'" size="large">
            综合评分 {{ quality.score }} 分
          </el-tag>
        </div>
      </template>
      <div v-if="quality.score === 0" style="text-align:center;padding:30px 0;color:var(--c-text-tertiary);">暂无足够数据生成课堂质量评分</div>
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

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="sci-fi-card">
          <template #header>
            <div class="card-header">
              <span>本周出勤趋势</span>
              <el-button-group>
                <el-button size="small" @click="refreshWeekChart">刷新</el-button>
              </el-button-group>
            </div>
          </template>
          <div ref="weekChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="sci-fi-card">
          <template #header>
            <div class="card-header">
              <span>班级出勤率排行 · 3D</span>
              <el-button-group>
                <el-button size="small" @click="refreshClassChart">刷新</el-button>
              </el-button-group>
            </div>
          </template>
          <div ref="classChartRef" style="height: 340px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="sci-fi-card">
          <template #header>
            <div class="card-header">
              <span>行为类型分布</span>
              <el-button-group>
                <el-button size="small" @click="refreshBehaviorChart">刷新</el-button>
              </el-button-group>
            </div>
          </template>
          <div ref="behaviorChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="sci-fi-card">
          <template #header>
            <div class="card-header">
              <span>学生出勤-行为 3D 关联</span>
              <el-button-group>
                <el-button size="small" @click="refreshScatter3D">刷新</el-button>
              </el-button-group>
            </div>
          </template>
          <div ref="scatterChartRef" style="height: 340px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="sci-fi-card">
          <template #header>
            <div class="card-header">
              <span>最近考勤记录</span>
              <el-button link type="primary" @click="goToAttendance">查看更多</el-button>
            </div>
          </template>
          <el-table :data="recentAttendances" style="width: 100%" max-height="300">
            <el-table-column prop="studentName" label="学生姓名" width="120" />
            <el-table-column prop="className" label="班级" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="checkInTime" label="签到时间" />
          </el-table>
        </el-card>
      </el-col>

    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import 'echarts-gl'
import { User, School, Calendar, Warning, Document, DataAnalysis } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useTheme } from '@/composables/useTheme'

const router = useRouter()
const { isDark } = useTheme()

const statistics = ref({
  totalStudents: 0,
  totalClasses: 0,
  todayAttendance: 0,
  unhandledBehaviors: 0
})

const weekChartRef = ref(null)
const classChartRef = ref(null)
const behaviorChartRef = ref(null)
const scatterChartRef = ref(null)

const recentAttendances = ref([])
const quality = ref({
  score: 0, overallScore: 0, attendanceScore: 0, handRaiseScore: 0,
  violationScore: 0, focusScore: 0, totalAttendance: 0, presentCount: 0,
  totalBehaviors: 0, positiveBehaviors: 0, violationBehaviors: 0
})

let weekChart = null
let classChart = null
let behaviorChart = null
let scatterChart = null

onMounted(() => {
  loadStatistics()
  loadRecentAttendances()
  loadClassQuality()
  nextTick(() => {
    initCharts()
  })
})

// 监听主题变化重建图表
watch(isDark, () => {
  // 销毁旧实例
  if (weekChart) weekChart.dispose()
  if (classChart) classChart.dispose()
  if (behaviorChart) behaviorChart.dispose()
  if (scatterChart) scatterChart.dispose()
  nextTick(() => initCharts())
})

const handleExportPdf = () => {
  const token = localStorage.getItem('token')
  const url = `/api/export/pdf/attendance?token=${token}`
  window.open(url, '_blank')
}

const loadClassQuality = async () => {
  try {
    const res = await request.get('/statistics/class-quality')
    if (res.data) {
      quality.value = { ...res.data, score: res.data.overallScore || 0 }
    }
  } catch (e) { console.error(e) }
}

const loadStatistics = async () => {
  try {
    const res = await request.get('/statistics/dashboard')
    const d = res.data || {}
    statistics.value = {
      totalStudents: d.studentCount || 0,
      totalClasses: d.classCount || 0,
      todayAttendance: d.todayAttendance || 0,
      unhandledBehaviors: d.unhandledBehavior || 0
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const loadRecentAttendances = async () => {
  try {
    const res = await request.get('/attendance/list', {
      params: { pageNum: 1, pageSize: 5 }
    })
    recentAttendances.value = res.data.records || []
  } catch (error) {
    console.error('加载最近考勤记录失败', error)
  }
}

const initCharts = () => {
  initWeekChart()
  initClassChart3D()
  initBehaviorChart()
  initScatter3D()
}

const initWeekChart = async () => {
  if (weekChartRef.value) {
    weekChart = echarts.init(weekChartRef.value, isDark.value ? 'dark' : undefined)

    try {
      const today = new Date()
      const weekStart = new Date(today)
      weekStart.setDate(today.getDate() - today.getDay() + 1)
      const weekEnd = new Date(weekStart)
      weekEnd.setDate(weekStart.getDate() + 4)

      const startDate = weekStart.toISOString().split('T')[0]
      const endDate = weekEnd.toISOString().split('T')[0]

      const [statsRes, studentRes] = await Promise.all([
        request.get('/statistics/attendance', { params: { startDate, endDate } }),
        request.get('/student/all')
      ])

      const stats = statsRes.data || {}
      const totalStudents = studentRes.data?.length || 1
      const daily = stats.daily || {}

      const dayLabels = ['周一', '周二', '周三', '周四', '周五']
      const percentages = dayLabels.map((_, i) => {
        const date = new Date(weekStart)
        date.setDate(weekStart.getDate() + i)
        const key = date.toISOString().split('T')[0]
        const count = daily[key] || 0
        return Math.round((count / totalStudents) * 100)
      })

      const option = {
        tooltip: { trigger: 'axis', formatter: '{b}: {c}%' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: dayLabels, boundaryGap: false },
        yAxis: { type: 'value', name: '出勤率(%)', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
        series: [{
          name: '出勤率', data: percentages, type: 'line', smooth: true,
          symbol: 'circle', symbolSize: 8,
          itemStyle: { color: '#409eff' }, lineStyle: { width: 3 },
          areaStyle: {
            color: {
              type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
              ]
            }
          }
        }]
      }
      weekChart.setOption(option)
    } catch (error) {
      console.error('加载周趋势数据失败', error)
      weekChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五'] },
        yAxis: { type: 'value', name: '出勤率(%)' },
        series: [{ data: [95, 92, 88, 96, 94], type: 'line', smooth: true, itemStyle: { color: '#409eff' } }]
      })
    }
  }
}

const initClassChart3D = async () => {
  if (classChartRef.value) {
    classChart = echarts.init(classChartRef.value, isDark.value ? 'dark' : undefined)

    try {
      const [rankingRes, classRes] = await Promise.all([
        request.get('/statistics/ranking'),
        request.get('/class/all')
      ])

      const ranking = rankingRes.data?.ranking || []
      const classes = classRes.data || []
      const classNameMap = {}
      classes.forEach(c => { classNameMap[c.id] = c.className })

      const classNames = []
      const attendanceRates = []
      ranking.slice(0, 10).forEach(item => {
        classNames.push(classNameMap[item.studentId] || `班级${item.studentId}`)
        attendanceRates.push(item.rate || 0)
      })

      const data3D = classNames.map((name, i) => ({
        value: [i, attendanceRates[i], attendanceRates[i]],
        name: name
      }))

      const option = {
        tooltip: { formatter: (params) => `${params.name}: ${params.value[1]}%` },
        visualMap: {
          show: false,
          min: 0,
          max: 100,
          inRange: { color: ['#FF3B30', '#FF9500', '#007AFF', '#34C759'] }
        },
        grid3D: {
          boxWidth: 70,
          boxDepth: 40,
          viewControl: {
            autoRotate: true,
            autoRotateSpeed: 6,
            distance: 160,
            alpha: 25,
            beta: 30
          },
          light: {
            main: { intensity: 1.2, shadow: true },
            ambient: { intensity: 0.5 }
          }
        },
        xAxis3D: {
          type: 'category',
          data: classNames,
          axisLabel: { interval: 0, fontSize: 10 }
        },
        yAxis3D: { type: 'value', name: '出勤率%', max: 100 },
        zAxis3D: { type: 'value', name: '' },
        series: [{
          type: 'bar3D',
          data: data3D,
          shading: 'lambert',
          barSize: 0.6,
          label: { show: true, formatter: (p) => p.value[1] + '%', fontSize: 10, distance: 2 }
        }]
      }
      classChart.setOption(option)
    } catch (error) {
      console.error('加载班级3D数据失败', error)
      classChart.setOption({
        grid3D: { boxWidth: 60, boxDepth: 30, viewControl: { autoRotate: true } },
        xAxis3D: { type: 'category', data: ['1班','2班','3班','4班'] },
        yAxis3D: { type: 'value' },
        zAxis3D: { type: 'value' },
        series: [{ type: 'bar3D', data: [
          { value: [0, 95, 95] }, { value: [1, 88, 88] },
          { value: [2, 92, 92] }, { value: [3, 96, 96] }
        ], shading: 'lambert' }]
      })
    }
  }
}

const initBehaviorChart = async () => {
  if (behaviorChartRef.value) {
    behaviorChart = echarts.init(behaviorChartRef.value, isDark.value ? 'dark' : undefined)
    
    try {
      const behaviorRes = await request.get('/behavior/list', {
        params: { pageNum: 1, pageSize: 200 }
      })

      const behaviors = behaviorRes.data?.records || []

      const behaviorCount = {}
      behaviors.forEach(b => {
        const type = b.behaviorType
        behaviorCount[type] = (behaviorCount[type] || 0) + 1
      })

      const pieData = Object.entries(behaviorCount).map(([type, count]) => {
        const colorMap = {
          raising_hand: '#67c23a', reading: '#409eff', writing: '#909399',
          using_phone: '#e6a23c', bowing_head: '#f56c6c', leaning_over: '#e040fb',
          sleeping: '#f56c6c', phone: '#e6a23c', eating: '#909399',
          talking: '#409eff', absent: '#67c23a'
        }

        const nameMap = {
          raising_hand: '举手', reading: '阅读', writing: '书写',
          using_phone: '玩手机', bowing_head: '低头', leaning_over: '趴桌',
          sleeping: '睡觉', phone: '玩手机', eating: '吃东西',
          talking: '讲话', absent: '缺勤'
        }
        
        return {
          value: count,
          name: nameMap[type] || type,
          itemStyle: { color: colorMap[type] || '#909399' }
        }
      })
      
      // 如果没有数据，显示提示
      if (pieData.length === 0) {
        pieData.push({
          value: 1,
          name: '暂无数据',
          itemStyle: { color: '#dcdfe6' }
        })
      }
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle'
        },
        series: [{
          name: '行为类型',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}: {d}%'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            },
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          data: pieData
        }]
      }
      behaviorChart.setOption(option)
    } catch (error) {
      console.error('加载行为数据失败', error)
      // 使用默认数据
      const option = {
        tooltip: { trigger: 'item' },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          type: 'pie',
          radius: '60%',
          data: [
            { value: 15, name: '睡觉', itemStyle: { color: '#f56c6c' } },
            { value: 25, name: '玩手机', itemStyle: { color: '#e6a23c' } },
            { value: 10, name: '吃东西', itemStyle: { color: '#909399' } },
            { value: 30, name: '讲话', itemStyle: { color: '#409eff' } },
            { value: 8, name: '离开座位', itemStyle: { color: '#67c23a' } }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      behaviorChart.setOption(option)
    }
  }
}

// --- 3D 散点图: 学生出勤-行为关联 ---
const initScatter3D = async () => {
  if (!scatterChartRef.value) return
  scatterChart = echarts.init(scatterChartRef.value, isDark.value ? 'dark' : undefined)

  try {
    const [attendRes, behaviorRes] = await Promise.all([
      request.get('/attendance/list', { params: { pageNum: 1, pageSize: 500 } }),
      request.get('/behavior/list', { params: { pageNum: 1, pageSize: 200 } })
    ])

    const attendances = attendRes.data?.records || []
    const behaviors = behaviorRes.data?.records || []

    // Aggregate per student
    const studentData = {}
    attendances.forEach(a => {
      const sid = a.studentId || a.studentName
      if (!studentData[sid]) studentData[sid] = { present: 0, late: 0, absent: 0, behavior: 0, name: a.studentName }
      const s = studentData[sid]
      if (a.status === 'present') s.present++
      else if (a.status === 'late') s.late++
      else if (a.status === 'absent') s.absent++
    })
    behaviors.forEach(b => {
      const sid = b.studentId
      if (studentData[sid]) studentData[sid].behavior++
    })

    const scatterData = Object.entries(studentData)
      .filter(([s, d]) => d.present + d.late + d.absent > 0)
      .map(([s, d]) => {
        const total = d.present + d.late + d.absent
        const rate = Math.round((d.present / total) * 100)
        return { value: [rate, d.behavior, d.late], name: d.name || `学生#${s}` }
      })
      .slice(0, 50)

    const option = {
      tooltip: { formatter: (p) => `${p.name}<br/>出勤率: ${p.value[0]}%<br/>异常行为: ${p.value[1]}<br/>迟到: ${p.value[2]}` },
      grid3D: {
        boxWidth: 80, boxDepth: 60,
        viewControl: { autoRotate: true, autoRotateSpeed: 4, distance: 140 },
        light: { main: { intensity: 1.0 }, ambient: { intensity: 0.6 } }
      },
      xAxis3D: { type: 'value', name: '出勤率%', min: 0, max: 100 },
      yAxis3D: { type: 'value', name: '异常行为' },
      zAxis3D: { type: 'value', name: '迟到次数' },
      visualMap: {
        show: false, min: 0, max: 100,
        inRange: { color: ['#34C759', '#007AFF', '#FF9500', '#FF3B30'] }
      },
      series: [{
        type: 'scatter3D',
        data: scatterData,
        symbolSize: 6,
        itemStyle: { borderWidth: 0.5, borderColor: 'rgba(255,255,255,0.3)' }
      }]
    }
    scatterChart.setOption(option)
  } catch (e) {
    console.error('3D散点加载失败', e)
    scatterChart.setOption({
      grid3D: { boxWidth: 60, boxDepth: 40, viewControl: { autoRotate: true } },
      xAxis3D: { type: 'value', name: '出勤率' },
      yAxis3D: { type: 'value', name: '行为' },
      zAxis3D: { type: 'value', name: '迟到' },
      series: [{ type: 'scatter3D', data: [[95,2,0],[88,4,2],[72,8,5],[96,1,0],[85,3,1]], symbolSize: 6 }]
    })
  }
}

const refreshScatter3D = () => { initScatter3D() }

const refreshWeekChart = () => {
  ElMessage.success('数据已刷新')
  initWeekChart()
}

const refreshClassChart = () => {
  ElMessage.success('数据已刷新')
  initClassChart3D()
}

const refreshBehaviorChart = () => {
  ElMessage.success('数据已刷新')
  initBehaviorChart()
}

const goToAttendance = () => {
  router.push('/dashboard/attendance/list')
}

const getStatusType = (status) => {
  const typeMap = {
    present: 'success',
    absent: 'danger',
    late: 'warning',
    leave: 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    present: '出勤',
    absent: '缺勤',
    late: '迟到',
    leave: '请假'
  }
  return textMap[status] || status
}
</script>

<style scoped>
.statistics {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
  background: var(--c-glass-bg);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--c-glass-shadow);
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
}
.stat-card:hover {
  transform: translateY(-4px) rotateX(2deg);
  box-shadow: 0 8px 30px var(--c-shadow);
  border-color: var(--c-primary);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark)) !important;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--c-text);
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: var(--c-text-tertiary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header span {
  color: var(--c-text);
  font-weight: 600;
}

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
.q-weight { font-size: 11px; color: var(--c-text-tertiary); margin-top: 2px; }
</style>
