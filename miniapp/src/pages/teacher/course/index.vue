<template>
  <view class="page">
    <view class="top-bar">
      <text class="page-title">课程管理</text>
      <button class="add-btn" @tap="openAdd">+ 添加</button>
    </view>

    <view v-for="c in courseList" :key="c.id" class="course-card">
      <view class="cc-head">
        <view class="cch-left">
          <text class="cch-name">{{ c.courseName }}</text>
          <text class="cch-tag">{{ c.weekDay }}</text>
        </view>
        <text class="cch-weeks">第{{ c.startWeek || 1 }}-{{ c.endWeek || 16 }}周</text>
      </view>
      <view class="cc-body">
        <text class="ccb-info">{{ c.classroom || '未指定教室' }} | {{ fmtTime(c.startTime) }} - {{ fmtTime(c.endTime) }}</text>
      </view>
      <view class="cc-actions">
        <button class="cca-btn" @tap="genQR(c)">签到码</button>
        <button class="cca-btn cca-del" @tap="delCourse(c)">删除</button>
      </view>
    </view>

    <view v-if="courseList.length === 0" class="empty">暂无课程</view>

    <view v-if="showAdd" class="modal-mask" @tap="showAdd = false">
      <view class="modal-card" @tap.stop>
        <text class="modal-title">添加课程</text>
        <input class="m-input" v-model="form.courseName" placeholder="课程名称" />
        <input class="m-input" v-model="form.classroom" placeholder="教室" />
        <view class="m-row">
          <picker mode="selector" :range="weekDayOpts" @change="e => form.weekDay = weekDayOpts[e.detail.value]">
            <view class="m-picker">{{ form.weekDay || '选择星期' }}</view>
          </picker>
        </view>
        <view class="m-row">
          <picker mode="time" :value="form.startTime" @change="e => form.startTime = e.detail.value">
            <view class="m-picker">{{ form.startTime || '开始时间' }}</view>
          </picker>
        </view>
        <view class="m-row">
          <picker mode="time" :value="form.endTime" @change="e => form.endTime = e.detail.value">
            <view class="m-picker">{{ form.endTime || '结束时间' }}</view>
          </picker>
        </view>
        <view class="m-row">
          <text>周次：</text>
          <input class="m-input-sm" v-model.number="form.startWeek" type="number" placeholder="1" />
          <text>至</text>
          <input class="m-input-sm" v-model.number="form.endWeek" type="number" placeholder="16" />
        </view>
        <button class="modal-btn" @tap="submitAdd">确认添加</button>
        <button class="modal-cancel" @tap="showAdd = false">取消</button>
      </view>
    </view>

    <view v-if="qrVisible" class="modal-mask" @tap="qrVisible = false">
      <view class="modal-card" @tap.stop>
        <text class="modal-title">签到二维码</text>
        <text class="qr-course-name">{{ qrCourseName }}</text>
        <image v-if="qrImage" :src="qrImage" mode="widthFix" class="qr-img" />
        <text class="qr-timer">{{ qrCountdown }}秒后过期</text>
        <button class="modal-btn" @tap="qrVisible = false">关闭</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCourseList, addCourse, deleteCourse } from '@/api/course'
import { generateQRCode } from '@/api/qrcode'

const courseList = ref([])
const showAdd = ref(false)
const qrVisible = ref(false)
const qrImage = ref('')
const qrCourseName = ref('')
const qrCountdown = ref(300)
let qrTimer = null

const weekDayOpts = ['周一','周二','周三','周四','周五','周六','周日']
const form = ref({ courseName: '', classroom: '', weekDay: '周一', startTime: '08:00', endTime: '09:40', startWeek: 1, endWeek: 16 })

function fmtTime(t) { return t ? t.substring(0, 5) : '--:--' }

function openAdd() {
  form.value = { courseName: '', classroom: '', weekDay: '周一', startTime: '08:00', endTime: '09:40', startWeek: 1, endWeek: 16 }
  showAdd.value = true
}

async function submitAdd() {
  if (!form.value.courseName) { uni.showToast({ title: '请输入课程名称', icon: 'none' }); return }
  try {
    await addCourse(form.value)
    uni.showToast({ title: '添加成功', icon: 'success' })
    showAdd.value = false
    loadCourses()
  } catch (e) { uni.showToast({ title: '添加失败', icon: 'none' }) }
}

