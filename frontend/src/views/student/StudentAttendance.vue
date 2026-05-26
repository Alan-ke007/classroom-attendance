<template>
  <div class="student-attendance">
    <el-card class="stats-card">
      <el-row :gutter="16">
        <el-col :span="6" v-for="s in statsItems" :key="s.label">
          <div class="stat-item" :style="{ borderLeftColor: s.color }">
            <div class="stat-value" :style="{ color: s.color }">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card style="margin-top: 16px;">
      <template #header>
        <div class="filter-bar">
          <span>考勤记录</span>
          <div>
            <el-select v-model="statusFilter" placeholder="考勤状态" clearable style="width: 130px; margin-right: 8px;">
              <el-option label="全部" value="" />
              <el-option label="已出勤" value="present" />
              <el-option label="缺勤" value="absent" />
              <el-option label="迟到" value="late" />
              <el-option label="请假" value="leave" />
            </el-select>
            <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px; margin-right: 8px;" />
            <el-button type="primary" @click="loadData">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="attendanceList" v-loading="loading" stripe>
        <el-table-column prop="attendanceDate" label="日期" width="120" />
        <el-table-column label="课程" min-width="150">
          <template #default="{ row }">
            {{ row.courseName || courseMap[row.courseId] || '未知课程' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="签到时间" width="170" />
        <el-table-column prop="confidence" label="置信度" width="90">
          <template #default="{ row }">
            {{ row.confidence ? (row.confidence + '%') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAttendanceList, getCourseList, getAttendanceStats } from '@/api/student'

const loading = ref(false)
const attendanceList = ref([])
const total = ref(0)
const statusFilter = ref('')
const dateRange = ref(null)
const courseMap = ref({})

const statsItems = ref([
  { label: '总考勤', value: 0, color: '#409EFF' },
  { label: '已出勤', value: 0, color: '#67C23A' },
  { label: '缺勤', value: 0, color: '#F56C6C' },
  { label: '迟到', value: 0, color: '#E6A23C' }
])

const query = reactive({ pageNum: 1, pageSize: 10 })

function statusType(s) {
  const map = { present: 'success', absent: 'danger', late: 'warning', leave: 'info' }
  return map[s] || 'info'
}
function statusLabel(s) {
  const map = { present: '已出勤', absent: '缺勤', late: '迟到', leave: '请假' }
  return map[s] || s
}

async function loadData() {
  loading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
    if (statusFilter.value) params.status = statusFilter.value
    if (dateRange.value) {
      params.startDate = dateRange.value[0].toISOString().split('T')[0]
      params.endDate = dateRange.value[1].toISOString().split('T')[0]
    }
    if (userInfo.userId) params.studentId = userInfo.userId

    const body = await getAttendanceList(params)
    attendanceList.value = body.data?.records || []
    total.value = body.data?.total || 0

    // 加载课程名称
    const cRes = await getCourseList({ pageNum: 1, pageSize: 100 })
    const cData = cRes.data?.records || []
    cData.forEach(c => { courseMap.value[c.id] = c.courseName })

    // 更新统计
    const sBody = await getAttendanceStats()
    if (sBody.data) {
      const d = sBody.data
      statsItems.value = [
        { label: '总考勤', value: d.total || attendanceList.value.length, color: '#409EFF' },
        { label: '已出勤', value: d.present || 0, color: '#67C23A' },
        { label: '缺勤', value: d.absent || 0, color: '#F56C6C' },
        { label: '迟到', value: d.late || 0, color: '#E6A23C' }
      ]
    }
  } catch (e) {
    console.error('加载考勤记录失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.stat-item {
  border-left: 4px solid var(--c-primary);
  padding: 12px 16px;
  background: var(--c-fill-color, var(--c-bg-alt));
  border-radius: 4px;
}
.stat-value { font-size: 28px; font-weight: 700; color: var(--c-text); }
.stat-label { font-size: 13px; color: var(--c-text-tertiary); margin-top: 4px; }
</style>
