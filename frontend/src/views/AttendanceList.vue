<template>
  <div class="attendance-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考勤记录</span>
          <div>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出Excel
            </el-button>
            <el-button type="danger" @click="handleExportPdf">
              <el-icon><Document /></el-icon>
              导出PDF
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
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="出勤" value="present" />
            <el-option label="缺勤" value="absent" />
            <el-option label="迟到" value="late" />
            <el-option label="请假" value="leave" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAttendanceList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="attendanceList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentName" label="学生姓名" />
        <el-table-column prop="className" label="班级" />
        <el-table-column prop="courseName" label="课程" />
        <el-table-column prop="attendanceDate" label="日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="签到时间" width="180" />
        <el-table-column prop="confidence" label="置信度" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
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
        @size-change="loadAttendanceList"
        @current-change="loadAttendanceList"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="form.studentId" placeholder="请选择学生" style="width: 100%">
            <el-option v-for="stu in studentList" :key="stu.id" :label="stu.name" :value="stu.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="course in courseList" :key="course.id" :label="course.courseName" :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="attendanceDate">
          <el-date-picker v-model="form.attendanceDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="出勤" value="present" />
            <el-option label="缺勤" value="absent" />
            <el-option label="迟到" value="late" />
            <el-option label="请假" value="leave" />
          </el-select>
        </el-form-item>
        <el-form-item label="签到时间">
          <el-date-picker v-model="form.checkInTime" type="datetime" placeholder="选择时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Document } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const attendanceList = ref([])
const studentList = ref([])
const courseList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('添加考勤记录')
const searchForm = ref({
  studentName: '',
  status: '',
  dateRange: null
})
const form = ref({
  id: null,
  studentId: null,
  courseId: null,
  attendanceDate: '',
  status: '',
  checkInTime: '',
  remark: ''
})

// 表单验证规则
const formRules = {
  studentId: [
    { required: true, message: '请选择学生', trigger: 'change' }
  ],
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change' }
  ],
  attendanceDate: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

onMounted(() => {
  loadAttendanceList()
  loadStudentList()
  loadCourseList()
})

const loadAttendanceList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      studentName: searchForm.value.studentName,
      status: searchForm.value.status
    }
    if (searchForm.value.dateRange) {
      params.startDate = searchForm.value.dateRange[0]
      params.endDate = searchForm.value.dateRange[1]
    }
    const res = await request.get('/attendance/list', { params })
    attendanceList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载考勤记录失败')
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

const loadCourseList = async () => {
  try {
    const res = await request.get('/course/all')
    courseList.value = res.data || []
  } catch (error) {
    console.error('加载课程列表失败', error)
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加考勤记录'
  form.value = { id: null, studentId: null, courseId: null, attendanceDate: '', status: '', checkInTime: '', remark: '' }
  // 清除表单验证状态
  setTimeout(() => {
    formRef.value?.clearValidate()
  }, 0)
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑考勤记录'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      if (form.value.id) {
        await request.put(`/attendance/${form.value.id}`, form.value)
        ElMessage.success('更新成功')
      } else {
        await request.post('/attendance', form.value)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadAttendanceList()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该记录吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/attendance/${row.id}`)
    ElMessage.success('删除成功')
    loadAttendanceList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const resetSearch = () => {
  searchForm.value = { studentName: '', status: '', dateRange: null }
  loadAttendanceList()
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

// 导出PDF
const handleExportPdf = () => {
  const token = localStorage.getItem('token')
  const params = new URLSearchParams()
  if (searchForm.value.dateRange) {
    params.append('startDate', searchForm.value.dateRange[0])
    params.append('endDate', searchForm.value.dateRange[1])
  }
  const url = `/api/export/pdf/attendance?${params.toString()}&token=${token}`
  window.open(url, '_blank')
}

// 导出Excel
const handleExport = async () => {
  try {
    ElMessage.info('正在生成Excel文件...')
    const response = await request.get('/attendance/export', { responseType: 'blob' })
    // 对于blob响应，拦截器返回完整的axios response对象
    const blob = response.data
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const date = new Date()
    const fileName = `考勤记录_${date.getFullYear()}${(date.getMonth() + 1).toString().padStart(2, '0')}${date.getDate().toString().padStart(2, '0')}.xlsx`
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
.attendance-list {
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
