<template>
  <div class="student-leave">
    <el-card style="margin-bottom: 16px;">
      <template #header><span>提交请假申请</span></template>
      <el-form :model="form" label-width="80px" style="max-width: 500px;">
        <el-form-item label="请假类型" required>
          <el-select v-model="form.leaveType" style="width: 100%">
            <el-option label="事假" value="personal" />
            <el-option label="病假" value="sick" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" required>
          <el-date-picker v-model="form.dateRange" type="daterange" range-separator="至"
            start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="请假事由" required>
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请详细说明请假原因" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" :loading="submitting">提交申请</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header><span>我的请假记录</span></template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.leaveType === 'sick' ? 'danger' : 'info'" size="small">
              {{ row.leaveType === 'sick' ? '病假' : '事假' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="220">
          <template #default="{ row }">{{ row.startDate }} 至 {{ row.endDate }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="事由" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approveRemark" label="审批意见" width="150" show-overflow-tooltip />
      </el-table>
      <el-pagination
        v-if="total > 0" v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10, 20]" layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end" @change="loadList"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const submitting = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const form = reactive({ leaveType: 'personal', dateRange: [], reason: '' })

function statusTag(s) { return { pending: 'warning', approved: 'success', rejected: 'danger' }[s] || 'info' }
function statusLabel(s) { return { pending: '待审批', approved: '已通过', rejected: '已驳回' }[s] || s }

async function submit() {
  if (!form.dateRange || form.dateRange.length === 0) { ElMessage.warning('请选择日期'); return }
  if (!form.reason.trim()) { ElMessage.warning('请填写事由'); return }
  submitting.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    await request.post('/leave/apply', {
      studentId: userInfo.studentId,
      leaveType: form.leaveType,
      startDate: form.dateRange[0],
      endDate: form.dateRange[1],
      reason: form.reason,
      status: 'pending'
    })
    ElMessage.success('请假申请已提交')
    form.reason = ''; form.dateRange = []
    loadList()
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    submitting.value = false
  }
}

async function loadList() {
  loading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await request.get('/leave/list', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value, studentId: userInfo.studentId }
    })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载请假列表失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadList)
</script>
