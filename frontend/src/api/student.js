import request from '@/utils/request'

export function getCourseList(params) {
  return request({ url: '/course/list', method: 'get', params })
}

export function getCoursesByClassId(classId) {
  return request({ url: `/course/class/${classId}`, method: 'get' })
}

export function getAttendanceList(params) {
  return request({ url: '/attendance/list', method: 'get', params })
}

export function createAttendance(data) {
  return request({ url: '/attendance', method: 'post', data })
}

export function getBehaviorList(params) {
  return request({ url: '/behavior/list', method: 'get', params })
}

export function getDashboardStats() {
  return request({ url: '/statistics/dashboard', method: 'get' })
}

export function getStudentDashboardStats(studentId) {
  return request({ url: `/statistics/student/${studentId}`, method: 'get' })
}

export function getAttendanceStats(params) {
  return request({ url: '/statistics/attendance', method: 'get', params })
}
