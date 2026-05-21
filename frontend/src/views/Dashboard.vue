<template>
  <div class="dashboard">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px">
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
          background-color="#2c2c2e"
          text-color="#bfcbd9"
          active-text-color="#007AFF"
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
        </el-menu>
      </el-aside>
      
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header>
          <div class="header-content">
            <div class="breadcrumb">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/dashboard' }">智课考勤</el-breadcrumb-item>
                <el-breadcrumb-item>{{ currentPage }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="user-info">
              <el-dropdown trigger="click">
                <span class="user-name">
                  <el-avatar :size="28" :src="avatarUrl" />
                  {{ userInfo?.realName || userInfo?.username }}
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
          <ParticleBackground mode="dataflow" color="#007AFF" position-type="absolute" />
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
import ParticleBackground from '@/components/sci-fi/ParticleBackground.vue'
import ProfileDialog from '@/components/ProfileDialog.vue'

const router = useRouter()
const route = useRoute()
const userInfo = ref(null)
const unreadCount = ref(0)
const showProfile = ref(false)
const avatarUrl = ref('')
let unreadTimer = null

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 自动展开子菜单
const openedMenus = computed(() => {
  const path = route.path
  if (path.startsWith('/dashboard/attendance')) return ['attendance']
  if (path.startsWith('/dashboard/behavior')) return ['behavior']
  if (path.startsWith('/dashboard/class') || path.startsWith('/dashboard/student') || path.startsWith('/dashboard/course')) return ['management']
  return []
})

// 当前页面名称
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
  background: #f5f7fa;
}

.el-aside {
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
.logo-text span {
  font-size: 11px; color: rgba(255, 255, 255, 0.5); font-weight: 400;
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
}
:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
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
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  padding: 0 20px;
  border-bottom: 1px solid #ebeef5;
  animation: slideDown 0.5s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.breadcrumb {
  font-size: 14px;
}

:deep(.el-breadcrumb__inner) {
  font-weight: 400;
  color: #606266;
  transition: all 0.3s ease;
}

:deep(.el-breadcrumb__inner:hover) {
  color: #409eff;
  transform: translateX(2px);
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
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-name:hover {
  background: #ecf5ff;
  color: #409eff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.el-main {
  padding: 20px;
  background: transparent;
  animation: fadeIn 0.5s ease-out 0.2s both;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu) {
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
  padding: 6px;
  animation: scaleIn 0.3s ease-out;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

:deep(.el-dropdown-menu__item) {
  border-radius: 4px;
  transition: all 0.3s ease;
  padding: 8px 12px;
}

:deep(.el-dropdown-menu__item:hover) {
  background: #ecf5ff;
  color: #409eff;
  transform: translateX(4px);
}

/* 聊天徽章 */
.chat-badge { margin-left: auto; }

/* 响应式设计 */
@media (max-width: 768px) {
  .el-aside {
    width: 64px !important;
  }
  
  .logo h3 {
    font-size: 14px;
  }
  
  :deep(.el-menu-item span),
  :deep(.el-sub-menu__title span) {
    display: none;
  }
}
</style>
