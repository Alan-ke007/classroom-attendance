<template>
  <view class="page">
    <view class="filter-tabs">
      <text class="tab" :class="{ active: filter === '' }" @tap="filter = ''; loadData()">全部</text>
      <text class="tab" :class="{ active: filter === 'pending' }" @tap="filter = 'pending'; loadData()">待审批</text>
      <text class="tab" :class="{ active: filter === 'approved' }" @tap="filter = 'approved'; loadData()">已通过</text>
      <text class="tab" :class="{ active: filter === 'rejected' }" @tap="filter = 'rejected'; loadData()">已驳回</text>
    </view>

    <view v-if="list.length === 0 && !loading" class="empty">暂无请假记录</view>

    <view v-for="item in list" :key="item.id" class="l-item">
      <view class="li-head">
        <view>
          <text class="li-student">{{ item.studentName || '学生#' + item.studentId }}</text>
          <text class="li-course">{{ item.courseName || '课程#' + item.courseId }}</text>
        </view>
        <text class="li-status" :class="'ls-'+item.status">{{ statusMap[item.status] || item.status }}</text>
      </view>
      <view class="li-row">
        <text class="lir-label">类型</text>
        <text class="lir-value">{{ item.leaveType === 'sick' ? '病假' : '事假' }}</text>
      </view>
      <view class="li-row">
        <text class="lir-label">时间</text>
        <text class="lir-value">{{ item.startDate }} 至 {{ item.endDate }}</text>
      </view>
      <view class="li-row">
        <text class="lir-label">原因</text>
        <text class="lir-value">{{ item.reason }}</text>
      </view>
      <view v-if="item.proofImage" class="li-proof" @tap="previewImage(item.proofImage)">
        <text class="proof-link">查看证明材料</text>
      </view>
      <view v-if="item.approveRemark" class="li-row">
        <text class="lir-label">意见</text>
        <text class="lir-value">{{ item.approveRemark }}</text>
      </view>
      <view v-if="item.status === 'pending'" class="li-actions">
        <button class="btn btn-reject" @tap="handleReject(item)">驳回</button>
        <button class="btn btn-approve" @tap="handleApprove(item)">批准</button>
      </view>
    </view>

    <view v-if="loading" class="load-tip">加载中...</view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLeaveList, approveLeave, rejectLeave } from '@/api/leave'

const list = ref([])
const filter = ref('')
const loading = ref(false)
const statusMap = { pending: '待审批', approved: '已通过', rejected: '已驳回' }

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (filter.value) params.status = filter.value
    const res = await getLeaveList({ pageNum: 1, pageSize: 200, ...params })
    list.value = res.records || []
  } catch (e) { console.error('加载请假列表失败', e) }
  finally { loading.value = false }
}

function handleApprove(item) {
  uni.showModal({
    title: '批准请假',
    content: `确认批准 ${item.studentName || '该学生'} 的请假申请？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await approveLeave(item.id, { approverId: 0, approveRemark: '批准' })
        uni.showToast({ title: '已批准', icon: 'success' })
        loadData()
      } catch (e) { uni.showToast({ title: '操作失败', icon: 'none' }) }
    }
  })
}

function handleReject(item) {
  uni.showModal({
    title: '驳回请假',
    content: `确认驳回 ${item.studentName || '该学生'} 的请假申请？`,
    editable: true,
    placeholderText: '请输入驳回原因',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await rejectLeave(item.id, { approverId: 0, approveRemark: res.content || '驳回' })
        uni.showToast({ title: '已驳回', icon: 'success' })
        loadData()
      } catch (e) { uni.showToast({ title: '操作失败', icon: 'none' }) }
    }
  })
}

function previewImage(url) { uni.previewImage({ urls: [url], current: url }) }

onMounted(loadData)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20rpx; }

.filter-tabs { display: flex; gap: 8rpx; padding: 16rpx 20rpx; background: #fff; border-bottom: 1rpx solid #ebeef5; }
.tab {
  font-size: 26rpx; padding: 10rpx 22rpx; border-radius: 20rpx; color: #909399; background: #f5f5f5;
}
.tab.active { background: #4A90D9; color: #fff; }

.empty { text-align: center; padding: 120rpx 0; color: #c0c4cc; font-size: 28rpx; }

.l-item { background: #fff; margin: 12rpx 20rpx; border-radius: 16rpx; padding: 24rpx; }
.li-head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16rpx; }
.li-student { font-size: 30rpx; font-weight: 600; color: #303133; display: block; }
.li-course { font-size: 24rpx; color: #909399; margin-top: 4rpx; display: block; }
.li-status { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 16rpx; }
.ls-pending { background: #fdf6ec; color: #E6A23C; }
.ls-approved { background: #f0f9eb; color: #67C23A; }
.ls-rejected { background: #fef0f0; color: #F56C6C; }

.li-row { display: flex; padding: 6rpx 0; }
.lir-label { width: 80rpx; font-size: 26rpx; color: #909399; flex-shrink: 0; }
.lir-value { font-size: 26rpx; color: #606266; flex: 1; }

.li-proof { padding: 8rpx 0; }
.proof-link { font-size: 26rpx; color: #4A90D9; }

.li-actions { display: flex; gap: 16rpx; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #f5f5f5; justify-content: flex-end; }
.btn { border: none; border-radius: 10rpx; padding: 12rpx 36rpx; font-size: 26rpx; line-height: 1.5; }
.btn-approve { background: #67C23A; color: #fff; }
.btn-reject { background: #F56C6C; color: #fff; }
.load-tip { text-align: center; padding: 24rpx; color: #909399; font-size: 26rpx; }
</style>
