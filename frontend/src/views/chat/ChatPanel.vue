<template>
  <div class="chat-panel">
    <div class="chat-layout">
      <!-- 左侧对话列表 -->
      <div class="conv-sidebar">
        <div class="conv-header">
          <span>消息</span>
          <el-button size="small" circle @click="showNewChat = true">
            <el-icon><Plus /></el-icon>
          </el-button>
        </div>
        <div v-if="loadingConvs" class="conv-loading">加载中...</div>
        <div v-else-if="conversations.length === 0" class="conv-empty">
          <el-empty description="暂无对话" :image-size="80" />
        </div>
        <div v-else class="conv-list">
          <div
            v-for="conv in conversations"
            :key="conv.otherUserId"
            class="conv-item"
            :class="{ active: activeUserId === conv.otherUserId }"
            @click="openConversation(conv)"
          >
            <el-avatar :size="40" :src="conv.otherUserAvatar" />
            <div class="conv-info">
              <div class="conv-top">
                <span class="conv-name">{{ conv.otherUserName }}</span>
                <span class="conv-time">{{ fmtTime(conv.lastMessageTime) }}</span>
              </div>
              <div class="conv-bottom">
                <span class="conv-preview">{{ truncate(conv.lastMessage, 30) }}</span>
                <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧消息区 -->
      <div class="msg-area" v-if="activeUserId">
        <div class="msg-header">
          <span>{{ activeUserName }}</span>
        </div>
        <div class="msg-list" ref="msgListRef">
          <div v-if="loadingMsgs" class="msg-loading">加载中...</div>
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="msg-item"
            :class="{ mine: msg.senderId === myUserId }"
          >
            <el-avatar :size="32" :src="msg.senderAvatar" />
            <div class="msg-bubble" :class="{ failed: msg._sendFailed }">
              <div class="msg-text">{{ msg.content }}</div>
              <div class="msg-time">
                <span v-if="msg._sendFailed" class="msg-status failed">发送失败</span>
                <span v-else-if="msg._tempId < 0 && !msg.id" class="msg-status sending">发送中...</span>
                {{ fmtTime(msg.createTime) }}
              </div>
            </div>
          </div>
        </div>
        <div class="msg-input">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入消息..."
            resize="none"
            maxlength="500"
            show-word-limit
            @keydown.enter.exact.prevent="send"
          />
          <el-button type="primary" :disabled="!inputText.trim()" @click="send">发送</el-button>
        </div>
      </div>

      <div v-else class="msg-placeholder">
        <el-empty description="选择一个对话开始聊天" :image-size="100" />
      </div>
    </div>
  </div>

  <!-- 新对话弹窗 -->
  <el-dialog v-model="showNewChat" title="发起新对话" width="420px" :close-on-click-modal="true" @closed="searchResults=[]; searchKeyword='';">
    <div class="new-chat-body">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名或姓名..."
        clearable
        @input="debouncedSearch"
        @keydown.enter="doSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div v-if="searchingUsers" class="search-loading">搜索中...</div>
      <div v-else-if="searchResults.length === 0 && searchKeyword" class="search-empty">
        <el-empty description="未找到用户" :image-size="60" />
      </div>
      <div v-else class="search-results">
        <div
          v-for="user in searchResults"
          :key="user.id"
          class="search-user-item"
          @click="startConversation(user)"
        >
          <el-avatar :size="36" :src="user.avatar" />
          <div class="search-user-info">
            <span class="search-user-name">{{ user.realName || user.username }}</span>
            <span class="search-user-role">{{ user.role === 'teacher' ? '教师' : user.role === 'admin' ? '管理员' : '学生' }}</span>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getConversations, getMessages, sendMessage as apiSendMessage, markAsRead as apiMarkAsRead, searchUsers } from '@/api/chat'
import chatSocket from '@/utils/chatSocket'

const myUserId = ref(null)
const conversations = ref([])
const loadingConvs = ref(false)
const activeUserId = ref(null)
const activeUserName = ref('')
const messages = ref([])
const loadingMsgs = ref(false)
const inputText = ref('')
const msgListRef = ref(null)

// 新对话弹窗
const showNewChat = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const searchingUsers = ref(false)
let searchTimer = null

// 本地消息序列号，用于可靠匹配服务端确认
let msgSeq = 0
/** Map<tempId, Array<{msg, timeout}>> 待确认的消息 */
const pendingAcks = new Map()

function fmtTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }) + ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function truncate(s, n) { return s && s.length > n ? s.substring(0, n) + '...' : s }

function debouncedSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(doSearch, 300)
}

async function doSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) { searchResults.value = []; return }
  searchingUsers.value = true
  try {
    const res = await searchUsers(kw)
    searchResults.value = res.data || []
  } catch (e) { /* ignore */ }
  finally { searchingUsers.value = false }
}

function startConversation(user) {
  showNewChat.value = false
  // 如果已经有该用户的对话，直接打开
  const existing = conversations.value.find(c => c.otherUserId === user.id)
  if (existing) {
    openConversation(existing)
    return
  }
  // 否则创建一个临时的对话条目
  activeUserId.value = user.id
  activeUserName.value = user.realName || user.username
  messages.value = []
}

async function loadConversations() {
  loadingConvs.value = true
  try {
    const res = await getConversations()
    conversations.value = res.data || []
  } catch (e) { /* ignore */ }
  finally { loadingConvs.value = false }
}

async function openConversation(conv) {
  activeUserId.value = conv.otherUserId
  activeUserName.value = conv.otherUserName
  loadingMsgs.value = true
  try {
    const res = await getMessages(conv.otherUserId)
    const list = res.data?.records || []
    messages.value = list.reverse()
    await apiMarkAsRead(conv.otherUserId)
    if (chatSocket.ready) chatSocket.markRead(conv.otherUserId)
    conv.unreadCount = 0
    await nextTick()
    scrollToBottom()
  } finally { loadingMsgs.value = false }
}

/** 在消息列表中根据 tempId 替换为服务端确认的消息 */
function resolvePending(tempId, serverMsg) {
  const pending = pendingAcks.get(tempId)
  if (!pending) return
  clearTimeout(pending.timeout)
  pendingAcks.delete(tempId)
  const idx = messages.value.findIndex(m => m._tempId === tempId)
  if (idx >= 0) {
    // 保留 _tempId 标记已被替换，防止重复处理
    messages.value[idx] = { ...serverMsg, _tempId: tempId }
  }
}

/** 添加一条乐观消息并返回临时ID */
function addOptimisticMessage(text) {
  const tempId = --msgSeq
  const optimistic = {
    _tempId: tempId,
    id: tempId,
    senderId: myUserId.value,
    content: text,
    createTime: new Date().toISOString()
  }
  messages.value.push(optimistic)

  // 5秒后自动清理未确认的占位
  const timeout = setTimeout(() => {
    const pending = pendingAcks.get(tempId)
    if (pending) {
      const idx = messages.value.findIndex(m => m._tempId === tempId)
      if (idx >= 0) messages.value[idx] = { ...messages.value[idx], _sendFailed: true }
      pendingAcks.delete(tempId)
    }
  }, 5000)
  pendingAcks.set(tempId, { msg: optimistic, timeout })
  return tempId
}

async function send() {
  const text = inputText.value.trim()
  if (!text || !activeUserId.value) return
  inputText.value = ''

  const tempId = addOptimisticMessage(text)
  scrollToBottom()

  try {
    if (chatSocket.ready) {
      chatSocket.sendMessage(activeUserId.value, text, tempId)
    } else {
      // HTTP 降级：用响应数据替换占位消息
      const res = await apiSendMessage({ receiverId: activeUserId.value, content: text })
      if (res.code === 200 && res.data) {
        resolvePending(tempId, res.data)
      }
      loadConversations()
    }
  } catch (e) {
    // 标记发送失败
    const idx = messages.value.findIndex(m => m._tempId === tempId)
    if (idx >= 0) messages.value[idx] = { ...messages.value[idx], _sendFailed: true }
    ElMessage.error('发送失败')
  }
}

function scrollToBottom() {
  if (msgListRef.value) {
    nextTick(() => {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    })
  }
}

// WebSocket 事件
function onNewMessage(msg) {
  if (msg.senderId === activeUserId.value) {
    messages.value.push(msg)
    scrollToBottom()
  }
  loadConversations()
}

function onMessageAck(msg, clientMsgId) {
  if (clientMsgId != null) {
    resolvePending(clientMsgId, msg)
  } else {
    // 降级：通过内容匹配（兼容旧服务端）
    const idx = messages.value.findIndex(m =>
      m.content === msg.content && m._tempId < 0 && m.id < 0
    )
    if (idx >= 0) messages.value[idx] = msg
  }
  loadConversations()
}

