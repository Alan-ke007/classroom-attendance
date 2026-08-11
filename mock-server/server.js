/**
 * 课堂智能考勤系统 —— 演示后端（mock-server）
 * 零依赖 Node http 实现，与前端 REST 契约一致，用于本地一键演示全栈交互。
 * 真实后端为 Spring Boot（已 push 至 GitHub），此文件仅作沙箱/无 JDK+MySQL 环境的演示落地。
 *
 * 启动： node mock-server/server.js   （默认 8080）
 */
const http = require('http')
const { URL } = require('url')

const PORT = process.env.MOCK_PORT || 8080
const MOCK_ADMIN = 'mock-jwt-admin-demo'
const MOCK_STUDENT = 'mock-jwt-student-demo'

const adminUser = {
  userId: 1, username: 'admin', realName: '系统管理员',
  role: 'admin', studentId: null, classId: null
}
const studentUser = {
  userId: 1001, username: 'student', realName: '张同学',
  role: 'student', studentId: '20230101', classId: 1
}

function ok(res, data, cookie) {
  const headers = { 'Content-Type': 'application/json; charset=utf-8' }
  if (cookie) headers['Set-Cookie'] = cookie
  res.writeHead(200, headers)
  res.end(JSON.stringify({ code: 200, data, msg: 'success' }))
}

// ---------- 示例数据 ----------
const classes = [
  { id: 1, name: '计算机2301' },
  { id: 2, name: '软件2302' }
]
const courses = [
  { id: 1, name: '高等数学', teacher: '李老师', classId: 1, classroom: 'A301', time: '周一 1-2节' },
  { id: 2, name: '数据结构', teacher: '王老师', classId: 1, classroom: 'B205', time: '周二 3-4节' },
  { id: 3, name: '操作系统', teacher: '赵老师', classId: 2, classroom: 'C102', time: '周三 5-6节' }
]
const students = [
  { id: 1001, name: '张同学', studentId: '20230101', classId: 1 },
  { id: 1002, name: '李同学', studentId: '20230102', classId: 1 },
  { id: 1003, name: '王同学', studentId: '20230103', classId: 2 },
  { id: 1004, name: '赵同学', studentId: '20230104', classId: 2 }
]
const behaviors = ['睡觉', '玩手机', '吃东西', '举手发言', '低头']
const statuses = ['正常', '迟到', '缺勤']
const faceStatuses = ['NEED_REVIEW', 'VERIFIED', 'UNMATCHED']

function pick(arr, i) { return arr[i % arr.length] }
function rnd(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min }

function buildAttendance(n = 14) {
  const list = []
  for (let i = 0; i < n; i++) {
    const s = pick(students, i)
    const c = pick(courses, i)
    const fs = pick(faceStatuses, i)
    list.push({
      id: i + 1,
      studentName: s.name,
      studentId: s.studentId,
      courseName: c.name,
      className: pick(classes, s.classId - 1).name,
      date: '2026-08-11',
      time: `0${rnd(8, 11)}:${rnd(10, 59)}`,
      status: pick(statuses, i),
      faceStatus: fs,
      confidence: fs === 'NEED_REVIEW' ? null : Number((0.82 + Math.random() * 0.17).toFixed(3)),
      snapshotUrl: '/mock/face/' + (i + 1) + '.jpg'
    })
  }
  return list
}
const attendanceList = buildAttendance()

function dashboard() {
  const needReview = attendanceList.filter(a => a.faceStatus === 'NEED_REVIEW').length
  const abnormal = attendanceList.filter(a => a.status !== '正常').length
  return {
    totalStudents: students.length,
    totalCourses: courses.length,
    todayAttendanceRate: 0.93,
    abnormalCount: abnormal,
    pendingFaceReviews: needReview,
    trend: Array.from({ length: 7 }, (_, i) => ({
      date: `0${8 - i}/1${1 - i > 0 ? 1 - i : ''}`.replace('//', '/'),
      rate: Number((0.85 + Math.random() * 0.12).toFixed(2))
    })).reverse(),
    recentBehaviors: buildAttendance(5).map(a => ({
      studentName: a.studentName, courseName: a.courseName,
      behavior: pick(behaviors, a.id), time: a.time, confidence: Number((0.7 + Math.random() * 0.29).toFixed(2))
    }))
  }
}

