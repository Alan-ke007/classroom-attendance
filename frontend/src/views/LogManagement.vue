<template>
  <div class="log-page">
    <div class="page-header">
      <h2><el-icon style="vertical-align: middle; margin-right: 8px"><List /></el-icon>操作日志</h2>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="filters" class="search-form">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="操作人">
              <el-input v-model="filters.username" placeholder="用户名" clearable @change="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="操作类型">
              <el-select v-model="filters.operation" placeholder="全部" clearable @change="handleSearch" style="width: 100%;">
                <el-option label="全部" value="" />
                <el-option label="登录" value="login" />
                <el-option label="创建" value="create" />
                <el-option label="更新" value="update" />
                <el-option label="删除" value="delete" />
                <el-option label="导出" value="export" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="开始日期">
              <el-date-picker v-model="filters.startDate" type="date" placeholder="开始日期"
                value-format="YYYY-MM-DD" style="width: 100%;" @change="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="结束日期">
              <el-date-picker v-model="filters.endDate" type="date" placeholder="结束日期"
                value-format="YYYY-MM-DD" style="width: 100%;" @change="handleSearch" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="logs" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="title" label="操作" width="160" />
        <el-table-column prop="operation" label="操作类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="operationTagType(row.operation)" size="small">
              {{ operationLabel(row.operation) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="method" label="请求方法" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.costTime > 1000 ? '#f56c6c' : 'var(--c-text-secondary)' }">
              {{ row.costTime }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="errorMsg" label="错误信息" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.errorMsg" style="color: var(--c-danger)">{{ row.errorMsg }}</span>
            <span v-else style="color: var(--c-text-tertiary)">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current="filters.page"
          v-model:page-size="filters.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const logs = ref([])
const total = ref(0)

const filters = reactive({
  page: 1,
  size: 20,
  username: '',
  operation: '',
  startDate: '',
  endDate: ''
})

function operationTagType(op) {
  const map = { login: '', create: 'success', update: 'warning', delete: 'danger', export: 'info' }
  return map[op] || 'info'
}

function operationLabel(op) {
  const map = { login: '登录', create: '创建', update: '更新', delete: '删除', export: '导出' }
  return map[op] || op
}

function handleSearch() {
  filters.page = 1
  loadLogs()
}

async function loadLogs() {
  loading.value = true
  try {
    const res = await request.get('/log/list', { params: { ...filters } })
    logs.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(loadLogs)
</script>

<style scoped>
.log-page {
  padding: 0;
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 20px;
  margin: 0;
}
.search-card {
  margin-bottom: 16px;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
}
.search-form {
  padding: 8px 0;
}
.table-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
