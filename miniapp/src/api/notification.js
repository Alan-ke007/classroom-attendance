import { get, put } from './request'

export function getNotificationList(params = {}) {
  const { pageNum = 1, pageSize = 20, ...rest } = params
  return get('/notification/list', { pageNum, pageSize, ...rest })
}

export function getUnreadCount(userId) {
  return get('/notification/unread-count', { userId })
}

export function markRead(id) {
  return put(`/notification/read/${id}`)
}

export function markAllRead(userId) {
  return put('/notification/read-all', { userId })
}
