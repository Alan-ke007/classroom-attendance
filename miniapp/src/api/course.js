import { get, post, put, del } from './request'

export function getCourseList(params = {}) {
  const { pageNum = 1, pageSize = 20, ...rest } = params
  return get('/course/list', { pageNum, pageSize, ...rest })
}

export function getAllCourses() {
  return get('/course/all')
}

export function getCoursesByClassId(classId) {
  return get(`/course/class/${classId}`)
}

export function getCourseById(id) {
  return get(`/course/${id}`)
}

export function addCourse(data) {
  return post('/course', data)
}

export function updateCourse(id, data) {
  return put(`/course/${id}`, data)
}

export function deleteCourse(id) {
  return del(`/course/${id}`)
}
