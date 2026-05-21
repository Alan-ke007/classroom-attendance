<template>
  <div class="import-data">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>批量导入学生</span>
            <el-button style="float: right" text type="primary" @click="downloadTemplate('students')">
              下载模板
            </el-button>
          </template>
          <el-alert title="模板格式" type="info" :closable="false" style="margin-bottom: 16px;">
            学号 | 姓名 | 性别 | 班级 | 手机号（每行一个学生）
          </el-alert>
          <el-upload
            drag :auto-upload="false" :limit="1" accept=".xlsx,.xls"
            :on-change="f => studentFile = f.raw"
          >
            <el-icon :size="48"><UploadFilled /></el-icon>
            <div>拖拽或点击上传Excel文件</div>
          </el-upload>
          <el-button type="primary" style="margin-top: 12px; width: 100%"
            @click="importStudents" :loading="importingStudent">
            开始导入学生
          </el-button>
          <el-alert v-if="studentResult" :title="`成功 ${studentResult.success} 条，失败 ${studentResult.fail} 条`"
            :type="studentResult.fail === 0 ? 'success' : 'warning'" style="margin-top: 12px;" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>批量导入课程</span>
            <el-button style="float: right" text type="primary" @click="downloadTemplate('courses')">
              下载模板
            </el-button>
          </template>
          <el-alert title="模板格式" type="info" :closable="false" style="margin-bottom: 16px;">
            课程名称 | 教室 | 开始时间 | 结束时间 | 星期 | 班级
          </el-alert>
          <el-upload
            drag :auto-upload="false" :limit="1" accept=".xlsx,.xls"
            :on-change="f => courseFile = f.raw"
          >
            <el-icon :size="48"><UploadFilled /></el-icon>
            <div>拖拽或点击上传Excel文件</div>
          </el-upload>
          <el-button type="primary" style="margin-top: 12px; width: 100%"
            @click="importCourses" :loading="importingCourse">
            开始导入课程
          </el-button>
          <el-alert v-if="courseResult" :title="`成功 ${courseResult.success} 条，失败 ${courseResult.fail} 条`"
            :type="courseResult.fail === 0 ? 'success' : 'warning'" style="margin-top: 12px;" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const studentFile = ref(null)
const courseFile = ref(null)
const importingStudent = ref(false)
const importingCourse = ref(false)
const studentResult = ref(null)
const courseResult = ref(null)

async function importStudents() {
  if (!studentFile.value) { ElMessage.warning('请上传文件'); return }
  importingStudent.value = true
  try {
    const form = new FormData()
    form.append('file', studentFile.value)
    const res = await request.post('/import/students', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    studentResult.value = res.data
  } catch (e) { console.error(e) }
  finally { importingStudent.value = false }
}

async function importCourses() {
  if (!courseFile.value) { ElMessage.warning('请上传文件'); return }
  importingCourse.value = true
  try {
    const form = new FormData()
    form.append('file', courseFile.value)
    const res = await request.post('/import/courses', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    courseResult.value = res.data
  } catch (e) { console.error(e) }
  finally { importingCourse.value = false }
}

function downloadTemplate(type) {
  const wb = window.XLSX?.utils?.book_new?.()
  if (!wb) {
    ElMessage.warning('请安装xlsx依赖或手动创建Excel文件')
    return
  }
  const headers = type === 'students'
    ? [['学号', '姓名', '性别', '班级', '手机号']]
    : [['课程名称', '教室', '开始时间', '结束时间', '星期', '班级']]
  const ws = window.XLSX.utils.aoa_to_sheet(headers)
  window.XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
  window.XLSX.writeFile(wb, `${type}_template.xlsx`)
}
</script>
