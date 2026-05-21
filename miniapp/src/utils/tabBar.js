// 学生 tab 配置
const studentTabs = [
  { index: 0, text: '首页', pagePath: 'pages/student/home/index', iconPath: 'static/icons/home.png', selectedIconPath: 'static/icons/home-active.png' },
  { index: 1, text: '课程', pagePath: 'pages/student/schedule/index', iconPath: 'static/icons/course.png', selectedIconPath: 'static/icons/course-active.png' },
  { index: 2, text: '考勤', pagePath: 'pages/student/attendance/index', iconPath: 'static/icons/attendance.png', selectedIconPath: 'static/icons/attendance-active.png' },
  { index: 3, text: '行为', pagePath: 'pages/student/behavior/index', iconPath: 'static/icons/behavior.png', selectedIconPath: 'static/icons/behavior-active.png' }
]

// 教师/管理员 tab 配置
const teacherTabs = [
  { index: 0, text: '首页', pagePath: 'pages/teacher/home/index', iconPath: 'static/icons/home.png', selectedIconPath: 'static/icons/home-active.png' },
  { index: 1, text: '课程', pagePath: 'pages/teacher/course/index', iconPath: 'static/icons/course.png', selectedIconPath: 'static/icons/course-active.png' },
  { index: 2, text: '考勤', pagePath: 'pages/teacher/attendance/index', iconPath: 'static/icons/attendance.png', selectedIconPath: 'static/icons/attendance-active.png' },
  { index: 3, text: '学生', pagePath: 'pages/teacher/student/index', iconPath: 'static/icons/behavior.png', selectedIconPath: 'static/icons/behavior-active.png' }
]

// 根据角色切换 tabBar（仅在 tabBar 页面调用）
export function switchTabBar(role) {
  const tabs = (role === 'teacher' || role === 'admin') ? teacherTabs : studentTabs
  const pages = getCurrentPages()
  if (!pages.length) return
  const currentPage = pages[pages.length - 1]
  if (!currentPage) return
  const route = currentPage.route || ''
  // 只在 tabBar 页面才调用 setTabBarItem
  const isTabPage = tabs.some(t => t.pagePath === route)
  if (!isTabPage) return
  tabs.forEach(tab => {
    try {
      uni.setTabBarItem(tab)
    } catch (e) {
      // 忽略非 tabBar 页面错误
    }
  })
}