// ---------- 路由 ----------
const handlers = {
  'POST /api/auth/login'(req, res, body) {
    const username = String(body.username || '').toLowerCase()
    const role = username.includes('student') ? 'student' : 'admin'
    const user = role === 'student' ? studentUser : adminUser
    const token = role === 'student' ? MOCK_STUDENT : MOCK_ADMIN
    const cookie = `token=${token}; HttpOnly; Path=/; SameSite=Lax; Max-Age=86400`
    ok(res, user, cookie)
  },
  'GET /api/auth/info'(req, res) {
    const cookie = req.headers.cookie || ''
    const m = cookie.match(/token=([^;]+)/)
    const token = m ? m[1] : ''
    const role = token.includes('student') ? 'student' : 'admin'
    ok(res, role === 'student' ? studentUser : adminUser)
  },
  'POST /api/auth/logout'(req, res) {
    const cookie = 'token=; HttpOnly; Path=/; Max-Age=0'
    ok(res, null, cookie)
  },
  'GET /api/captcha/generate'(req, res) {
    ok(res, { captcha: '8888', captchaId: 'demo' })
  },
  'GET /api/attendance/list'(req, res) {
    ok(res, attendanceList)
  },
  'GET /api/attendance/face-review'(req, res) {
    const list = attendanceList
      .filter(a => a.faceStatus === 'NEED_REVIEW')
      .map(a => ({
        ...a,
        faceImageUrl: a.snapshotUrl,
        extractedFeature: Array.from({ length: 8 }, () => Number(Math.random().toFixed(3)))
      }))
    ok(res, list)
  },
  'GET /api/statistics/dashboard'(req, res) { ok(res, dashboard()) },
  'GET /api/statistics/pending-tasks'(req, res) {
    ok(res, [
      { id: 1, type: '人脸复核', title: '3 条考勤记录待人工复核', count: 3, urgency: 'high' },
      { id: 2, type: '行为预警', title: '今日 2 起课堂异常行为', count: 2, urgency: 'medium' }
    ])
  },
  'GET /api/statistics/attendance'(req, res) {
    ok(res, { rate: 0.93, normal: 11, late: 2, absent: 1, total: 14 })
  },
  'GET /api/statistics/ranking'(req, res) {
    ok(res, students.map((s, i) => ({ rank: i + 1, name: s.name, rate: Number((0.8 + Math.random() * 0.19).toFixed(2)) })))
  },
  'GET /api/statistics/credit-ranking'(req, res) {
    ok(res, students.map((s, i) => ({ rank: i + 1, name: s.name, score: rnd(80, 100) })))
  },
  'GET /api/statistics/today-schedule'(req, res) {
    ok(res, courses.map(c => ({ courseName: c.name, teacher: c.teacher, classroom: c.classroom, time: c.time })))
  },
  'GET /api/course/list'(req, res) { ok(res, courses) },
  'GET /api/behavior/list'(req, res) {
    ok(res, buildAttendance(8).map(a => ({
      id: a.id, studentName: a.studentName, courseName: a.courseName,
      behavior: pick(behaviors, a.id), time: a.time, confidence: Number((0.7 + Math.random() * 0.29).toFixed(2)),
      snapshotUrl: a.snapshotUrl
    })))
  },
  'GET /api/announcement/active'(req, res) {
    ok(res, [
      { id: 1, title: '新学期考勤系统升级通知', content: '已支持人脸复核与行为检测大屏。', publishTime: '2026-08-10' },
      { id: 2, title: '摄像头建档指引', content: '请于管理端完成人脸建档后再启用自动签到。', publishTime: '2026-08-09' }
    ])
  },
  'GET /api/announcement/list'(req, res) {
    ok(res, [
      { id: 1, title: '新学期考勤系统升级通知', content: '已支持人脸复核与行为检测大屏。', publishTime: '2026-08-10' },
      { id: 2, title: '摄像头建档指引', content: '请于管理端完成人脸建档后再启用自动签到。', publishTime: '2026-08-09' }
    ])
  },
  'GET /api/file/list'(req, res) {
    ok(res, [
      { id: 1, name: '2026春季考勤汇总.xlsx', size: '24KB', uploadTime: '2026-08-08' },
      { id: 2, name: '人脸特征库备份.zip', size: '1.2MB', uploadTime: '2026-08-07' }
    ])
  },
  'GET /api/algorithm/health'(req, res) {
    ok(res, { status: 'ok', model_loaded: true, service: 'mock-algorithm' })
  },
  'POST /api/algorithm/detect'(req, res) {
    ok(res, {
      detected: true,
      behaviors: [
        { type: pick(behaviors, rnd(0, 4)), confidence: Number((0.7 + Math.random() * 0.29).toFixed(2)) }
      ],
      timestamp: new Date().toISOString()
    })
  }
}

