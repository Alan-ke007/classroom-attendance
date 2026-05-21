<template>
  <div class="behavior-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>行为记录</span>
          <div>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出Excel
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              添加记录
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学生姓名">
          <el-input v-model="searchForm.studentName" placeholder="请输入学生姓名" clearable />
        </el-form-item>
        <el-form-item label="行为类型">
          <el-select v-model="searchForm.behaviorType" placeholder="请选择行为类型" clearable>
            <el-option label="举手" value="raising_hand" />
            <el-option label="阅读" value="reading" />
            <el-option label="书写" value="writing" />
            <el-option label="玩手机" value="using_phone" />
            <el-option label="低头" value="bowing_head" />
            <el-option label="趴桌" value="leaning_over" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.handled" placeholder="请选择" clearable>
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadBehaviorList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="behaviorList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentName" label="学生姓名" />
        <el-table-column prop="className" label="班级" />
        <el-table-column prop="behaviorType" label="行为类型" width="120">
          <template #default="scope">
            <el-tag :type="getBehaviorTypeTag(scope.row.behaviorType)">
              {{ getBehaviorText(scope.row.behaviorType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="behaviorTime" label="发生时间" width="180" />
        <el-table-column prop="confidence" label="置信度" width="100" />
        <el-table-column prop="handled" label="处理状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.handled === 1 ? 'success' : 'warning'">
              {{ scope.row.handled === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              v-if="scope.row.handled === 0" 
              size="small" 
              type="success" 
              @click="handleMarkAsHandled(scope.row)"
            >
              标记处理
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadBehaviorList"
        @current-change="loadBehaviorList"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
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
          <el-date-picker v-model="form.behaviorTime" type="datetime" placeholder="选择时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="置信度">
          <el-input-number v-model="form.confidence" :min="0" :max="100" :precision="2" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="form.handleRemark" type="textarea" placeholder="请输入处理备注" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const behaviorList = ref([])
const studentList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
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
        studentName: searchForm.value.studentName,
        behaviorType: searchForm.value.behaviorType,
        handled: searchForm.value.handled
      }
    })
    behaviorList.value = res.data.records || []
    total.value = res.data.total || 0
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
    await ElMessageBox.confirm('确定要删除该记录吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/behavior/${row.id}`)
    ElMessage.success('删除成功')
    loadBehaviorList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
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

const getBehaviorTypeTag = (type) => {
  const tagMap = {
    raising_hand: 'success', reading: 'primary', writing: 'info',
    using_phone: 'warning', bowing_head: 'danger', leaning_over: 'danger',
    sleeping: 'danger', phone: 'warning', eating: 'info',
    talking: 'primary', leaving: 'success'
  }
  return tagMap[type] || 'info'
}

const getBehaviorText = (type) => {
  const textMap = {
    raising_hand: '举手', reading: '阅读', writing: '书写',
    using_phone: '玩手机', bowing_head: '低头', leaning_over: '趴桌',
    sleeping: '睡觉', phone: '玩手机', eating: '吃东西',
    talking: '讲话', leaving: '离开座位'
  }
  return textMap[type] || type
}

// 导出Excel
const handleExport = async () => {
  try {
    ElMessage.info('正在生成Excel文件...')
    
    // 调用后端导出接口
    const response = await request.get('/behavior/export', { responseType: 'blob' })
    const blob = response.data
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    const date = new Date()
    const fileName = `行为记录_${date.getFullYear()}${(date.getMonth() + 1).toString().padStart(2, '0')}${date.getDate().toString().padStart(2, '0')}.xlsx`
    link.download = fileName
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败：' + (error.message || '未知错误'))
  }
}
</script>

<style scoped>
.behavior-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
