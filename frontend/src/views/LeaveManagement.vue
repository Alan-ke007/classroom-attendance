<template>
  <div class="leave-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>请假管理</span>
          <el-radio-group v-model="filterStatus" size="small" @change="loadData">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="pending">待审批</el-radio-button>
            <el-radio-button value="approved">已通过</el-radio-button>
            <el-radio-button value="rejected">已驳回</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="请假类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.leaveType === 'sick' ? 'danger' : 'info'" size="small">
              {{ row.leaveType === 'sick' ? '病假' : '事假' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学生" width="150">
          <template #default="{ row }">
            <span>{{ row.studentName || '学生#' + row.studentId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间范围" width="220">
          <template #default="{ row }">
            <span>{{ row.startDate }} 至 {{ row.endDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="事由" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approveRemark" label="审批意见" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button type="success" size="small" @click="approve(row)">通过</el-button>
              <el-button type="danger" size="small" @click="reject(row)">驳回</el-button>
            </template>
            <span v-else style="color: var(--c-text-tertiary);">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0" v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filterStatus = ref('pending')

function statusTag(s) {
  return { pending: 'warning', approved: 'success', rejected: 'danger' }[s] || 'info'
}
function statusLabel(s) {
  return { pending: '待审批', approved: '已通过', rejected: '已驳回' }[s] || s
}

async function loadData() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterStatus.value) params.status = filterStatus.value
    const res = await request.get('/leave/list', { params })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载请假列表失败', e)
  } finally {
    loading.value = false
  }
}

async function approve(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入审批意见（可选）', '审批通过', { confirmButtonText: '确认通过', inputValue: '同意请假' })
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    await request.put(`/leave/approve/${row.id}`, { approveRemark: value, approverId: userInfo.userId })
    ElMessage.success('已通过')
    loadData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

async function reject(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回申请', { confirmButtonText: '确认驳回', inputValue: '不符合请假条件' })
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    await request.put(`/leave/reject/${row.id}`, { approveRemark: value, approverId: userInfo.userId })
    ElMessage.success('已驳回')
    loadData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
