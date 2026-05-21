import request from '@/utils/request'

export function getAttendanceList(params) {
  return request({ url: '/attendance/list', method: 'get', params })
}

export function getAttendanceById(id) {
  return request({ url: `/attendance/${id}`, method: 'get' })
}

export function addAttendance(data) {
  return request({ url: '/attendance', method: 'post', data })
}

export function updateAttendance(id, data) {
  return request({ url: `/attendance/${id}`, method: 'put', data })
}

export function deleteAttendance(id) {
  return request({ url: `/attendance/${id}`, method: 'delete' })
}

export function getAttendancesByStudentId(studentId) {
  return request({ url: `/attendance/student/${studentId}`, method: 'get' })
}

export function getAttendancesByClassId(classId) {
  return request({ url: `/attendance/class/${classId}`, method: 'get' })
}

export function getAttendancesByCourseId(courseId) {
  return request({ url: `/attendance/course/${courseId}`, method: 'get' })
}

export function getAttendancesByDateRange(startDate, endDate) {
  return request({ url: '/attendance/range', method: 'get', params: { startDate, endDate } })
}

export function calculateAttendanceRate(studentId, startDate, endDate) {
  return request({ url: `/attendance/rate/${studentId}`, method: 'get', params: { startDate, endDate } })
}
