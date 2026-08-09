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
import { faceCheckin } from '@/api/face'

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

// P1：图片随 faceCheckin 一并上传后端，写签到由后端合并端点完成（VERIFIED/NEED_REVIEW 均放行）
// 小程序只透传图片、按后端 faceStatus 分支展示，绝不自行断言 VERIFIED（R6）
async function submitCheckin() {
  submitting.value = true
  resultMsg.value = ''
  try {
    const facePass = await verifyFace()
    if (!facePass) return   // REJECTED：verifyFace 已提示并取消签到，不写库
    // 写签到已在 faceCheckin 合并端点内由后端完成，此处仅提示并退出
    setTimeout(() => { uni.navigateBack() }, 1500)
  } catch (e) {
    resultMsg.value = '签到失败: ' + (e.message || '未知错误')
    resultType.value = 'error'
  } finally {
    submitting.value = false
  }
}

// P1：调后端合并端点（faceCheckin）完成「提取+比对+写签到」，按 faceStatus 明确分支
// 后端算法不可达时返回 NEED_REVIEW（降级放行），比对不通过返回 REJECTED（取消签到），
// 二者以枚举强制区分，杜绝旧版「算法不可达就无痕 return true」（R7）。小程序不直连算法、不伪造 VERIFIED。
async function verifyFace() {
  if (!capturedImage.value) {
    uni.showToast({ title: '请先拍照', icon: 'none' })
    return false
  }
  let base64
  try {
    base64 = await readImageBase64(capturedImage.value)
  } catch (e) {
    console.error('读取图片失败', e)
    return false
  }

  let r
  try {
    // 图片随签到一并上传后端；request.js 自动带 JWT，不直连算法、不持有密钥
    r = await faceCheckin({ courseId: courseId.value, image: base64 })
  } catch (e) {
    // 网络/服务端错误（非可降级的 faceStatus 分支）：按真实错误提示，不静默放行
    resultMsg.value = e.message || '核验请求失败，请重试'
    resultType.value = 'error'
    return false
  }

  const status = r && r.faceStatus
  if (status === 'REJECTED') {
    resultMsg.value = '人脸核验未通过，签到已取消'
    resultType.value = 'error'
    return false
  }
  if (status === 'NEED_REVIEW') {
    // 后端已写签到（降级放行），仅提示待复核
    resultMsg.value = r.message || '已签到，人脸待复核'
    resultType.value = 'success'
    return true
  }
  // VERIFIED（默认放行）
  resultMsg.value = r.message || '签到成功！'
  resultType.value = 'success'
  return true
}

function readImageBase64(path) {
  return new Promise((resolve, reject) => {
    uni.getFileSystemManager().readFile({
      filePath: path,
      encoding: 'base64',
      success: (r) => resolve(r.data),
      fail: reject
    })
  })
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