// 动态前缀路由（含路径参数）
const prefixHandlers = [
  ['/api/student/credit-score/', (req, res) => ok(res, { score: rnd(80, 100), level: '良好' })],
  ['/api/attendance/rate/', (req, res) => ok(res, { rate: Number((0.8 + Math.random() * 0.19).toFixed(2)) })],
  ['/api/attendance/student/', (req, res) => ok(res, attendanceList.slice(0, 6))],
  ['/api/statistics/student/', (req, res) => ok(res, { rate: 0.95, credit: 90 })],
  ['/api/course/class/', (req, res) => ok(res, courses)],
  ['/api/attendance/class/', (req, res) => ok(res, attendanceList)],
  ['/api/attendance/course/', (req, res) => ok(res, attendanceList)],
  ['/api/announcement/', (req, res) => ok(res, { id: 1, title: '通知', content: '演示数据' })],
  ['/api/file/download/', (req, res) => ok(res, { url: '/mock/file.bin' })],
  ['/api/attendance/', (req, res) => ok(res, attendanceList)]
]

const server = http.createServer((req, res) => {
  res.setHeader('Access-Control-Allow-Origin', req.headers.origin || '*')
  res.setHeader('Access-Control-Allow-Credentials', 'true')
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,DELETE,OPTIONS')
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type,Authorization')
  if (req.method === 'OPTIONS') { res.writeHead(204); res.end(); return }

  const u = new URL(req.url, 'http://localhost')
  const path = u.pathname
  const method = req.method
  let body = ''
  req.on('data', c => { body += c })
  req.on('end', () => {
    let parsed = {}
    try { parsed = body ? JSON.parse(body) : {} } catch (e) { /* ignore */ }
    const key = method + ' ' + path
    try {
      if (handlers[key]) return handlers[key](req, res, parsed)
      for (const [p, fn] of prefixHandlers) {
        if (path.startsWith(p)) return fn(req, res, parsed, path)
      }
      // 兜底：返回空成功，避免前端页面报错
      ok(res, [])
    } catch (err) {
      console.error('[mock-server] handler error:', err && err.message)
      if (!res.headersSent) ok(res, [])
    }
  })
})

server.on('upgrade', (req, socket) => {
  // 演示后端不实现 WebSocket，直接关闭，避免前端挂起重连打印噪声
  socket.destroy()
})

server.listen(PORT, '0.0.0.0', () => {
  console.log(`[mock-server] 演示后端已启动: http://localhost:${PORT}`)
})
