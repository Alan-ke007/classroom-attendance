import { get, post, put, del } from './request'

export function getBehaviorList(params = {}) {
  const { pageNum = 1, pageSize = 20, ...rest } = params
  return get('/behavior/list', { pageNum, pageSize, ...rest })
}

export function getBehaviorById(id) {
  return get(`/behavior/${id}`)
}

export function getBehaviorsByStudentId(studentId) {
  return get(`/behavior/student/${studentId}`)
}

export function getBehaviorsByClassId(classId) {
  return get(`/behavior/class/${classId}`)
}

export function getBehaviorsByType(behaviorType) {
  return get(`/behavior/type/${behaviorType}`)
}

export function getBehaviorsByTimeRange(startTime, endTime) {
  return get('/behavior/range', { startTime, endTime })
}

export function markAsHandled(id, handleRemark) {
  return put(`/behavior/handle/${id}`, handleRemark)
}

export function addBehavior(data) {
  return post('/behavior', data)
}

export function deleteBehavior(id) {
  return del(`/behavior/${id}`)
}
