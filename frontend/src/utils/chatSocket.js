const HEARTBEAT_INTERVAL = 30000
const MAX_RECONNECT = 5

class ChatSocket {
  constructor() {
    this.ws = null
    this.listeners = {}
    this._reconnectAttempts = 0
    this._heartbeatTimer = null
    this._reconnectTimer = null
    this._intentionalClose = false
  }

  connect() {
    // ② 安全：未登录不连接（软判断）；登录态以 httpOnly Cookie 为准，浏览器握手时自动携带。
    if (!localStorage.getItem('userInfo')) return
    this._intentionalClose = false
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    // 同源连接：确保与页面同源，浏览器才会在 WS 握手时自动带上 httpOnly Cookie（经由 Vite/网关代理转发）。
    const url = `${protocol}//${location.host}/ws/chat`

    try {
      this.ws = new WebSocket(url)
    } catch (e) {
      this._scheduleReconnect()
      return
    }

    this.ws.onopen = () => {
      this._reconnectAttempts = 0
      this._startHeartbeat()
      this._emit('connected')
    }

    this.ws.onclose = () => {
      this._stopHeartbeat()
      if (!this._intentionalClose) this._scheduleReconnect()
      this._emit('disconnected')
    }

    this.ws.onerror = () => {
      this._emit('error')
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'pong') return
        if (data.type === 'chat_message_ack') this._emit('messageAck', data.message, data.clientMsgId)
        else if (data.type === 'new_message') this._emit('newMessage', data.message)
        else if (data.type === 'mark_read_ack') this._emit('readAck', data.otherUserId)
        else if (data.type === 'connected') this._emit('connected')
        else this._emit('message', data)
      } catch (e) {
        console.error('Chat WS parse error:', e)
      }
    }
  }

  disconnect() {
    this._intentionalClose = true
    this._stopHeartbeat()
    if (this._reconnectTimer) { clearTimeout(this._reconnectTimer); this._reconnectTimer = null }
    if (this.ws) { try { this.ws.close() } catch (e) { /* ignore */ }; this.ws = null }
  }

  sendMessage(receiverId, content, clientMsgId) {
    this._send({ type: 'chat_message', receiverId, content, clientMsgId })
  }

  markRead(otherUserId) {
    this._send({ type: 'mark_read', otherUserId })
  }

  on(event, callback) {
    if (!this.listeners[event]) this.listeners[event] = []
    this.listeners[event].push(callback)
  }

  off(event, callback) {
    if (!this.listeners[event]) return
    this.listeners[event] = this.listeners[event].filter(cb => cb !== callback)
  }

  _send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
    }
  }

  _emit(event, ...args) {
    (this.listeners[event] || []).forEach(cb => { try { cb(...args) } catch (e) { /* ignore */ } })
  }

  _startHeartbeat() {
    this._stopHeartbeat()
    this._heartbeatTimer = setInterval(() => this._send({ type: 'ping' }), HEARTBEAT_INTERVAL)
  }

  _stopHeartbeat() {
    if (this._heartbeatTimer) { clearInterval(this._heartbeatTimer); this._heartbeatTimer = null }
  }

  _scheduleReconnect() {
    if (this._reconnectAttempts >= MAX_RECONNECT) {
      this._emit('reconnectFailed')
      return
    }
    this._reconnectAttempts++
    const delay = Math.min(3000 * this._reconnectAttempts, 15000)
    this._reconnectTimer = setTimeout(() => this.connect(), delay)
  }

  get ready() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}

const chatSocket = new ChatSocket()
export default chatSocket
