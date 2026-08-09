import { get } from './request'

export function getDashboardStats() {
  return get('/statistics/dashboard')
}

export function getAttendanceStats(startDate, endDate) {
  return get('/statistics/attendance', { startDate, endDate })
}

export function getStudentDashboardStats(studentId) {
  return get(`/statistics/student/${studentId}`)
}

export function getStudentAttendanceRanking(classId) {
  return get('/statistics/ranking', { classId })
}
