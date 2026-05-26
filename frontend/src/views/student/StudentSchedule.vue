<template>
  <div class="schedule-page">
    <el-card shadow="never">
      <template #header>
        <div class="schedule-header">
          <span class="title">课程表</span>
          <div class="header-right">
            <span class="week-info">第 {{ currentWeek }} 周</span>
            <el-button-group size="small">
              <el-button :disabled="currentWeek <= 1" @click="currentWeek--">上一周</el-button>
              <el-button type="primary" @click="currentWeek = getCurrentWeek()">本周</el-button>
              <el-button :disabled="currentWeek >= 20" @click="currentWeek++">下一周</el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <div class="timetable-wrapper">
        <table class="timetable">
          <thead>
            <tr>
              <th class="time-col">时间</th>
              <th v-for="d in weekDays" :key="d.key" :class="{ today: d.key === todayDay }">
                {{ d.label }}<br/><span class="date-sm">{{ d.date }}</span>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="slot in timeSlots" :key="slot.key">
              <td class="time-cell">
                <div class="slot-num">{{ slot.label }}</div>
                <div class="slot-time">{{ slot.time }}</div>
              </td>
              <td v-for="d in weekDays" :key="d.key" class="course-cell" :class="{ today: d.key === todayDay }">
                <div v-for="c in getSlotCourses(d.key, slot.key)" :key="c.id" class="course-block" @click="showCourseDetail(c)">
                  <div class="cb-name">{{ c.courseName }}</div>
                  <div class="cb-room">{{ c.classroom || '待定' }}</div>
                  <div class="cb-teacher">{{ c.teacherName || '' }}</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" :title="detailCourse?.courseName" width="420px">
      <div v-if="detailCourse" class="course-detail">
        <div class="cd-row"><span class="cd-label">课程名称</span><span>{{ detailCourse.courseName }}</span></div>
        <div class="cd-row"><span class="cd-label">授课教师</span><span>{{ detailCourse.teacherName || '待定' }}</span></div>
        <div class="cd-row"><span class="cd-label">教室</span><span>{{ detailCourse.classroom || '待定' }}</span></div>
        <div class="cd-row"><span class="cd-label">时间</span><span>{{ detailCourse.weekDay }} {{ detailCourse.startTime?.substring(0,5) }}-{{ detailCourse.endTime?.substring(0,5) }}</span></div>
        <div class="cd-row"><span class="cd-label">周次</span><span>第{{ detailCourse.startWeek || 1 }}-{{ detailCourse.endWeek || 16 }}周</span></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCoursesByClassId } from '@/api/student'

const weekDays = ref([])
const todayDay = ref(-1)
const currentWeek = ref(1)
const courses = ref([])
const detailVisible = ref(false)
const detailCourse = ref(null)

const weekNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const timeSlots = [
  { key: 1, label: '第1-2节', time: '08:00-09:40' },
  { key: 2, label: '第3-4节', time: '10:00-11:40' },
  { key: 3, label: '第5-6节', time: '14:00-15:40' },
  { key: 4, label: '第7-8节', time: '16:00-17:40' },
  { key: 5, label: '第9-10节', time: '19:00-20:40' },
]

function getCurrentWeek() {
  // 假设学期从2026-02-17开始（春节后第一周）
  const start = new Date('2026-02-17')
  const now = new Date()
  const diff = Math.floor((now - start) / (1000 * 60 * 60 * 24))
  return Math.max(1, Math.min(20, Math.floor(diff / 7) + 1))
}

function getSlotCourses(day, slot) {
  return courses.value.filter(c => {
    if (c.weekDay !== weekNames[day]) return false
    // 根据时间段匹配
    const start = c.startTime?.substring(0,5)
    const slotStart = timeSlots[slot - 1]?.time?.substring(0,5)
    if (!start || !slotStart) return false
    // 简单匹配：课程开始时间与时段开始时间匹配
    const courseStartMin = parseInt(start.substring(0,2)) * 60 + parseInt(start.substring(3,5))
    const slotStartMin = parseInt(slotStart.substring(0,2)) * 60 + parseInt(slotStart.substring(3,5))
    const diff = Math.abs(courseStartMin - slotStartMin)
    // 在30分钟内认为是该时段的课程
    return diff <= 30
  })
}

function showCourseDetail(c) {
  detailCourse.value = c
  detailVisible.value = true
}

function getWeekDate(dayOfWeek) {
  const now = new Date()
  const day = now.getDay()
  const diff = dayOfWeek - day
  const d = new Date(now)
  d.setDate(d.getDate() + diff)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

onMounted(async () => {
  const now = new Date()
  todayDay.value = now.getDay()
  currentWeek.value = getCurrentWeek()

  // 计算本周日期
  weekDays.value = weekNames.map((label, i) => {
    const d = new Date(now)
    d.setDate(d.getDate() + (i - now.getDay()))
    return {
      key: i,
      label,
      date: `${d.getMonth() + 1}/${d.getDate()}`
    }
  })

  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const classId = userInfo.classId || 1
    const res = await getCoursesByClassId(classId)
    const records = res.data || []
    courses.value = records.filter(c =>
      c.startWeek <= currentWeek.value && c.endWeek >= currentWeek.value
    )
  } catch (e) {
    console.error('加载课程失败', e)
  }
})
</script>

<style scoped>
.schedule-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: 600; color: var(--c-text); }
.header-right { display: flex; align-items: center; gap: 12px; }
.week-info { font-size: 14px; color: var(--c-text-tertiary); }

.timetable-wrapper { overflow-x: auto; }
.timetable {
  width: 100%; border-collapse: collapse;
  font-size: 13px; min-width: 800px;
}
.timetable th {
  background: var(--c-primary); color: #fff; padding: 10px 4px;
  text-align: center; font-weight: 500; width: 13%;
}
.timetable th.today { background: var(--c-primary-dark); }
.timetable th.time-col { width: 9%; }
.date-sm { font-size: 11px; opacity: 0.8; }

.time-cell {
  text-align: center; padding: 8px 4px;
  background: var(--c-bg-alt); border: 1px solid var(--c-border);
  vertical-align: middle;
}
.slot-num { font-size: 12px; font-weight: 600; color: var(--c-text-secondary); }
.slot-time { font-size: 11px; color: var(--c-text-tertiary); }

.course-cell {
  border: 1px solid var(--c-border); padding: 3px;
  width: 13%; height: 80px; vertical-align: top;
}
.course-cell.today { background: var(--c-primary-bg); }

.course-block {
  background: linear-gradient(135deg, var(--c-primary-bg), rgba(0,122,255,0.08));
  border-left: 3px solid var(--c-primary);
  border-radius: 4px; padding: 4px 6px;
  margin-bottom: 2px; cursor: pointer;
  transition: all .2s;
}
.course-block:hover { transform: translateY(-1px); box-shadow: 0 2px 6px var(--c-shadow); }
.cb-name { font-size: 12px; font-weight: 600; color: var(--c-text); }
.cb-room { font-size: 11px; color: var(--c-text-secondary); }
.cb-teacher { font-size: 10px; color: var(--c-text-tertiary); }

.cd-row { display: flex; padding: 8px 0; border-bottom: 1px solid var(--c-border-light); }
.cd-row:last-child { border-bottom: none; }
.cd-label { width: 80px; color: var(--c-text-tertiary); font-size: 13px; }
</style>
