<template>
  <div class="qr-checkin">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header><span>生成签到二维码</span></template>
          <el-form label-width="80px">
            <el-form-item label="选择课程">
              <el-select v-model="selectedCourseId" placeholder="请选择课程" style="width: 100%">
                <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="generateQR" :loading="generating">生成二维码</el-button>
            </el-form-item>
          </el-form>
          <div v-if="qrToken" class="qr-display">
            <canvas ref="qrCanvas" width="260" height="260" />
            <div class="qr-info">
              <el-tag type="success" size="large">{{ countdown }} 秒后过期</el-tag>
              <p style="margin-top: 8px; color: var(--c-text-tertiary);">课程：{{ qrCourseName }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header><span>扫码签到结果</span></template>
          <div v-if="checkinResults.length === 0" class="empty-hint">
            <el-icon :size="48"><Checked /></el-icon>
            <p>等待学生扫码签到...</p>
          </div>
          <el-timeline v-else>
            <el-timeline-item
              v-for="r in checkinResults" :key="r.time"
              :timestamp="r.time" placement="top"
            >
              {{ r.message }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Checked } from '@element-plus/icons-vue'
import request from '@/utils/request'

const courses = ref([])
const selectedCourseId = ref('')
const generating = ref(false)
const qrToken = ref('')
const qrCourseName = ref('')
const countdown = ref(300)
const checkinResults = ref([])
const qrCanvas = ref(null)
let countdownTimer = null

onMounted(async () => {
  try {
    const res = await request.get('/course/all')
    courses.value = res.data || []
  } catch (e) { console.error(e) }
})

function generateQR() {
  if (!selectedCourseId.value) { ElMessage.warning('请选择课程'); return }
  generating.value = true
  request.get('/qrcode/generate', { params: { courseId: selectedCourseId.value } }).then(res => {
    const data = res.data
    qrToken.value = data.token
    qrCourseName.value = data.courseName
    countdown.value = (data.expireMinutes || 5) * 60
    drawQRCode(data.token)
    startCountdown()
    generating.value = false
  }).catch(() => { generating.value = false })
}

function drawQRCode(text) {
  import('qrcode').then(QRCode => {
    if (qrCanvas.value) QRCode.toCanvas(qrCanvas.value, text, { width: 260 })
  }).catch(() => {
    // 备用：使用在线API生成
    if (qrCanvas.value) {
      const ctx = qrCanvas.value.getContext('2d')
      const img = new Image()
      img.onload = () => ctx.drawImage(img, 0, 0, 260, 260)
      img.src = `https://api.qrserver.com/v1/create-qr-code/?size=260x260&data=${encodeURIComponent(text)}`
    }
  })
}

function startCountdown() {
  clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) { clearInterval(countdownTimer); qrToken.value = '' }
  }, 1000)
}

onBeforeUnmount(() => clearInterval(countdownTimer))
</script>

<style scoped>
.qr-display { margin-top: 20px; text-align: center; }
.qr-info { margin-top: 12px; }
.empty-hint { text-align: center; padding: 60px 0; color: var(--c-text-tertiary); }
.empty-hint p { margin-top: 12px; font-size: 14px; }
</style>
