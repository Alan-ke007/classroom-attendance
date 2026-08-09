<template>
  <view class="page">
    <view class="tip-card">
      <text class="tip-title">人脸建档</text>
      <text class="tip-desc">请上传 1~3 张清晰正脸照，用于后续拍照签到核验。建议光线充足、免遮挡。</text>
    </view>

    <view class="img-grid">
      <view class="img-item" v-for="(img, idx) in images" :key="idx" @tap="removeImage(idx)">
        <image :src="img" mode="aspectFill" class="img-thumb" />
        <text class="img-del">×</text>
      </view>
      <view class="img-item img-add" v-if="images.length < 3" @tap="chooseImage">
        <text class="img-add-icon">＋</text>
        <text class="img-add-text">添加照片</text>
      </view>
    </view>

    <text class="count-tip">已选 {{ images.length }}/3 张</text>

    <button class="submit-btn" :disabled="submitting || images.length === 0" @tap="submitEnroll">
      {{ submitting ? '建档中...' : '确认建档' }}
    </button>

    <view v-if="resultMsg" class="result-card" :class="resultType">
      <text class="result-text">{{ resultMsg }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { enrollFace } from '@/api/face'

const images = ref([])
const submitting = ref(false)
const resultMsg = ref('')
const resultType = ref('')

function chooseImage() {
  const remain = 3 - images.value.length
  uni.chooseMedia({
    count: remain,
    mediaType: ['image'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const paths = (res.tempFiles || []).map(f => f.tempFilePath)
      images.value = images.value.concat(paths).slice(0, 3)
    },
    fail: (err) => {
      if (err && err.errMsg && err.errMsg.indexOf('cancel') === -1) {
        uni.showToast({ title: '选择图片失败', icon: 'none' })
      }
    }
  })
}

function removeImage(idx) {
  images.value.splice(idx, 1)
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

async function submitEnroll() {
  if (images.value.length === 0) {
    uni.showToast({ title: '请先选择照片', icon: 'none' })
    return
  }
  submitting.value = true
  resultMsg.value = ''
  try {
    const base64List = await Promise.all(images.value.map(readImageBase64))
    await enrollFace(base64List)
    resultMsg.value = '建档成功'
    resultType.value = 'success'
    images.value = []
  } catch (e) {
    // 后端 message 已含具体原因（张数非法 / 无单人脸 / 算法不可达等）
    resultMsg.value = e.message || '建档失败，请重试'
    resultType.value = 'error'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding: 24rpx; }
.tip-card { background: #fff; border-radius: 16rpx; padding: 28rpx; margin-bottom: 24rpx; }
.tip-title { font-size: 32rpx; font-weight: 600; color: #303133; display: block; }
.tip-desc { font-size: 26rpx; color: #909399; margin-top: 10rpx; display: block; line-height: 1.5; }

.img-grid { display: flex; flex-wrap: wrap; gap: 20rpx; }
.img-item {
  position: relative; width: 200rpx; height: 200rpx; border-radius: 16rpx;
  background: #fff; overflow: hidden;
}
.img-thumb { width: 100%; height: 100%; }
.img-del {
  position: absolute; top: 6rpx; right: 6rpx; width: 36rpx; height: 36rpx;
  line-height: 36rpx; text-align: center; border-radius: 50%;
  background: rgba(0,0,0,0.5); color: #fff; font-size: 28rpx;
}
.img-add {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  border: 2rpx dashed #c0c4cc;
}
.img-add-icon { font-size: 60rpx; color: #c0c4cc; }
.img-add-text { font-size: 24rpx; color: #909399; margin-top: 8rpx; }

.count-tip { display: block; text-align: center; font-size: 24rpx; color: #909399; margin: 20rpx 0; }

.submit-btn {
  width: 100%; height: 88rpx; line-height: 88rpx; font-size: 30rpx;
  color: #fff; border-radius: 44rpx; border: none;
  background: linear-gradient(135deg, #4A90D9, #357ABD);
}
.submit-btn[disabled] { opacity: 0.6; }

.result-card { margin-top: 24rpx; padding: 24rpx; border-radius: 16rpx; text-align: center; }
.result-card.success { background: #f0f9eb; }
.result-card.error { background: #fef0f0; }
.result-text { font-size: 28rpx; }
.success .result-text { color: #67C23A; }
.error .result-text { color: #F56C6C; }
</style>
