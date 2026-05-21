<template>
  <view class="page">
    <view class="tab-bar">
      <view class="tab" :class="{ active: tab === 'apply' }" @tap="tab = 'apply'">申请请假</view>
      <view class="tab" :class="{ active: tab === 'history' }" @tap="tab = 'history'">申请记录</view>
    </view>

    <view v-if="tab === 'apply'" class="form-card">
      <view class="form-item">
        <text class="fi-label">课程</text>
        <picker mode="selector" :range="courseNames" @change="e => courseIdx = e.detail.value">
          <view class="fi-picker">{{ courseNames[courseIdx] || '请选择课程' }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="fi-label">类型</text>
        <picker mode="selector" :range="leaveTypeLabels" @change="e => leaveType = leaveTypes[e.detail.value]">
          <view class="fi-picker">{{ leaveType === 'sick' ? '病假' : '事假' }}</view>
        </picker>
      </view>
      <view class="form-row">
        <view class="form-item" style="flex:1;margin-right:12rpx;">
          <text class="fi-label">开始日期</text>
          <picker mode="date" @change="e => startDate = e.detail.value">
            <view class="fi-picker">{{ startDate || '请选择' }}</view>
          </picker>
        </view>
        <view class="form-item" style="flex:1;">
          <text class="fi-label">结束日期</text>
          <picker mode="date" @change="e => endDate = e.detail.value">
            <view class="fi-picker">{{ endDate || '请选择' }}</view>
          </picker>
        </view>
      </view>
      <view class="form-item">
        <text class="fi-label">请假原因</text>
        <textarea class="fi-textarea" v-model="reason" placeholder="请输入请假原因" :maxlength="500" />
      </view>
      <view class="form-item">
        <text class="fi-label">证明材料（可选）</text>
        <view class="upload-box" @tap="chooseImage">
          <image v-if="proofImage" class="proof-img" :src="proofImage" mode="aspectFill" />
          <text v-else class="upload-hint">+ 上传图片</text>
        </view>
      </view>
      <button class="submit-btn" :disabled="submitting" @tap="submitLeave">
        {{ submitting ? '提交中...' : '提交申请' }}
      </button>
    </view>

    <view v-if="tab === 'history'" class="list">
      <view v-if="list.length === 0 && !loading" class="empty">暂无请假记录</view>
      <view v-for="item in list" :key="item.id" class="l-item">
        <view class="li-head">
          <text class="li-type" :class="'lt-'+item.leaveType">{{ item.leaveType === 'sick' ? '病假' : '事假' }}</text>
          <text class="li-status" :class="'ls-'+item.status">{{ statusMap[item.status] || item.status }}</text>
        </view>
        <text class="li-course">{{ item.courseName || '课程#' + item.courseId }}</text>
        <text class="li-date">{{ item.startDate }} 至 {{ item.endDate }}</text>
        <text class="li-reason">原因：{{ item.reason }}</text>
        <text v-if="item.approveRemark" class="li-remark">审批：{{ item.approveRemark }}</text>
      </view>
      <view v-if="loading" class="load-tip">加载中...</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { applyLeave, getLeaveList } from '@/api/leave'
import { getCourseList } from '@/api/course'

const tab = ref('apply')
const leaveType = ref('sick')
const leaveTypes = ['sick', 'personal']
const leaveTypeLabels = ['病假', '事假']
const startDate = ref('')
const endDate = ref('')
const reason = ref('')
const proofImage = ref('')
const proofBase64 = ref('')
const submitting = ref(false)
const courseIdx = ref(-1)
const courses = ref([])
const courseNames = ref([])
const list = ref([])
const loading = ref(false)

const statusMap = { pending: '待审批', approved: '已通过', rejected: '已驳回' }

function chooseImage() {
  uni.chooseImage({
    count: 1, sizeType: ['compressed'],
    success: (res) => {
      proofImage.value = res.tempFilePaths[0]
      uni.getFileSystemManager().readFile({
        filePath: res.tempFilePaths[0], encoding: 'base64',
        success: (r) => { proofBase64.value = r.data }
      })
    }
  })
}

async function submitLeave() {
  if (courseIdx.value < 0) { uni.showToast({ title: '请选择课程', icon: 'none' }); return }
  if (!startDate.value) { uni.showToast({ title: '请选择开始日期', icon: 'none' }); return }
  if (!endDate.value) { uni.showToast({ title: '请选择结束日期', icon: 'none' }); return }
  if (!reason.value.trim()) { uni.showToast({ title: '请输入请假原因', icon: 'none' }); return }
  if (startDate.value > endDate.value) { uni.showToast({ title: '结束日期不能早于开始日期', icon: 'none' }); return }

  submitting.value = true
  try {
    const userInfo = uni.getStorageSync('userInfo') || {}
    await applyLeave({
      studentId: userInfo.studentId,
      courseId: courses.value[courseIdx.value].id,
      leaveType: leaveType.value,
      startDate: startDate.value,
      endDate: endDate.value,
      reason: reason.value,
      proofImage: proofBase64.value || ''
    })
    uni.showToast({ title: '申请已提交', icon: 'success' })
    leaveType.value = 'sick'; startDate.value = ''; endDate.value = ''
    reason.value = ''; proofImage.value = ''; proofBase64.value = ''; courseIdx.value = -1
    tab.value = 'history'
    loadHistory()
  } catch (e) { uni.showToast({ title: '提交失败', icon: 'none' }) }
  finally { submitting.value = false }
}

async function loadHistory() {
  loading.value = true
  try {
    const userInfo = uni.getStorageSync('userInfo') || {}
    const res = await getLeaveList({ pageNum: 1, pageSize: 50, studentId: userInfo.studentId })
    list.value = res.records || []
  } catch (e) { console.error('加载请假记录失败', e) }
  finally { loading.value = false }
}

async function loadCourses() {
  try {
    const res = await getCourseList({ pageNum: 1, pageSize: 100 })
    courses.value = res.records || []
    courseNames.value = courses.value.map(c => c.courseName)
  } catch (e) { console.error('加载课程失败', e) }
}

onMounted(() => { loadCourses(); loadHistory() })
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20rpx; }

