<template>
  <div class="student-layout">
    <!-- 移动端遮罩层 -->
    <div v-if="sidebarOpen" class="sidebar-overlay" @click="closeSidebar" />

    <div :class="{ 'sidebar-open': sidebarOpen }" class="student-sidebar">
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 32 32" fill="none"><rect width="32" height="32" rx="8" fill="url(#lg)"/><circle cx="16" cy="14" r="6" stroke="#fff" stroke-width="2" fill="none"/><circle cx="16" cy="14" r="2" fill="#fff"/><defs><linearGradient id="lg" x1="0" y1="0" x2="32" y2="32"><stop offset="0%" stop-color="#007AFF"/><stop offset="100%" stop-color="#0055CC"/></linearGradient></defs></svg>
        </div>
        <div class="logo-text">
          <h3>智课考勤</h3>
          <p class="logo-sub">学生端</p>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#2c2c2e"
        text-color="#bfcbd9"
        active-text-color="#007AFF"
        @select="onMenuSelect"
      >
        <el-menu-item index="/student/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/student/schedule">
          <el-icon><Calendar /></el-icon>
          <span>课程表</span>
        </el-menu-item>
        <el-menu-item index="/student/attendance">
          <el-icon><List /></el-icon>
          <span>考勤记录</span>
        </el-menu-item>
        <el-menu-item index="/student/behavior">
          <el-icon><WarningFilled /></el-icon>
          <span>行为记录</span>
        </el-menu-item>
        <el-menu-item index="/student/leave">
          <el-icon><Notebook /></el-icon>
          <span>请假申请</span>
        </el-menu-item>
        <el-menu-item index="/student/qrscan">
          <el-icon><Iphone /></el-icon>
          <span>扫码签到</span>
        </el-menu-item>
        <el-menu-item index="/student/weekly-report">
          <el-icon><DataAnalysis /></el-icon>
          <span>课堂周报</span>
        </el-menu-item>
        <el-menu-item index="/student/notification">
          <el-icon><Bell /></el-icon>
          <span>消息通知</span>
        </el-menu-item>
        <el-menu-item index="/student/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>聊天消息</span>
          <el-badge v-if="unreadCount > 0" :value="unreadCount" class="chat-badge" />
        </el-menu-item>
      </el-menu>
    </div>

    <el-container>
      <el-header>
        <div class="header-content">
          <div class="header-left">
            <button class="hamburger" @click="toggleSidebar" :aria-label="sidebarOpen ? '关闭菜单' : '打开菜单'">
              <span />
              <span />
              <span />
            </button>
            <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/student/home' }">智课考勤</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPage }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          </div>
          <div class="user-info">
            <el-tag v-if="role === 'student'" type="success" size="small" style="margin-right: 12px">学生</el-tag>
            <el-dropdown trigger="click">
              <span class="user-name">
                <el-avatar :size="28" :src="avatarUrl" />
                <span class="user-name-text">{{ userInfo?.realName || userInfo?.username }}</span>
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="showProfile = true">个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- 个人中心弹窗 -->
      <ProfileDialog v-model="showProfile" :user="userInfo" @profile-updated="onProfileUpdated" @close="showProfile = false" />

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  HomeFilled,
  Calendar,
  List,
  WarningFilled,
  Notebook,
  Iphone,
  Bell,
  ArrowDown,
  ChatDotRound,
  DataAnalysis
} from '@element-plus/icons-vue'
import { getUnreadCount } from '@/api/chat'
import ProfileDialog from '@/components/ProfileDialog.vue'

const router = useRouter()
const route = useRoute()
const userInfo = ref(null)
const role = ref('student')
const unreadCount = ref(0)
const showProfile = ref(false)
const sidebarOpen = ref(false)
const avatarUrl = ref('')
let unreadTimer = null

const activeMenu = computed(() => route.path)

const currentPage = computed(() => {
  const map = {
    '/student/home': '首页',
    '/student/schedule': '课程表',
    '/student/attendance': '考勤记录',
    '/student/behavior': '行为记录',
    '/student/leave': '请假申请',
    '/student/qrscan': '扫码签到',
    '/student/notification': '消息通知',
    '/student/chat': '聊天消息',
    '/student/weekly-report': '课堂周报'
  }
  return map[route.path] || ''
})

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function closeSidebar() {
  sidebarOpen.value = false
}

function onMenuSelect() {
  if (window.innerWidth < 768) {
    sidebarOpen.value = false
  }
}

onMounted(() => {
  const info = localStorage.getItem('userInfo')
  if (info) {
    userInfo.value = JSON.parse(info)
    role.value = userInfo.value?.role || 'student'
    avatarUrl.value = userInfo.value.avatar || ''
  }
  fetchUnread()
  unreadTimer = setInterval(fetchUnread, 30000)
})

onUnmounted(() => {
  if (unreadTimer) clearInterval(unreadTimer)
})

async function fetchUnread() {
  try { const r = await getUnreadCount(); unreadCount.value = r.data || 0 } catch (e) { /* ignore */ }
}

function onProfileUpdated(info) {
  userInfo.value = info
  avatarUrl.value = info.avatar || ''
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.student-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.student-sidebar {
  width: 200px;
  background: linear-gradient(180deg, #1c1c1e 0%, #2c2c2e 100%);
  color: #fff;
  overflow-x: hidden;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.08);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 18px;
  background: rgba(0, 0, 0, 0.12);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.logo-icon svg { display: block; width: 32px; height: 32px; }
.logo-text h3 {
  margin: 0; color: #fff; font-size: 15px; font-weight: 600; line-height: 1.2;
}
.logo-sub {
  margin: 1px 0 0; font-size: 11px; color: rgba(255, 255, 255, 0.5);
}

.el-menu {
  border-right: none;
  background: transparent;
}
:deep(.el-menu-item) {
  transition: all 0.25s ease;
  margin: 2px 8px;
  border-radius: 8px;
}
:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
}
:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.25), rgba(64, 158, 255, 0.08)) !important;
  color: #409eff !important;
  font-weight: 600;
  border-radius: 8px;
}

.el-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  padding: 0 20px;
  border-bottom: 1px solid #ebeef5;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
  font-size: 14px;
}

.el-main {
  padding: 20px;
  background: transparent;
}

.chat-badge { margin-left: auto; }

/* ========== 遮罩层 ========== */
.sidebar-overlay {
  display: none;
}

/* ========== 汉堡菜单按钮 ========== */
.hamburger {
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  width: 28px;
  height: 28px;
  padding: 4px;
  background: none;
  border: none;
  cursor: pointer;
  z-index: 1100;
}
.hamburger span {
  display: block;
  width: 100%;
  height: 2px;
  background: #606266;
  border-radius: 2px;
  transition: all 0.3s ease;
  transform-origin: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 220px;
}

.user-name-text {
  display: inline;
}

/* ================================================================
   响应式设计 — 移动端
   ================================================================ */
@media (max-width: 768px) {
  .hamburger {
    display: flex;
  }

  .student-sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 1050;
    transform: translateX(-100%);
    transition: transform 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  }
  .student-sidebar.sidebar-open {
    transform: translateX(0);
  }

  .sidebar-overlay {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 1040;
    background: rgba(0, 0, 0, 0.45);
    animation: fadeIn 0.25s ease;
  }

  .el-header {
    padding: 0 12px;
  }
  .user-name-text {
    display: none;
  }
  .breadcrumb {
    max-width: 140px;
    font-size: 13px;
  }

  .el-main {
    padding: 12px;
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}
</style>
