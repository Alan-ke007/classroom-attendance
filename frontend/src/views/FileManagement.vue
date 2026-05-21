<template>
  <div class="file-management">
    <el-card class="search-card">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-select v-model="query.category" placeholder="文件分类" clearable style="width: 100%">
            <el-option label="全部" value="" />
            <el-option label="通用" value="general" />
            <el-option label="课程资料" value="course" />
            <el-option label="考勤图片" value="attendance" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-col>
        <el-col :span="12" style="text-align: right">
          <el-upload
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
            accept="*/*"
          >
            <el-button type="primary" :loading="uploading">
              <el-icon style="margin-right: 4px"><Upload /></el-icon>
              上传文件
            </el-button>
          </el-upload>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table :data="fileList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="originalName" label="文件名" min-width="200">
          <template #default="{ row }">
            <div class="file-name">
              <el-icon><Document /></el-icon>
              {{ row.originalName }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="fileExtension" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.fileExtension || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="categoryTagType(row.category)" size="small">{{ categoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mimeType" label="MIME" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="上传时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleDownload(row)">下载</el-button>
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFileList, deleteFile, uploadFile } from '@/api/file'

const loading = ref(false)
const uploading = ref(false)
const fileList = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  category: ''
})

function formatSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(i > 0 ? 1 : 0) + ' ' + units[i]
}

function categoryTagType(category) {
  const map = { general: '', face: 'success', course: 'warning', attendance: 'primary' }
  return map[category] || 'info'
}

function categoryLabel(category) {
  const map = { general: '通用', face: '人脸图片', course: '课程资料', attendance: '考勤图片' }
  return map[category] || category
}

async function loadData() {
  loading.value = true
  try {
    const res = await getFileList({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      category: query.category || undefined
    })
    fileList.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error('加载文件列表失败', e)
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  loadData()
}

function reset() {
  query.category = ''
  query.pageNum = 1
  loadData()
}

function beforeUpload(file) {
  if (file.size > 20 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 20MB')
    return false
  }
  return true
}

async function handleUpload(options) {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    formData.append('category', query.category || 'general')
    await uploadFile(formData)
    ElMessage.success('上传成功')
    loadData()
  } catch (e) {
    console.error('上传失败', e)
  } finally {
    uploading.value = false
  }
}

async function handleDownload(row) {
  const token = localStorage.getItem('token')
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  window.open(`${baseUrl}/file/download/${row.id}?token=${token}`, '_blank')
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除文件「${row.originalName}」？`, '提示')
    await deleteFile(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败', e)
  }
}

onMounted(loadData)
</script>

<style scoped>
.file-management {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  min-height: 400px;
}

.file-name {
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