.tab-bar { display: flex; background: #fff; border-bottom: 1rpx solid #ebeef5; }
.tab {
  flex: 1; text-align: center; padding: 28rpx 0; font-size: 30rpx;
  color: #909399; border-bottom: 4rpx solid transparent;
}
.tab.active { color: #4A90D9; border-bottom-color: #4A90D9; font-weight: 600; }

.form-card { margin: 20rpx; background: #fff; border-radius: 16rpx; padding: 32rpx 24rpx; }
.form-item { margin-bottom: 20rpx; }
.fi-label { font-size: 28rpx; color: #303133; font-weight: 500; margin-bottom: 10rpx; display: block; }
.fi-picker {
  padding: 18rpx 20rpx; border: 1rpx solid #ebeef5; border-radius: 12rpx;
  font-size: 28rpx; color: #606266; background: #f8f9fb;
}
.form-row { display: flex; }
.fi-textarea {
  width: 100%; height: 160rpx; padding: 16rpx 20rpx; border: 1rpx solid #ebeef5;
  border-radius: 12rpx; font-size: 28rpx; background: #f8f9fb; box-sizing: border-box;
}
.upload-box {
  height: 160rpx; border: 2rpx dashed #dcdfe6; border-radius: 16rpx;
  display: flex; align-items: center; justify-content: center; background: #f8f9fb;
}
.upload-hint { font-size: 28rpx; color: #909399; }
.proof-img { width: 100%; height: 100%; border-radius: 14rpx; }
.submit-btn {
  width: 100%; height: 88rpx; background: linear-gradient(135deg, #4A90D9, #357ABD);
  color: #fff; font-size: 30rpx; font-weight: 600;
  border-radius: 44rpx; border: none; margin-top: 12rpx;
}
.submit-btn[disabled] { opacity: 0.6; }

.list { padding: 0 20rpx; }
.empty { text-align: center; padding: 120rpx 0; color: #c0c4cc; font-size: 28rpx; }
.l-item { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 12rpx; }
.li-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.li-type { font-size: 24rpx; padding: 4rpx 14rpx; border-radius: 10rpx; }
.lt-sick { background: #fdf6ec; color: #E6A23C; }
.lt-personal { background: #ecf5ff; color: #4A90D9; }
.li-status { font-size: 24rpx; padding: 4rpx 14rpx; border-radius: 10rpx; }
.ls-pending { background: #fdf6ec; color: #E6A23C; }
.ls-approved { background: #f0f9eb; color: #67C23A; }
.ls-rejected { background: #fef0f0; color: #F56C6C; }
.li-course { font-size: 28rpx; font-weight: 500; color: #303133; display: block; margin-bottom: 4rpx; }
.li-date { font-size: 24rpx; color: #909399; display: block; margin-bottom: 4rpx; }
.li-reason { font-size: 26rpx; color: #606266; display: block; margin-top: 6rpx; }
.li-remark { font-size: 24rpx; color: #909399; display: block; margin-top: 4rpx; }
.load-tip { text-align: center; padding: 24rpx; color: #909399; font-size: 26rpx; }
</style>
