<template>
  <div class="announcement-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>发布公告
          </el-button>
        </div>
      </template>

      <div class="table-wrapper"><el-table :data="announcements" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="置顶" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.isPinned" type="warning" size="small">置顶</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column label="目标" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ targetLabel(row.targetRole) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table></div>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="isMobile ? '95%' : '600px'">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容（支持HTML）" />
        </el-form-item>
        <el-form-item label="目标角色">
          <el-select v-model="form.targetRole" placeholder="全部可见" style="width: 100%">
            <el-option label="全部" value="" />
            <el-option label="仅教师" value="teacher" />
            <el-option label="仅学生" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isPinned" :active-value="1" :inactive-value="0" />
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
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAnnouncementList, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/announcement'

const isMobile = computed(() => window.innerWidth < 768)
const loading = ref(false)
const submitLoading = ref(false)
const announcements = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('发布公告')
const formRef = ref(null)
const form = ref({ title: '', content: '', targetRole: '', isPinned: 0 })

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

const targetLabel = (role) => {
  if (!role) return '全部'
  return role === 'teacher' ? '教师' : '学生'
}

onMounted(() => loadData())

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList({ pageNum: pageNum.value, pageSize: pageSize.value })
    announcements.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('加载公告列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '发布公告'
  form.value = { title: '', content: '', targetRole: '', isPinned: 0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑公告'
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
        await updateAnnouncement(form.value.id, form.value)
        ElMessage.success('更新成功')
      } else {
        await createAnnouncement(form.value)
        ElMessage.success('发布成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
    await deleteAnnouncement(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.announcement-list { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.table-wrapper { overflow-x: auto; }

@media (max-width: 768px) {
  .announcement-list { padding: 12px; }
  .card-header { flex-direction: column; gap: 8px; align-items: flex-start; }
}
</style>
