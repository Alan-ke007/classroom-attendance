<template>
  <view class="page">
    <view class="scan-section">
      <text class="scan-icon">📱</text>
      <text class="scan-title">二维码签到</text>
      <text class="scan-desc">扫描教师展示的课堂二维码完成签到</text>
      <button class="scan-btn" @tap="startScan">扫一扫</button>
    </view>

    <view v-if="resultMsg" class="result-card" :class="resultType">
      <text class="result-icon">{{ resultType === 'success' ? '✓' : '✗' }}</text>
      <text class="result-text">{{ resultMsg }}</text>
      <text v-if="courseName" class="result-course">{{ courseName }}</text>
    </view>

    <view class="history-section">
      <text class="section-title">签到记录</text>
      <view v-if="records.length === 0" class="empty">暂无签到记录</view>
      <view v-for="item in records" :key="item.id" class="rec-item">
        <view class="rec-left">
          <text class="rec-course">{{ item.courseName || '课程#' + item.courseId }}</text>
          <text class="rec-time">{{ item.attendanceDate }} {{ fmtTime(item.checkInTime) }}</text>
        </view>
        <text class="rec-status" :class="'rs-'+item.status">{{ statusMap[item.status] || item.status }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { checkinByQRCode } from '@/api/qrcode'
import { getAttendanceList } from '@/api/attendance'

const resultMsg = ref('')
const resultType = ref('')
const courseName = ref('')
const records = ref([])
const statusMap = { present: '已签到', late: '迟到', absent: '缺勤', leave: '请假' }

function fmtTime(t) { return t ? t.substring(11, 16) : '' }

function startScan() {
  uni.scanCode({
    onlyFromCamera: true, scanType: ['qrCode'],
    success: async (res) => {
      const token = res.result || ''
      if (!token) { resultMsg.value = '未能识别二维码'; resultType.value = 'error'; return }
      uni.showLoading({ title: '签到中...' })
      try {
        const data = await checkinByQRCode(token)
        uni.hideLoading()
        resultMsg.value = data.message || '签到成功！'
        resultType.value = 'success'
        courseName.value = data.courseName || ''
        loadRecords()
      } catch (e) {
        uni.hideLoading()
        resultMsg.value = e.message || '签到失败'
        resultType.value = 'error'
      }
    },
    fail: () => { resultMsg.value = '扫码已取消'; resultType.value = 'error' }
  })
}

async function loadRecords() {
  try {
    const userInfo = uni.getStorageSync('userInfo') || {}
    const res = await getAttendanceList({ pageNum: 1, pageSize: 10, studentId: userInfo.studentId })
    records.value = res.records || []
  } catch (e) { console.error('加载签到记录失败', e) }
}

onMounted(loadRecords)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20rpx; }

.scan-section {
  display: flex; flex-direction: column; align-items: center;
  padding: 80rpx 40rpx; background: #fff; margin: 20rpx;
  border-radius: 16rpx;
}
.scan-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.scan-title { font-size: 36rpx; font-weight: 700; color: #303133; }
.scan-desc { font-size: 26rpx; color: #909399; margin-top: 12rpx; text-align: center; }
.scan-btn {
  width: 400rpx; height: 88rpx; background: linear-gradient(135deg, #4A90D9, #357ABD);
  color: #fff; border: none; border-radius: 44rpx; font-size: 32rpx;
  font-weight: 600; margin-top: 40rpx; line-height: 88rpx;
}

.result-card {
  margin: 20rpx; padding: 32rpx; border-radius: 16rpx;
  display: flex; flex-direction: column; align-items: center;
}
.result-card.success { background: #f0f9eb; }
.result-card.error { background: #fef0f0; }
.result-icon {
  width: 64rpx; height: 64rpx; border-radius: 50%; display: flex;
  align-items: center; justify-content: center; font-size: 32rpx; color: #fff; margin-bottom: 12rpx;
}
.success .result-icon { background: #67C23A; }
.error .result-icon { background: #F56C6C; }
.result-text { font-size: 30rpx; font-weight: 500; }
.success .result-text { color: #67C23A; }
.error .result-text { color: #F56C6C; }
.result-course { font-size: 26rpx; color: #909399; margin-top: 6rpx; }

.history-section { margin: 20rpx; }
.section-title { font-size: 30rpx; font-weight: 600; color: #303133; margin-bottom: 16rpx; display: block; }
.empty { text-align: center; padding: 80rpx 0; color: #c0c4cc; font-size: 28rpx; }
.rec-item {
  background: #fff; border-radius: 14rpx; padding: 24rpx;
  margin-bottom: 10rpx; display: flex; justify-content: space-between; align-items: center;
}
.rec-course { font-size: 28rpx; font-weight: 500; color: #303133; display: block; }
.rec-time { font-size: 24rpx; color: #909399; margin-top: 4rpx; display: block; }
.rec-status { font-size: 24rpx; padding: 6rpx 16rpx; border-radius: 20rpx; }
.rs-present { background: #f0f9eb; color: #67C23A; }
.rs-late { background: #fdf6ec; color: #E6A23C; }
.rs-absent { background: #fef0f0; color: #F56C6C; }
.rs-leave { background: #f4f4f5; color: #909399; }
</style>
