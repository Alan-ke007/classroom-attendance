<template>
  <div class="notification-center">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息通知</span>
          <el-button text type="primary" @click="markAllRead">全部已读</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="loadList">
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
      </div>

      <el-pagination
        v-if="total > 0" v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10, 20]" layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end" @change="loadList"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('')
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const iconMap = { attendance: '📋', behavior: '⚠️', leave: '📝', system: '🔔' }

async function loadList() {
  try {
    const res = await request.get('/notification/list', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    })
    const all = res.data?.records || []
    list.value = activeTab.value ? all.filter(n => n.type === activeTab.value) : all
    total.value = all.length
  } catch (e) { console.error('加载通知失败', e) }
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

onMounted(loadList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.empty { text-align: center; padding: 60px 0; color: #909399; }
.notif-item {
  display: flex; padding: 14px; border-bottom: 1px solid #f0f0f0;
  cursor: pointer; transition: background 0.2s;
}
.notif-item:hover { background: #f5f7fa; }
.notif-item.unread { background: #ecf5ff; }
.notif-icon { font-size: 28px; margin-right: 14px; width: 40px; text-align: center; }
.notif-body { flex: 1; }
.notif-title { font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.notif-title .el-tag { margin-left: 8px; }
.notif-content { font-size: 13px; color: #606266; margin-bottom: 4px; }
.notif-time { font-size: 12px; color: #909399; }
</style>
