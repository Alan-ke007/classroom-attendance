import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue')
  },
  // Student routes
  {
    path: '/student',
    component: () => import('@/views/student/StudentLayout.vue'),
    meta: { requiresAuth: true, role: 'student' },
    children: [
      { path: 'home', name: 'StudentHome', component: () => import('@/views/student/StudentHome.vue') },
      { path: 'schedule', name: 'StudentSchedule', component: () => import('@/views/student/StudentSchedule.vue') },
      { path: 'attendance', name: 'StudentAttendance', component: () => import('@/views/student/StudentAttendance.vue') },
      { path: 'behavior', name: 'StudentBehavior', component: () => import('@/views/student/StudentBehavior.vue') },
      { path: 'leave', name: 'StudentLeave', component: () => import('@/views/student/StudentLeave.vue') },
      { path: 'qrscan', name: 'StudentQRScan', component: () => import('@/views/student/StudentQRScan.vue') },
      { path: 'notification', name: 'StudentNotification', component: () => import('@/views/NotificationCenter.vue') },
      { path: 'chat', name: 'StudentChat', component: () => import('@/views/chat/ChatPanel.vue') },
      { path: 'weekly-report', name: 'StudentWeeklyReport', component: () => import('@/views/student/StudentWeeklyReport.vue') },
      { path: 'credit', name: 'StudentCredit', component: () => import('@/views/student/StudentCredit.vue') }
    ]
  },
  // Admin/Teacher routes
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true, role: ['admin', 'teacher'] },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'class',
        name: 'ClassManagement',
        component: () => import('@/views/ClassManagement.vue')
      },
      {
        path: 'student',
        name: 'StudentManagement',
        component: () => import('@/views/StudentManagement.vue')
      },
      {
        path: 'course',
        name: 'CourseManagement',
        component: () => import('@/views/CourseManagement.vue')
      },
      {
        path: 'attendance/list',
        name: 'AttendanceList',
        component: () => import('@/views/AttendanceList.vue')
      },
      {
        path: 'behavior/list',
        name: 'BehaviorList',
        component: () => import('@/views/BehaviorList.vue')
      },
      {
        path: 'behavior/monitor',
        name: 'BehaviorMonitor',
        component: () => import('@/views/BehaviorMonitor.vue')
      },
      {
        path: 'algorithm/test',
        name: 'AlgorithmTest',
        component: () => import('@/views/AlgorithmTest.vue')
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue')
      },
      {
        path: 'file',
        name: 'FileManagement',
        component: () => import('@/views/FileManagement.vue')
      },
      {
        path: 'leave',
        name: 'LeaveManagement',
        component: () => import('@/views/LeaveManagement.vue')
      },
      {
        path: 'qrcheckin',
        name: 'QRCheckin',
        component: () => import('@/views/QRCheckin.vue')
      },
      {
        path: 'import',
        name: 'ImportData',
        component: () => import('@/views/ImportData.vue')
      },
      {
        path: 'notification',
        name: 'NotificationCenter',
        component: () => import('@/views/NotificationCenter.vue')
      },
      {
        path: 'chat',
        name: 'ChatPanel',
        component: () => import('@/views/chat/ChatPanel.vue')
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue')
      },
      {
        path: 'log',
        name: 'LogManagement',
        component: () => import('@/views/LogManagement.vue')
      },
      {
        path: 'announcement',
        name: 'AnnouncementList',
        component: () => import('@/views/AnnouncementList.vue')
      }
    ]
  },
  // 403 Forbidden
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/Forbidden.vue')
  },
  // 404 catch-all
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫（仅作 UX 层拦截）
// ⚠️ 注意：前端路由守卫只负责跳转体验，绝不承担鉴权可信度。
// ② 安全：登录态以 httpOnly Cookie 为准（JS 不可读），此处用 localStorage 中的 userInfo 软状态做 UX 判断；
// 真实鉴权/ RBAC 由后端接口层强制校验（Cookie + 401）。若 Cookie 失效，首个接口 401 即触发重新登录。
router.beforeEach((to, from, next) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const role = userInfo.role

  if (to.meta.requiresAuth && !role) {
    next('/login')
    return
  }

  if (to.meta.role) {
    const allowedRoles = Array.isArray(to.meta.role) ? to.meta.role : [to.meta.role]
    if (!allowedRoles.includes(role)) {
      next('/403')
      return
    }
  }

  if ((to.path === '/login' || to.path === '/register') && role) {
    next(role === 'student' ? '/student/home' : '/dashboard')
    return
  }

  next()
})

export default router
