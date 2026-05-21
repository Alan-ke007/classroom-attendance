<template>
  <view class="schedule-page">
    <view class="week-nav">
      <view class="wn-left" @tap="prevWeek">‹</view>
      <view class="wn-center">
        <text class="wn-title">第 {{ currentWeek }} 周</text>
        <button class="wn-today" @tap="goThisWeek">本周</button>
      </view>
      <view class="wn-right" @tap="nextWeek">›</view>
    </view>

    <scroll-view scroll-x class="table-scroll">
      <view class="timetable">
        <!-- 表头 -->
        <view class="tt-row tt-head">
          <view class="tt-time-h">时段</view>
          <view v-for="d in weekDays" :key="d.key" class="tt-col-h" :class="{ today: d.isToday }">
            <text class="tth-day">{{ d.label }}</text>
            <text class="tth-date">{{ d.date }}</text>
          </view>
        </view>

        <!-- 课表内容 -->
        <view v-for="slot in timeSlots" :key="slot.key" class="tt-row">
          <view class="tt-time">
            <text class="tt-period">{{ slot.label }}</text>
            <text class="tt-time-range">{{ slot.time }}</text>
          </view>
          <view v-for="d in weekDays" :key="d.key" class="tt-cell" :class="{ today: d.isToday }">
            <view v-for="c in getSlotCourses(d.key, slot.key)" :key="c.id" class="cb" @tap="showDetail(c)">
              <text class="cb-name">{{ c.courseName }}</text>
              <text class="cb-room">{{ c.classroom || '' }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 详情弹窗 -->
    <view v-if="detail" class="modal-mask" @tap="detail = null">
      <view class="modal-card" @tap.stop>
        <text class="modal-title">{{ detail.courseName }}</text>
        <view class="modal-row"><text class="mr-label">教室</text><text>{{ detail.classroom || '待定' }}</text></view>
        <view class="modal-row"><text class="mr-label">星期</text><text>{{ detail.weekDay }}</text></view>
        <view class="modal-row"><text class="mr-label">时间</text><text>{{ fmtTime(detail.startTime) }} - {{ fmtTime(detail.endTime) }}</text></view>
        <view class="modal-row"><text class="mr-label">周次</text><text>第{{ detail.startWeek || 1 }}-{{ detail.endWeek || 16 }}周</text></view>
        <button class="modal-close" @tap="detail = null">关闭</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCourseList } from '@/api/course'

const currentWeek = ref(1)
const weekDays = ref([])
const courses = ref([])
const detail = ref(null)

const timeSlots = [
  { key: 1, label: '1-2节', time: '08:00-09:40' },
  { key: 2, label: '3-4节', time: '10:00-11:40' },
  { key: 3, label: '5-6节', time: '14:00-15:40' },
  { key: 4, label: '7-8节', time: '16:00-17:40' },
  { key: 5, label: '9-10节', time: '19:00-20:40' },
]
const weekNames = ['周日','周一','周二','周三','周四','周五','周六']

function getSemesterStart() {
  return new Date('2026-02-17')
}

function getCurrentWeek() {
  const start = getSemesterStart()
  const now = new Date()
  const diff = Math.floor((now - start) / (1000 * 60 * 60 * 24))
  return Math.max(1, Math.min(20, Math.floor(diff / 7) + 1))
}

function buildWeekDays() {
  const now = new Date()
  const startWeek = getSemesterStart()
  const weekOffset = (currentWeek.value - 1) * 7
  const mondayOffset = startWeek.getDay() === 0 ? -6 : 1 - startWeek.getDay()
  const base = new Date(startWeek)
  base.setDate(base.getDate() + mondayOffset + weekOffset)

  const days = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(base)
    d.setDate(d.getDate() + i)
    days.push({
      key: i,
      label: weekNames[i],
      date: `${d.getMonth() + 1}/${d.getDate()}`,
      isToday: d.toDateString() === now.toDateString()
    })
  }
  weekDays.value = days
}

function getSlotCourses(day, slot) {
  return courses.value.filter(c => {
    if (c.weekDay !== weekNames[day]) return false
    const start = c.startTime?.substring(0, 5)
    const slotTime = timeSlots[slot - 1]?.time?.substring(0, 5)
    if (!start || !slotTime) return false
    const cMin = parseInt(start.substring(0, 2)) * 60 + parseInt(start.substring(3, 5))
    const sMin = parseInt(slotTime.substring(0, 2)) * 60 + parseInt(slotTime.substring(3, 5))
    return Math.abs(cMin - sMin) <= 30
  })
}