onMounted(() => {
  const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
  myUserId.value = info.userId

  chatSocket.connect()
  chatSocket.on('newMessage', onNewMessage)
  chatSocket.on('messageAck', onMessageAck)
  chatSocket.on('connected', () => console.log('Chat WS connected'))

  loadConversations()
})

onUnmounted(() => {
  chatSocket.off('newMessage', onNewMessage)
  chatSocket.off('messageAck', onMessageAck)
  // 清理所有待确认消息的定时器
  for (const [_, pending] of pendingAcks) clearTimeout(pending.timeout)
  pendingAcks.clear()
})
</script>

<style scoped>
.chat-panel { height: calc(100vh - 120px); background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.chat-layout { display: flex; height: 100%; }

.conv-sidebar { width: 280px; border-right: 1px solid #f0f0f0; display: flex; flex-direction: column; }
.conv-header { padding: 16px 20px; font-size: 16px; font-weight: 600; color: #1d1d1f; border-bottom: 1px solid #f0f0f0; display: flex; align-items: center; justify-content: space-between; }
.conv-header .el-button { --el-button-size: 28px; }
.conv-loading, .msg-loading { padding: 40px; text-align: center; color: #86868b; font-size: 13px; }
.conv-empty { padding: 40px 0; }
.conv-list { flex: 1; overflow-y: auto; }
.conv-item { display: flex; align-items: center; gap: 12px; padding: 14px 20px; cursor: pointer; transition: background 0.2s; border-left: 3px solid transparent; }
.conv-item:hover { background: #f5f5f7; }
.conv-item.active { background: #e8f4fd; border-left-color: #007AFF; }
.conv-info { flex: 1; overflow: hidden; }
.conv-top, .conv-bottom { display: flex; justify-content: space-between; align-items: center; }
.conv-name { font-size: 14px; font-weight: 500; color: #1d1d1f; }
.conv-time { font-size: 11px; color: #86868b; }
.conv-preview { font-size: 13px; color: #86868b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 160px; }

.msg-area { flex: 1; display: flex; flex-direction: column; }
.msg-header { padding: 14px 20px; font-size: 15px; font-weight: 600; color: #1d1d1f; border-bottom: 1px solid #f0f0f0; }
.msg-list { flex: 1; overflow-y: auto; padding: 16px 20px; display: flex; flex-direction: column; gap: 12px; }
.msg-item { display: flex; align-items: flex-start; gap: 8px; }
.msg-item.mine { flex-direction: row-reverse; }
.msg-bubble { max-width: 65%; padding: 10px 14px; border-radius: 16px; font-size: 14px; line-height: 1.5; }
.mine .msg-bubble { background: #007AFF; color: #fff; border-bottom-right-radius: 6px; }
.msg-item:not(.mine) .msg-bubble { background: #f2f2f7; color: #1d1d1f; border-bottom-left-radius: 6px; }
.msg-time { font-size: 11px; color: #86868b; margin-top: 4px; }
.mine .msg-time { color: rgba(255,255,255,0.7); text-align: right; }
.msg-status { font-size: 11px; margin-right: 4px; }
.msg-status.sending { color: rgba(255,255,255,0.5); }
.msg-status.failed { color: #ff4d4f; }
.msg-bubble.failed { border: 1px solid #ff4d4f; }

.msg-input { display: flex; align-items: flex-end; gap: 8px; padding: 12px 20px; border-top: 1px solid #f0f0f0; }
.msg-input :deep(.el-textarea__inner) { border-radius: 12px; }
.msg-input .el-button { height: 40px; border-radius: 20px; padding: 0 24px; }

.msg-placeholder { flex: 1; display: flex; align-items: center; justify-content: center; }

:deep(.el-badge__content.is-fixed) { top: 0; right: 0; }

/* 新对话弹窗 */
.new-chat-body { min-height: 200px; }
.new-chat-body .el-input { margin-bottom: 12px; }
.search-loading { text-align: center; padding: 40px 0; color: #86868b; font-size: 13px; }
.search-empty { padding: 20px 0; }
.search-results { max-height: 320px; overflow-y: auto; }
.search-user-item { display: flex; align-items: center; gap: 12px; padding: 10px 12px; border-radius: 8px; cursor: pointer; transition: background 0.2s; }
.search-user-item:hover { background: #f5f5f7; }
.search-user-info { display: flex; flex-direction: column; }
.search-user-name { font-size: 14px; font-weight: 500; color: #1d1d1f; }
.search-user-role { font-size: 12px; color: #86868b; }
</style>
