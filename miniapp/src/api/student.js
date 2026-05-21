import { get, post, put, del } from './request'

export function getStudentList(params = {}) {
  const { pageNum = 1, pageSize = 20, ...rest } = params
  return get('/student/list', { pageNum, pageSize, ...rest })
}

export function getAllStudents() {
  return get('/student/all')
}

export function getStudentById(id) {
  return get(`/student/${id}`)
}

export function addStudent(data) {
  return post('/student', data)
}

export function updateStudent(id, data) {
  return put(`/student/${id}`, data)
}

export function deleteStudent(id) {
  return del(`/student/${id}`)
}

export function saveStudentFace(id, faceImage) {
  return post(`/student/${id}/face`, { faceImage })
}
