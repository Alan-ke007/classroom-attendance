<template>
  <div class="course-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加课程
          </el-button>
        </div>
      </template>
      
      <el-table :data="courseList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="classroom" label="教室" />
        <el-table-column prop="startTime" label="开始时间" width="120" />
        <el-table-column prop="endTime" label="结束时间" width="120" />
        <el-table-column prop="weekDay" label="星期" width="80" />
        <el-table-column label="周次" width="120">
          <template #default="scope">
            <span>第{{ scope.row.startWeek || 1 }}-{{ scope.row.endWeek || 16 }}周</span>
          </template>
        </el-table-column>
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
        @size-change="loadCourseList"
        @current-change="loadCourseList"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="教室">
          <el-input v-model="form.classroom" placeholder="请输入教室" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker v-model="form.startTime" placeholder="选择时间" style="width: 100%" format="HH:mm:ss" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker v-model="form.endTime" placeholder="选择时间" style="width: 100%" format="HH:mm:ss" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="星期">
          <el-select v-model="form.weekDay" placeholder="请选择星期" style="width: 100%">
            <el-option label="周一" value="周一" />
            <el-option label="周二" value="周二" />
            <el-option label="周三" value="周三" />
            <el-option label="周四" value="周四" />
            <el-option label="周五" value="周五" />
            <el-option label="周六" value="周六" />
            <el-option label="周日" value="周日" />
          </el-select>
        </el-form-item>
        <el-form-item label="起始周">
          <el-input-number v-model="form.startWeek" :min="1" :max="20" placeholder="1" style="width: 120px" />
          <span style="margin: 0 8px; color: var(--c-text-tertiary);">至</span>
          <el-input-number v-model="form.endWeek" :min="1" :max="20" placeholder="16" style="width: 120px" />
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
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const courseList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('添加课程')
const form = ref({
  id: null,
  courseName: '',
  classroom: '',
  startTime: '',
  endTime: '',
  weekDay: '',
  startWeek: 1,
  endWeek: 16
})

// 表单验证规则
const formRules = {
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadCourseList()
})

const loadCourseList = async () => {
  loading.value = true
  try {
    const res = await request.get('/course/list', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    })
    courseList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载课程列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加课程'
  form.value = { id: null, courseName: '', classroom: '', startTime: '', endTime: '', weekDay: '', startWeek: 1, endWeek: 16 }
  // 清除表单验证状态
  setTimeout(() => {
    formRef.value?.clearValidate()
  }, 0)
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑课程'
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
        await request.put(`/course/${form.value.id}`, form.value)
        ElMessage.success('更新成功')
      } else {
        await request.post('/course', form.value)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadCourseList()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该课程吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/course/${row.id}`)
    ElMessage.success('删除成功')
    loadCourseList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.course-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
