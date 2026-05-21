import request from '@/utils/request'

export function getConversations() {
  return request.get('/chat/conversations')
}

export function getMessages(otherUserId, pageNum = 1, pageSize = 20) {
  return request.get('/chat/messages', { params: { otherUserId, pageNum, pageSize } })
}

export function sendMessage(data) {
  return request.post('/chat/send', data)
}

export function getUnreadCount() {
  return request.get('/chat/unread-count')
}

export function markAsRead(otherUserId) {
  return request.put(`/chat/read/${otherUserId}`)
}

export function searchUsers(keyword, role) {
  return request.get('/chat/search-users', { params: { keyword, role } })
}
