<template>
  <div class="dashboard">
    <!-- 移动端遮罩层 -->
    <div v-if="sidebarOpen" class="sidebar-overlay" @click="closeSidebar" />

    <el-container>
      <!-- 侧边栏 -->
      <el-aside :class="{ 'sidebar-open': sidebarOpen }" width="200px">
        <div class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 32 32" fill="none"><rect width="32" height="32" rx="8" fill="url(#lg)"/><circle cx="16" cy="14" r="6" stroke="#fff" stroke-width="2" fill="none"/><circle cx="16" cy="14" r="2" fill="#fff"/><defs><linearGradient id="lg" x1="0" y1="0" x2="32" y2="32"><stop offset="0%" stop-color="#007AFF"/><stop offset="100%" stop-color="#0055CC"/></linearGradient></defs></svg>
          </div>
          <div class="logo-text">
            <h3>智课考勤</h3>
            <span>管理端</span>
          </div>
        </div>
        <el-menu
          :default-active="activeMenu"
          :default-openeds="openedMenus"
          router
          @select="onMenuSelect"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-sub-menu index="management">
            <template #title>
              <el-icon><Management /></el-icon>
              <span>信息管理</span>
            </template>
            <el-menu-item index="/dashboard/class">班级管理</el-menu-item>
            <el-menu-item index="/dashboard/student">学生管理</el-menu-item>
            <el-menu-item index="/dashboard/course">课程管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="attendance">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>考勤管理</span>
            </template>
            <el-menu-item index="/dashboard/attendance/list">考勤记录</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="behavior">
            <template #title>
              <el-icon><VideoCamera /></el-icon>
              <span>行为监控</span>
            </template>
            <el-menu-item index="/dashboard/behavior/list">行为记录</el-menu-item>
            <el-menu-item index="/dashboard/behavior/monitor">实时监控</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/dashboard/leave">
            <el-icon><Notebook /></el-icon>
            <span>请假管理</span>
          </el-menu-item>

          <el-menu-item index="/dashboard/qrcheckin">
            <el-icon><Iphone /></el-icon>
            <span>二维码签到</span>
          </el-menu-item>

          <el-menu-item index="/dashboard/statistics">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </el-menu-item>

          <el-menu-item index="/dashboard/import">
            <el-icon><Upload /></el-icon>
            <span>批量导入</span>
          </el-menu-item>

          <el-menu-item index="/dashboard/file">
            <el-icon><Folder /></el-icon>
            <span>文件管理</span>
          </el-menu-item>

          <el-menu-item index="/dashboard/notification">
            <el-icon><Bell /></el-icon>
            <span>消息通知</span>
          </el-menu-item>

          <el-menu-item index="/dashboard/chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>聊天消息</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="chat-badge" />
          </el-menu-item>

          <el-menu-item v-if="userInfo?.role === 'admin'" index="/dashboard/user">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>

          <el-menu-item v-if="userInfo?.role === 'admin'" index="/dashboard/log">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部导航栏 -->
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
                  <el-breadcrumb-item :to="{ path: '/dashboard' }">智课考勤</el-breadcrumb-item>
                  <el-breadcrumb-item>{{ currentPage }}</el-breadcrumb-item>
                </el-breadcrumb>
              </div>
            </div>
            <div class="user-info">
              <ThemeToggle />
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

        <!-- 主内容区 -->
        <el-main style="position: relative;">
          <ParticleBackground mode="dataflow" :color="isDark ? '#0A84FF' : '#007AFF'" position-type="absolute" />
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  HomeFilled,
  Management,
  Calendar,
  VideoCamera,
  DataAnalysis,
  Folder,
  ArrowDown,
  Notebook,
  Iphone,
  Upload,
  Bell,
  ChatDotRound,
} from '@element-plus/icons-vue'
import { getUnreadCount } from '@/api/chat'
import { useTheme } from '@/composables/useTheme'
import ParticleBackground from '@/components/sci-fi/ParticleBackground.vue'
import ProfileDialog from '@/components/ProfileDialog.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router = useRouter()
const route = useRoute()
const { isDark } = useTheme()
const userInfo = ref(null)
const unreadCount = ref(0)
const showProfile = ref(false)
const sidebarOpen = ref(false)
const avatarUrl = ref('')
let unreadTimer = null

const activeMenu = computed(() => route.path)

const openedMenus = computed(() => {
  const path = route.path
  if (path.startsWith('/dashboard/attendance')) return ['attendance']
  if (path.startsWith('/dashboard/behavior')) return ['behavior']
  if (path.startsWith('/dashboard/class') || path.startsWith('/dashboard/student') || path.startsWith('/dashboard/course')) return ['management']
  return []
})

const currentPage = computed(() => {
  const pathMap = {
    '/dashboard': '首页',
    '/dashboard/class': '班级管理',
    '/dashboard/student': '学生管理',
    '/dashboard/course': '课程管理',
    '/dashboard/attendance/list': '考勤记录',
    '/dashboard/behavior/list': '行为记录',
    '/dashboard/behavior/monitor': '实时监控',
    '/dashboard/leave': '请假管理',
    '/dashboard/qrcheckin': '二维码签到',
    '/dashboard/statistics': '数据统计',
    '/dashboard/import': '批量导入',
    '/dashboard/file': '文件管理',
    '/dashboard/notification': '消息通知',
    '/dashboard/chat': '聊天消息',
    '/dashboard/algorithm/test': '算法测试'
  }
  return pathMap[route.path] || '页面'
})

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function closeSidebar() {
  sidebarOpen.value = false
}