async function genQR(c) {
  try {
    const res = await generateQRCode(c.id)
    const token = res.token || res.qrToken
    qrImage.value = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(token)}`
    qrCourseName.value = c.courseName
    qrCountdown.value = 300
    qrVisible.value = true
    clearInterval(qrTimer)
    qrTimer = setInterval(() => {
      qrCountdown.value--
      if (qrCountdown.value <= 0) { clearInterval(qrTimer); qrVisible.value = false }
    }, 1000)
  } catch (e) { uni.showToast({ title: '生成失败', icon: 'none' }) }
}

async function delCourse(c) {
  const res = await new Promise(r => uni.showModal({ title: '确认删除', content: `确定删除"${c.courseName}"吗？`, success: r }))
  if (!res.confirm) return
  try {
    await deleteCourse(c.id)
    uni.showToast({ title: '已删除', icon: 'success' })
    loadCourses()
  } catch (e) { uni.showToast({ title: '删除失败', icon: 'none' }) }
}

async function loadCourses() {
  try {
    const res = await getCourseList({ pageNum: 1, pageSize: 100 })
    courseList.value = res.records || []
  } catch (e) { console.error('加载课程失败', e) }
}

onMounted(loadCourses)
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20rpx; }

.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 24rpx; background: #fff; border-bottom: 1rpx solid #ebeef5; }
.page-title { font-size: 34rpx; font-weight: 600; color: #303133; }
.add-btn { background: #4A90D9; color: #fff; font-size: 26rpx; padding: 12rpx 28rpx; border-radius: 30rpx; border: none; }

.course-card { background: #fff; margin: 16rpx 20rpx; border-radius: 16rpx; padding: 24rpx; }
.cc-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.cch-left { display: flex; align-items: center; gap: 12rpx; }
.cch-name { font-size: 30rpx; font-weight: 600; color: #303133; }
.cch-tag { font-size: 22rpx; background: #ecf5ff; color: #4A90D9; padding: 4rpx 14rpx; border-radius: 16rpx; }
.cch-weeks { font-size: 24rpx; color: #909399; }
.ccb-info { font-size: 26rpx; color: #606266; }
.cc-actions { display: flex; gap: 16rpx; margin-top: 16rpx; }
.cca-btn { flex: 1; background: #ecf5ff; color: #4A90D9; font-size: 24rpx; padding: 14rpx 0; border-radius: 12rpx; border: none; text-align: center; }
.cca-del { background: #fef0f0; color: #F56C6C; }

.empty { text-align: center; padding: 120rpx 0; color: #c0c4cc; font-size: 28rpx; }

.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-card { width: 600rpx; background: #fff; border-radius: 24rpx; padding: 40rpx; max-height: 80vh; overflow-y: auto; }
.modal-title { font-size: 34rpx; font-weight: 600; display: block; margin-bottom: 24rpx; }
.m-input { background: #f8f9fb; border-radius: 12rpx; height: 80rpx; padding: 0 20rpx; margin-bottom: 16rpx; font-size: 28rpx; }
.m-input-sm { width: 100rpx; background: #f8f9fb; border-radius: 12rpx; height: 64rpx; padding: 0 16rpx; font-size: 26rpx; text-align: center; }
.m-row { display: flex; align-items: center; gap: 12rpx; margin-bottom: 16rpx; font-size: 26rpx; color: #606266; }
.m-picker { background: #f8f9fb; padding: 14rpx 24rpx; border-radius: 12rpx; font-size: 26rpx; }
.modal-btn { width: 100%; height: 80rpx; background: #4A90D9; color: #fff; font-size: 28rpx; border-radius: 16rpx; border: none; margin-top: 8rpx; }
.modal-cancel { width: 100%; height: 80rpx; background: #f5f5f5; color: #606266; font-size: 28rpx; border-radius: 16rpx; border: none; margin-top: 12rpx; }

.qr-course-name { font-size: 28rpx; color: #606266; display: block; margin-bottom: 16rpx; }
.qr-img { width: 100%; border-radius: 12rpx; }
.qr-timer { font-size: 26rpx; color: #E6A23C; display: block; text-align: center; margin-top: 12rpx; }
</style>
