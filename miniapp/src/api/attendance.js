import { get, post, put, del } from './request'

export function getAttendanceList(params = {}) {
  const { pageNum = 1, pageSize = 20, ...rest } = params
  return get('/attendance/list', { pageNum, pageSize, ...rest })
}

export function getAttendanceById(id) {
  return get(`/attendance/${id}`)
}

export function getAttendancesByStudentId(studentId) {
  return get(`/attendance/student/${studentId}`)
}

export function getAttendancesByClassId(classId) {
  return get(`/attendance/class/${classId}`)
}

export function getAttendancesByCourseId(courseId) {
  return get(`/attendance/course/${courseId}`)
}

export function getAttendancesByDateRange(startDate, endDate) {
  return get('/attendance/range', { startDate, endDate })
}

export function calculateAttendanceRate(studentId, startDate, endDate) {
  return get(`/attendance/rate/${studentId}`, { startDate, endDate })
}

export function addAttendance(data) {
  return post('/attendance', data)
}

export function updateAttendance(id, data) {
  return put(`/attendance/${id}`, data)
}

export function deleteAttendance(id) {
  return del(`/attendance/${id}`)
}
