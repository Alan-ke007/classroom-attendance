import { get, post, put, del } from './request'

export function getClassList(params = {}) {
  const { pageNum = 1, pageSize = 20, ...rest } = params
  return get('/class/list', { pageNum, pageSize, ...rest })
}

export function getAllClasses() {
  return get('/class/all')
}

export function getClassById(id) {
  return get(`/class/${id}`)
}

export function addClass(data) {
  return post('/class', data)
}

export function updateClass(id, data) {
  return put(`/class/${id}`, data)
}

export function deleteClass(id) {
  return del(`/class/${id}`)
}
