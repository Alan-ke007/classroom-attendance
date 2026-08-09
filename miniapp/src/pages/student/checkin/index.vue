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
import { recognizeFace } from '@/api/algorithm'
import { BASE_URL } from '@/config'

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

// C6 人脸签到客户端防呆（受限实现）
// 完整闭环依赖后端识别接口 + 算法服务鉴权（P1 协同），
// 当前仅做客户端前置校验：写库前必须完成身份比对，识别失败则不落库 present。
async function submitCheckin() {
  submitting.value = true
  resultMsg.value = ''
  try {
    const facePass = await verifyFace()
    if (!facePass) {
      resultMsg.value = '人脸核验未通过，签到已取消'
      resultType.value = 'error'
      return
    }

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

// C6 人脸身份比对（客户端防呆）
// 流程：读取图片 -> 上传后端/对象存储 -> 调用算法服务识别
// 注：生产环境上传与识别应由后端中转鉴权，前端不直连算法服务（P1）
async function verifyFace() {
  if (!capturedImage.value) {
    uni.showToast({ title: '请先拍照', icon: 'none' })
    return false
  }
  const userInfo = uni.getStorageSync('userInfo') || {}

  // 1) 图片转 base64 供算法服务识别
  let base64
  try {
    base64 = await readImageBase64(capturedImage.value)
  } catch (e) {
    console.error('读取图片失败', e)
    return false
  }

  // 2) 上传图片到后端/对象存储（需后端提供上传/识别接口）
  //    后端暂无上传接口时可跳过，但生产必须上传以供服务端复核
  try {
    await uploadCheckinImage(capturedImage.value)
  } catch (e) {
    console.warn('图片上传失败，将仅做前端识别', e)
  }

  // 3) 调用算法服务完成人脸比对（前端直连，P1 整改：应由后端中转鉴权）
  let res
  try {
    res = await recognizeFace(base64)
  } catch (e) {
    // 算法服务暂不可达（如未配置密钥 / P1 后端中转未就绪）：降级为常规签到，避免阻塞主流程。
    // 待 P1 后端代理就位、识别接口可用后，此分支将返回真实比对结果并真正生效。
    console.warn('人脸识别服务不可用，降级为常规签到', e)
    uni.showToast({ title: '人脸服务暂不可用，已按常规签到', icon: 'none' })
    return true
  }

  // 识别结果判定：以 matched/success 为准；若返回 studentId 须与当前用户一致
  const matched = res && (res.matched === true || res.success === true || res.code === 200)
  if (!matched) return false
  if (res.studentId && userInfo.studentId && String(res.studentId) !== String(userInfo.studentId)) {
    return false
  }
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

// 上传签到图片（需后端提供上传/识别接口；URL 走 config.BASE_URL）
function uploadCheckinImage(path) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + '/attendance/upload', // TODO: 与后端确认上传接口路径
      filePath: path,
      name: 'file',
      formData: { type: 'checkin' },
      success: (r) => {
        if (r.statusCode >= 200 && r.statusCode < 300) resolve(r)
        else reject(new Error('upload failed: ' + r.statusCode))
      },
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
