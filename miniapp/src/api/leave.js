import { get, post, put } from './request'

export function getLeaveList(params = {}) {
  const { pageNum = 1, pageSize = 20, ...rest } = params
  return get('/leave/list', { pageNum, pageSize, ...rest })
}

export function applyLeave(data) {
  return post('/leave/apply', data)
}

export function approveLeave(id, data) {
  return put(`/leave/approve/${id}`, data)
}

export function rejectLeave(id, data) {
  return put(`/leave/reject/${id}`, data)
}