function onMenuSelect() {
  // 移动端点击菜单后自动关闭侧边栏
  if (window.innerWidth < 768) {
    sidebarOpen.value = false
  }
}

onMounted(() => {
  const info = localStorage.getItem('userInfo')
  if (info) {
    userInfo.value = JSON.parse(info)
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
.dashboard {
  min-height: 100vh;
  background: transparent;
}

/* ========== 侧边栏 ========== */
.el-aside {
  background: var(--c-glass-bg);
  backdrop-filter: blur(28px) saturate(180%);
  -webkit-backdrop-filter: blur(28px) saturate(180%);
  border-right: 1px solid var(--c-glass-border);
  color: var(--c-text);
  overflow-x: hidden;
  box-shadow: var(--c-glass-shadow);
  transition: transform 0.3s cubic-bezier(0.25, 0.1, 0.25, 1),
              background-color 0.3s ease;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 18px;
  background: var(--c-glass-bg);
  border-bottom: 1px solid var(--c-glass-border);
}
.logo-icon svg { display: block; width: 32px; height: 32px; }
.logo-text h3 {
  margin: 0; color: var(--c-text); font-size: 15px; font-weight: 600; line-height: 1.2;
}
.logo-text span {
  font-size: 11px; color: var(--c-text-tertiary); font-weight: 400;
}
.el-menu {
  border-right: none;
  background: transparent;
}
:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  transition: all 0.25s ease;
  margin: 2px 8px;
  border-radius: 8px;
  color: var(--c-text-secondary) !important;
}
:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: var(--c-primary-bg) !important;
  color: var(--c-primary) !important;
}
:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, var(--c-primary-bg), transparent) !important;
  color: var(--c-primary) !important;
  font-weight: 600;
}
:deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}
:deep(.el-sub-menu .el-menu .el-menu-item) {
  padding-left: 50px !important;
}

/* ========== 遮罩层 ========== */
.sidebar-overlay {
  display: none;
}

/* ========== 顶部栏 ========== */
.el-header {
  background: var(--c-glass-bg);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  box-shadow: 0 1px 0 var(--c-glass-border);
  padding: 0 16px;
  border-bottom: 1px solid var(--c-glass-border);
  animation: slideDown 0.5s ease-out;
  transition: background-color 0.3s ease;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-20px); }
  to   { opacity: 1; transform: translateY(0); }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
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
  background: var(--c-text-secondary);
  border-radius: 2px;
  transition: all 0.3s ease;
  transform-origin: center;
}

.breadcrumb {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 220px;
}

:deep(.el-breadcrumb__inner) {
  font-weight: 400;
  color: var(--c-text-secondary) !important;
  transition: all 0.3s ease;
}
:deep(.el-breadcrumb__inner:hover) {
  color: var(--c-primary) !important;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--c-text-secondary);
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 4px;
  transition: all var(--transition);
}
.user-name:hover {
  background: var(--c-primary-bg);
  color: var(--c-primary);
  transform: translateY(-1px);
}
.user-name-text {
  display: inline;
}

/* ========== 主内容区 ========== */
.el-main {
  padding: 20px;
  background: transparent;
  animation: fadeIn 0.5s ease-out 0.2s both;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}

/* ========== 下拉菜单 ========== */
:deep(.el-dropdown-menu) {
  border-radius: var(--radius-md);
  background: var(--c-card);
  border: 1px solid var(--c-border);
  padding: 6px;
  box-shadow: var(--shadow-lg);
  animation: scaleIn 0.3s ease-out;
}
@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95); }
  to   { opacity: 1; transform: scale(1); }
}
:deep(.el-dropdown-menu__item) {
  border-radius: 4px;
  transition: all 0.3s ease;
  padding: 8px 12px;
  color: var(--c-text-secondary);
}
:deep(.el-dropdown-menu__item:hover) {
  background: var(--c-primary-bg);
  color: var(--c-primary);
  transform: translateX(4px);
}

.chat-badge { margin-left: auto; }

/* ================================================================
   响应式设计 — 移动端
   ================================================================ */
@media (max-width: 768px) {
  /* 汉堡菜单可见 */
  .hamburger {
    display: flex;
  }

  /* 侧边栏默认隐藏，滑入时显示 */
  .el-aside {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 1050;
    transform: translateX(-100%);
    width: 200px !important;
  }
  .el-aside.sidebar-open {
    transform: translateX(0);
  }

  /* 遮罩层 */
  .sidebar-overlay {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 1040;
    background: rgba(0, 0, 0, 0.55);
    animation: fadeIn 0.25s ease;
  }

  /* 菜单文字始终显示 */
  :deep(.el-menu-item span),
  :deep(.el-sub-menu__title span) {
    display: inline !important;
  }

  /* 顶部栏缩减 */
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

  /* 主区域减 padding */
  .el-main {
    padding: 12px;
  }
}
</style>
