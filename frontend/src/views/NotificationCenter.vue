<template>
  <div class="notification-center">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息通知</span>
          <el-button text type="primary" @click="markAllRead">全部已读</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="考勤" name="attendance" />
        <el-tab-pane label="行为" name="behavior" />
        <el-tab-pane label="请假" name="leave" />
        <el-tab-pane label="系统" name="system" />
      </el-tabs>

      <div v-if="list.length === 0" class="empty">暂无通知</div>

      <div v-for="item in list" :key="item.id" class="notif-item" :class="{ unread: item.isRead === 0 }"
        @click="markRead(item)">
        <div class="notif-icon" :class="item.type">
          {{ iconMap[item.type] || '📢' }}
        </div>
        <div class="notif-body">
          <div class="notif-title">
            {{ item.title }}
            <el-tag v-if="item.isRead === 0" size="small" type="danger">NEW</el-tag>
          </div>
          <div class="notif-content">{{ item.content }}</div>
          <div class="notif-time">{{ item.createTime }}</div>
        </div>
        <el-button text type="danger" size="small" @click.stop="handleDelete(item)">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>

      <el-pagination
        v-if="total > 0" v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10, 20]" layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end" @change="applyFilter"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const activeTab = ref('')
const list = ref([])
const allRecords = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const iconMap = { attendance: '📋', behavior: '⚠️', leave: '📝', system: '🔔' }

// 后端 /notification/list 仅支持 pageNum/pageSize，不支持按 type 过滤，
// 因此一次性拉取全量通知，在前端按 activeTab 过滤后再做客户端分页。
async function loadAll() {
  try {
    const res = await request.get('/notification/list', {
      params: { pageNum: 1, pageSize: 9999 }
    })
    allRecords.value = res.data?.records || []
    applyFilter()
  } catch (e) { console.error('加载通知失败', e) }
}

// 按当前 tab 过滤并截取对应分页
function applyFilter() {
  const filtered = activeTab.value
    ? allRecords.value.filter(n => n.type === activeTab.value)
    : allRecords.value
  total.value = filtered.length
  const start = (pageNum.value - 1) * pageSize.value
  list.value = filtered.slice(start, start + pageSize.value)
}

// 切换 tab 时重置回第一页，再重新过滤
function onTabChange() {
  pageNum.value = 1
  applyFilter()
}

async function markRead(item) {
  if (item.isRead === 1) return
  try {
    await request.put(`/notification/read/${item.id}`)
    item.isRead = 1
  } catch (e) { console.error(e) }
}

async function markAllRead() {
  try {
    await request.put('/notification/read-all')
    list.value.forEach(n => n.isRead = 1)
    ElMessage.success('已全部标记为已读')
  } catch (e) { console.error(e) }
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm('确定删除这条通知吗？', '提示', { type: 'warning' })
    await request.delete(`/notification/${item.id}`)
    allRecords.value = allRecords.value.filter(n => n.id !== item.id)
    applyFilter()
    ElMessage.success('已删除')
  } catch (e) { /* cancelled or error */ }
}

onMounted(loadAll)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header span { color: var(--c-text); font-weight: 600; }
.empty { text-align: center; padding: 60px 0; color: var(--c-text-tertiary); }
.notif-item {
  display: flex; padding: 14px; border-bottom: 1px solid var(--c-border-light);
  cursor: pointer; transition: background 0.2s;
}
.notif-item:hover { background: var(--c-fill-color, var(--c-bg-alt)); }
.notif-item.unread { background: var(--c-primary-bg); }
.notif-icon { font-size: 28px; margin-right: 14px; width: 40px; text-align: center; }
.notif-body { flex: 1; }
.notif-title { font-size: 15px; font-weight: 500; margin-bottom: 4px; color: var(--c-text); }
.notif-title .el-tag { margin-left: 8px; }
.notif-content { font-size: 13px; color: var(--c-text-secondary); margin-bottom: 4px; }
.notif-time { font-size: 12px; color: var(--c-text-tertiary); }
</style>
