import request from '@/utils/request'

export function getDashboardStats() {
  return request({
    url: '/statistics/dashboard',
    method: 'get'
  })
}

export function getAttendanceStats(startDate, endDate) {
  return request({
    url: '/statistics/attendance',
    method: 'get',
    params: { startDate, endDate }
  })
}

export function getStudentAttendanceRanking(classId) {
  return request({
    url: '/statistics/ranking',
    method: 'get',
    params: { classId }
  })
}
