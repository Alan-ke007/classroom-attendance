<template>
  <div class="class-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>班级管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加班级
          </el-button>
        </div>
      </template>
      
      <el-table :data="classList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="className" label="班级名称" />
        <el-table-column prop="major" label="专业" />
        <el-table-column prop="grade" label="年级" />
        <el-table-column prop="teacher" label="班主任" />
        <el-table-column prop="studentCount" label="人数" width="100" />
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
        @size-change="loadClassList"
        @current-change="loadClassList"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="form.className" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="form.major" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="form.grade" placeholder="请输入年级" />
        </el-form-item>
        <el-form-item label="班主任">
          <el-input v-model="form.teacher" placeholder="请输入班主任姓名" />
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
const classList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('添加班级')
const form = ref({
  id: null,
  className: '',
  major: '',
  grade: '',
  teacher: '',
  studentCount: 0
})

// 表单验证规则
const formRules = {
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadClassList()
})

const loadClassList = async () => {
  loading.value = true
  try {
    const res = await request.get('/class/list', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    })
    classList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载班级列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加班级'
  form.value = { id: null, className: '', major: '', grade: '', teacher: '', studentCount: 0 }
  // 清除表单验证状态
  setTimeout(() => {
    formRef.value?.clearValidate()
  }, 0)
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑班级'
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
        await request.put(`/class/${form.value.id}`, form.value)
        ElMessage.success('更新成功')
      } else {
        await request.post('/class', form.value)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadClassList()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该班级吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/class/${row.id}`)
    ElMessage.success('删除成功')
    loadClassList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.class-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