function fmtTime(t) { return t ? t.substring(0, 5) : '--:--' }
function showDetail(c) { detail.value = c }
function prevWeek() { if (currentWeek.value > 1) { currentWeek.value--; buildWeekDays() } }
function nextWeek() { if (currentWeek.value < 20) { currentWeek.value++; buildWeekDays() } }
function goThisWeek() { currentWeek.value = getCurrentWeek(); buildWeekDays() }

onMounted(async () => {
  currentWeek.value = getCurrentWeek()
  buildWeekDays()
  try {
    const res = await getCourseList({ pageNum: 1, pageSize: 100 })
    courses.value = (res.records || []).filter(c =>
      c.startWeek <= currentWeek.value && c.endWeek >= currentWeek.value
    )
  } catch (e) { console.error('加载课程失败', e) }
})
</script>

<style scoped>
.schedule-page { min-height: 100vh; background: #f5f5f5; }

.week-nav {
  display: flex; align-items: center; justify-content: center;
  background: #fff; padding: 16rpx 32rpx;
  border-bottom: 1rpx solid #ebeef5;
}
.wn-left, .wn-right { font-size: 48rpx; color: #4A90D9; padding: 0 24rpx; }
.wn-center { display: flex; align-items: center; gap: 16rpx; }
.wn-title { font-size: 32rpx; font-weight: 600; color: #303133; }
.wn-today {
  font-size: 24rpx; background: #4A90D9; color: #fff;
  padding: 6rpx 20rpx; border-radius: 20rpx; border: none;
  line-height: 1.4;
}

.table-scroll { white-space: nowrap; padding: 16rpx 0; }
.timetable { min-width: 1200rpx; padding: 0 16rpx; }

.tt-row { display: flex; }
.tt-head { position: sticky; top: 0; background: #4A90D9; border-radius: 12rpx 12rpx 0 0; }
.tt-time-h {
  width: 130rpx; padding: 16rpx 8rpx; text-align: center;
  font-size: 24rpx; color: #fff; font-weight: 500;
  flex-shrink: 0;
}
.tt-col-h {
  flex: 1; text-align: center; padding: 12rpx 4rpx;
  color: #fff; min-width: 140rpx;
}
.tt-col-h.today { background: rgba(255,255,255,0.15); border-radius: 8rpx; }
.tth-day { font-size: 26rpx; font-weight: 600; display: block; }
.tth-date { font-size: 20rpx; opacity: 0.8; }

.tt-time {
  width: 130rpx; padding: 12rpx 8rpx; text-align: center;
  background: #f8f9fb; border: 1rpx solid #ebeef5;
  display: flex; flex-direction: column; justify-content: center;
  flex-shrink: 0;
}
.tt-period { font-size: 22rpx; font-weight: 600; color: #606266; }
.tt-time-range { font-size: 18rpx; color: #909399; }

.tt-cell {
  flex: 1; border: 1rpx solid #ebeef5; padding: 4rpx;
  min-width: 140rpx; min-height: 110rpx; vertical-align: top;
}
.tt-cell.today { background: #f0f7ff; }

.cb {
  background: #ecf5ff; border-left: 5rpx solid #409EFF;
  border-radius: 6rpx; padding: 6rpx 8rpx; margin-bottom: 4rpx;
}
.cb:active { background: #d9ecff; }
.cb-name { font-size: 22rpx; font-weight: 600; color: #303133; display: block; }
.cb-room { font-size: 18rpx; color: #909399; }

.modal-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex;
  align-items: center; justify-content: center; z-index: 100;
}
.modal-card {
  width: 560rpx; background: #fff; border-radius: 24rpx;
  padding: 40rpx;
}
.modal-title { font-size: 34rpx; font-weight: 600; display: block; margin-bottom: 24rpx; }
.modal-row { display: flex; padding: 12rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.mr-label { width: 100rpx; color: #909399; font-size: 26rpx; }
.modal-close {
  width: 100%; height: 80rpx; margin-top: 24rpx;
  background: #4A90D9; color: #fff; font-size: 28rpx;
  border-radius: 40rpx; border: none;
}
</style>
