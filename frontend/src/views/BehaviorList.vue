<template>
  <div class="behavior-page">
    <!-- KPI Summary Cards -->
    <div class="kpi-grid">
      <div class="kpi-card kpi-total">
        <div class="kpi-icon">
          <el-icon :size="20"><List /></el-icon>
        </div>
        <div class="kpi-body">
          <span class="kpi-value">{{ total }}</span>
          <span class="kpi-label">总记录</span>
        </div>
      </div>
      <div class="kpi-card kpi-pending">
        <div class="kpi-icon">
          <el-icon :size="20"><WarningFilled /></el-icon>
        </div>
        <div class="kpi-body">
          <span class="kpi-value">{{ pendingCount }}</span>
          <span class="kpi-label">待处理</span>
        </div>
      </div>
      <div class="kpi-card kpi-today">
        <div class="kpi-icon">
          <el-icon :size="20"><Clock /></el-icon>
        </div>
        <div class="kpi-body">
          <span class="kpi-value">{{ todayCount }}</span>
          <span class="kpi-label">今日新增</span>
        </div>
      </div>
    </div>

    <!-- Main Card -->
    <div class="main-card">
      <div class="card-header">
        <h2 class="card-title">行为记录</h2>
        <div class="header-actions">
          <el-button class="action-btn export-btn" @click="handleExport">
            <el-icon><Download /></el-icon>
            <span class="btn-text">导出</span>
          </el-button>
          <el-button class="action-btn add-btn" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            <span class="btn-text">添加记录</span>
          </el-button>
        </div>
      </div>

      <!-- Search Bar -->
      <div class="search-bar">
        <el-input
          v-model="searchForm.studentName"
          placeholder="搜索学生姓名..."
          clearable
          class="search-input"
          @keyup.enter="loadBehaviorList"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.behaviorType" placeholder="行为类型" clearable class="search-select">
          <el-option label="举手" value="raising_hand" />
          <el-option label="阅读" value="reading" />
          <el-option label="书写" value="writing" />
          <el-option label="玩手机" value="using_phone" />
          <el-option label="低头" value="bowing_head" />
          <el-option label="趴桌" value="leaning_over" />
        </el-select>
        <el-select v-model="searchForm.handled" placeholder="处理状态" clearable class="search-select-sm">
          <el-option label="未处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>
        <el-button type="primary" class="search-btn" @click="loadBehaviorList">查询</el-button>
        <el-button class="search-btn" @click="resetSearch">重置</el-button>
      </div>

      <!-- Table -->
      <div class="table-wrapper">
        <el-table
          :data="behaviorList"
          style="width: 100%"
          v-loading="loading"
          row-class-name="behavior-row"
          @row-click="handleEdit"
        >
          <el-table-column prop="id" label="ID" width="72" />
          <el-table-column prop="studentName" label="学生姓名" min-width="100" />
          <el-table-column prop="className" label="班级" min-width="100" />
          <el-table-column label="行为类型" width="110">
            <template #default="scope">
              <span class="behavior-tag" :class="'tag-' + scope.row.behaviorType">
                {{ getBehaviorText(scope.row.behaviorType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="behaviorTime" label="发生时间" width="170" />
          <el-table-column prop="confidence" label="置信度" width="90" />
          <el-table-column label="状态" width="90">
            <template #default="scope">
              <span class="status-dot" :class="scope.row.handled === 1 ? 'dot-done' : 'dot-pending'" />
              {{ scope.row.handled === 1 ? '已处理' : '待处理' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <div class="row-actions">
                <el-button size="small" class="row-btn" @click.stop="handleEdit(scope.row)">编辑</el-button>
                <el-button
                  v-if="scope.row.handled === 0"
                  size="small"
                  type="success"
                  class="row-btn"
                  @click.stop="handleMarkAsHandled(scope.row)"
                >标记处理</el-button>
                <el-button size="small" type="danger" class="row-btn" @click.stop="handleDelete(scope.row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          size="default"
          @size-change="loadBehaviorList"
          @current-change="loadBehaviorList"
        />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :width="isMobile ? '92%' : '520px'"
      :close-on-click-modal="false"
      class="form-dialog"
    >
      <el-form :model="form" label-position="top" class="dialog-form">
        <el-form-item label="学生">
          <el-select v-model="form.studentId" placeholder="请选择学生" style="width: 100%">
            <el-option v-for="stu in studentList" :key="stu.id" :label="stu.name" :value="stu.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="行为类型" required>
          <el-select v-model="form.behaviorType" placeholder="请选择行为类型" style="width: 100%">
            <el-option label="举手" value="raising_hand" />
            <el-option label="阅读" value="reading" />
            <el-option label="书写" value="writing" />
            <el-option label="玩手机" value="using_phone" />
            <el-option label="低头" value="bowing_head" />
            <el-option label="趴桌" value="leaning_over" />
          </el-select>
        </el-form-item>
        <el-form-item label="发生时间" required>
          <el-date-picker
            v-model="form.behaviorTime"
            type="datetime"
            placeholder="选择时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="置信度">
          <el-input-number v-model="form.confidence" :min="0" :max="100" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="form.handleRemark" type="textarea" placeholder="请输入处理备注" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, List, WarningFilled, Clock, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

const isMobile = computed(() => window.innerWidth < 768)

const loading = ref(false)
const behaviorList = ref([])
const studentList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pendingCount = ref(0)
const todayCount = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('添加行为记录')

const searchForm = ref({
  studentName: '',
  behaviorType: '',
  handled: null
})

const form = ref({
  id: null,
  studentId: null,
  behaviorType: '',
  behaviorTime: '',
  confidence: 0,
  handleRemark: ''
})

onMounted(() => {
  loadBehaviorList()
  loadStudentList()
})

const loadBehaviorList = async () => {
  loading.value = true
  try {
    const res = await request.get('/behavior/list', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        studentName: searchForm.value.studentName || undefined,
        behaviorType: searchForm.value.behaviorType || undefined,
        handled: searchForm.value.handled !== null ? searchForm.value.handled : undefined
      }
    })
    const records = res.data?.records || []
    behaviorList.value = records
    total.value = res.data?.total || 0

    const all = res.data?.allRecords || records
    pendingCount.value = all.filter(r => r.handled === 0).length
    const today = new Date().toISOString().slice(0, 10)
    todayCount.value = all.filter(r => r.behaviorTime?.startsWith(today)).length
  } catch (error) {
    ElMessage.error('加载行为记录失败')
  } finally {
    loading.value = false
  }
}

const loadStudentList = async () => {
  try {
    const res = await request.get('/student/all')
    studentList.value = res.data || []
  } catch (error) {
    console.error('加载学生列表失败', error)
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加行为记录'
  form.value = { id: null, studentId: null, behaviorType: '', behaviorTime: '', confidence: 0, handleRemark: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑行为记录'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.behaviorType || !form.value.behaviorTime) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    if (form.value.id) {
      await request.put(`/behavior/${form.value.id}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/behavior', form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadBehaviorList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该记录吗？', '提示', { type: 'warning' })
    await request.delete(`/behavior/${row.id}`)
    ElMessage.success('删除成功')
    loadBehaviorList()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleMarkAsHandled = async (row) => {
  try {
    await request.put(`/behavior/handle/${row.id}`, { handleRemark: '已处理' })
    ElMessage.success('标记为已处理')
    loadBehaviorList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const resetSearch = () => {
  searchForm.value = { studentName: '', behaviorType: '', handled: null }
  loadBehaviorList()
}

const getBehaviorText = (type) => {
  const map = {
    raising_hand: '举手', reading: '阅读', writing: '书写',
    using_phone: '玩手机', bowing_head: '低头', leaning_over: '趴桌',
    sleeping: '睡觉', phone: '玩手机', eating: '吃东西',
    talking: '讲话', leaving: '离开座位'
  }
  return map[type] || type
}

const handleExport = async () => {
  try {
    ElMessage.info('正在生成Excel文件...')
    const response = await request.get('/behavior/export', { responseType: 'blob' })
    const blob = response.data
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const d = new Date()
    link.download = `行为记录_${d.getFullYear()}${(d.getMonth() + 1).toString().padStart(2, '0')}${d.getDate().toString().padStart(2, '0')}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败：' + (error.message || '未知错误'))
  }
}
</script>

<style scoped>
/* ===== Page Layout ===== */
.behavior-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* ===== KPI Cards ===== */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.kpi-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-radius: 12px;
  background: var(--bg);
  border: 1px solid var(--border);
  transition: box-shadow 200ms ease, transform 200ms ease;
  cursor: default;
}

.kpi-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.kpi-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-total .kpi-icon {
  background: rgba(184, 134, 11, 0.10);
  color: #00E5FF;
}

.kpi-pending .kpi-icon {
  background: rgba(230, 126, 34, 0.10);
  color: #E67E22;
}

.kpi-today .kpi-icon {
  background: rgba(76, 175, 80, 0.10);
  color: #4CAF50;
}

.kpi-body {
  display: flex;
  flex-direction: column;
}

.kpi-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--text-h);
  font-variant-numeric: tabular-nums;
}

.kpi-label {
  font-size: 13px;
  color: var(--text);
  margin-top: 2px;
}

/* ===== Main Card ===== */
.main-card {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 14px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  border-bottom: 1px solid var(--border);
  flex-wrap: wrap;
  gap: 10px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-h);
  margin: 0;
  letter-spacing: -0.2px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 200ms ease;
}

/* ===== Search Bar ===== */
.search-bar {
  display: flex;
  gap: 10px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  flex: 1;
  min-width: 180px;
  max-width: 260px;
}

.search-select {
  width: 140px;
}

.search-select-sm {
  width: 120px;
}

.search-btn {
  border-radius: 8px;
  transition: all 200ms ease;
}

/* ===== Table ===== */
.table-wrapper {
  overflow-x: auto;
  padding: 0 2px;
}

.table-wrapper :deep(.behavior-row) {
  cursor: pointer;
  transition: background-color 180ms ease;
}

.table-wrapper :deep(.behavior-row:hover) {
  background-color: rgba(0, 229, 255, 0.06);
}

/* Behavior Tag */
.behavior-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.tag-raising_hand  { background: rgba(76,175,80,0.10);  color: #388E3C; }
.tag-reading       { background: rgba(33,150,243,0.10); color: #1565C0; }
.tag-writing       { background: rgba(156,39,176,0.10); color: #7B1FA2; }
.tag-using_phone   { background: rgba(230,126,34,0.10); color: #BF5F0A; }
.tag-bowing_head   { background: rgba(229,57,53,0.10);  color: #C62828; }
.tag-leaning_over  { background: rgba(229,57,53,0.10);  color: #C62828; }
.tag-sleeping      { background: rgba(229,57,53,0.10);  color: #C62828; }
.tag-phone         { background: rgba(230,126,34,0.10); color: #BF5F0A; }
.tag-eating        { background: rgba(156,39,176,0.10); color: #7B1FA2; }
.tag-talking       { background: rgba(33,150,243,0.10); color: #1565C0; }
.tag-leaving       { background: rgba(76,175,80,0.10);  color: #388E3C; }

/* Status Dot */
.status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}

.dot-done    { background: #22C55E; }
.dot-pending { background: #F97316; }

/* Row Actions */
.row-actions {
  display: flex;
  gap: 6px;
  align-items: center;
}

.row-btn {
  border-radius: 6px;
  padding: 5px 12px;
  min-height: 32px;
  white-space: nowrap;
  transition: all 180ms ease;
}

/* ===== Pagination ===== */
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 20px;
  border-top: 1px solid var(--border);
}

/* ===== Dialog ===== */
.form-dialog :deep(.el-dialog) {
  border-radius: 14px;
}

.dialog-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-h);
}

/* ===== Dark Mode ===== */
@media (prefers-color-scheme: dark) {
  .kpi-card:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  }
}

/* ===== Mobile (≤768px) ===== */
@media (max-width: 768px) {
  .behavior-page {
    padding: 12px;
  }

  /* KPI — 3 col still fine, compact */
  .kpi-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    margin-bottom: 14px;
  }

  .kpi-card {
    padding: 12px;
    gap: 8px;
  }

  .kpi-icon {
    width: 34px;
    height: 34px;
    border-radius: 8px;
  }

  .kpi-value {
    font-size: 20px;
  }

  .kpi-label {
    font-size: 11px;
  }

  .card-header {
    padding: 14px 16px;
  }

  .header-actions {
    width: 100%;
  }

  .action-btn {
    flex: 1;
    justify-content: center;
    min-height: 42px;
  }

  .btn-text {
    display: inline;
  }

  /* Search — full width stacking */
  .search-bar {
    padding: 12px 16px;
    gap: 8px;
  }

  .search-input {
    max-width: 100%;
    flex: 1 1 100%;
  }

  .search-select,
  .search-select-sm {
    flex: 1;
    min-width: 0;
    width: auto;
  }

  .search-btn {
    flex: 1;
    min-height: 42px;
  }

  /* Table row actions — stack vertically */
  .row-actions {
    flex-direction: column;
    gap: 4px;
  }

  .row-btn {
    min-height: 36px;
    width: 100%;
  }

  .pagination-bar {
    justify-content: center;
    padding: 12px 16px;
  }

  .pagination-bar :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
}

/* ===== Small Mobile (≤400px) ===== */
@media (max-width: 400px) {
  .kpi-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .kpi-card {
    flex-direction: row;
    padding: 10px 14px;
  }

  .search-bar {
    flex-direction: column;
  }

  .search-select,
  .search-select-sm,
  .search-btn {
    width: 100%;
  }

  .row-actions {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .row-btn {
    flex: 1;
    min-width: 60px;
  }
}

/* Reduced motion */
@media (prefers-reduced-motion: reduce) {
  .kpi-card,
  .action-btn,
  .search-btn,
  .row-btn,
  .table-wrapper :deep(.behavior-row) {
    transition: none;
  }

  .kpi-card:hover {
    transform: none;
  }
}
</style>
