<template>
  <view class="page">
    <view class="course-header">
      <text class="ch-label">当前签到</text>
      <text class="ch-course">{{ courseName }}</text>
    </view>

    <view class="camera-area" v-if="!capturedImage">
      <camera v-if="showCamera" device-position="front" flash="off" class="camera" />
      <view v-else class="camera-placeholder">
        <text class="cp-icon">📷</text>
        <text class="cp-text">点击下方按钮启动摄像头</text>
      </view>
    </view>

    <view v-if="capturedImage" class="preview-area">
      <image :src="capturedImage" mode="aspectFit" class="preview-img" />
      <view class="preview-actions">
        <button class="pa-btn" @tap="retake">重新拍摄</button>
        <button class="pa-btn pa-confirm" @tap="submitCheckin" :disabled="submitting">
          {{ submitting ? '提交中...' : '确认签到' }}
        </button>
      </view>
    </view>

    <view v-if="!capturedImage" class="capture-bar">
      <button v-if="!showCamera" class="cap-btn" @tap="startCamera">启动摄像头</button>
      <button v-else class="cap-btn" @tap="capture">拍照签到</button>
    </view>

    <view v-if="resultMsg" class="result-card" :class="resultType">
      <text class="result-text">{{ resultMsg }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { addAttendance } from '@/api/attendance'

const courseId = ref('')
const courseName = ref('')
const showCamera = ref(false)
const capturedImage = ref('')
const submitting = ref(false)
const resultMsg = ref('')
const resultType = ref('')
let cameraCtx = null

onMounted(() => {
  const pages = getCurrentPages()
  const opts = pages[pages.length - 1]?.$page?.options || {}
  courseId.value = opts.courseId || ''
  courseName.value = decodeURIComponent(opts.courseName || '未知课程')
})

function startCamera() {
  showCamera.value = true
  setTimeout(() => {
    cameraCtx = uni.createCameraContext()
  }, 500)
}

function capture() {
  if (!cameraCtx) {
    uni.showToast({ title: '摄像头未就绪', icon: 'none' })
    return
  }
  cameraCtx.takePhoto({
    quality: 'high',
    success: (res) => {
      capturedImage.value = res.tempImagePath
      showCamera.value = false
    },
    fail: (err) => {
      console.error('拍照失败', err)
      uni.showToast({ title: '拍照失败，请重试', icon: 'none' })
    }
  })
}

function retake() {
  capturedImage.value = ''
  showCamera.value = true
  setTimeout(() => { cameraCtx = uni.createCameraContext() }, 500)
}

async function submitCheckin() {
  submitting.value = true
  resultMsg.value = ''
  try {
    const userInfo = uni.getStorageSync('userInfo') || {}
    await addAttendance({
      studentId: userInfo.studentId,
      courseId: courseId.value,
      status: 'present',
      imagePath: capturedImage.value,
      remark: '小程序签到'
    })
    resultMsg.value = '签到成功！'
    resultType.value = 'success'
    setTimeout(() => { uni.navigateBack() }, 1500)
  } catch (e) {
    resultMsg.value = '签到失败: ' + (e.message || '未知错误')
    resultType.value = 'error'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 40rpx; }

.course-header { background: #fff; padding: 24rpx 32rpx; margin-bottom: 20rpx; }
.ch-label { font-size: 24rpx; color: #909399; display: block; }
.ch-course { font-size: 34rpx; font-weight: 600; color: #303133; margin-top: 4rpx; }

.camera-area { margin: 0 20rpx; border-radius: 16rpx; overflow: hidden; }
.camera { width: 100%; height: 600rpx; }
.camera-placeholder {
  width: 100%; height: 600rpx; background: #e8eaed;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
}
.cp-icon { font-size: 80rpx; margin-bottom: 16rpx; }
.cp-text { font-size: 28rpx; color: #909399; }

.preview-area { margin: 0 20rpx; }
.preview-img { width: 100%; height: 600rpx; border-radius: 16rpx; background: #000; }
.preview-actions { display: flex; gap: 20rpx; margin-top: 24rpx; }
.pa-btn { flex: 1; height: 80rpx; line-height: 80rpx; font-size: 28rpx; background: #f5f5f5; color: #606266; border-radius: 40rpx; border: none; text-align: center; }
.pa-confirm { background: linear-gradient(135deg, #4A90D9, #357ABD); color: #fff; }

.capture-bar { text-align: center; margin: 30rpx 20rpx; }
.cap-btn { width: 100%; height: 88rpx; background: linear-gradient(135deg, #4A90D9, #357ABD); color: #fff; font-size: 30rpx; border-radius: 44rpx; border: none; }

.result-card { margin: 20rpx; padding: 24rpx; border-radius: 16rpx; text-align: center; }
.result-card.success { background: #f0f9eb; }
.result-card.error { background: #fef0f0; }
.result-text { font-size: 28rpx; }
.success .result-text { color: #67C23A; }
.error .result-text { color: #F56C6C; }
</style>
