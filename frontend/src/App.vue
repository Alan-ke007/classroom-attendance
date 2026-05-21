<template>
  <router-view v-slot="{ Component }">
    <transition name="fade-slide" mode="out-in">
      <component :is="Component || 'div'" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
</script>

<style>
@import './styles/sci-fi.css';

:root {
  --c-primary: #007AFF;
  --c-primary-dark: #0055CC;
  --c-bg: #F2F2F7;
  --c-card: #FFFFFF;
  --c-text: #1D1D1F;
  --c-text-secondary: #86868B;
  --c-border: #E5E5EA;
  --c-success: #34C759;
  --c-warning: #FF9500;
  --c-danger: #FF3B30;
  --radius: 12px;
  --shadow: 0 2px 12px rgba(0,0,0,0.06);
  --shadow-hover: 0 4px 20px rgba(0,0,0,0.1);
  --transition: 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);

  /* Element Plus 全局主题覆盖 */
  --el-color-primary: #007AFF;
  --el-color-primary-light-3: #66B2FF;
  --el-color-primary-light-5: #99CCFF;
  --el-color-primary-light-7: #C2DFFF;
  --el-color-primary-light-9: #EBF5FF;
  --el-color-primary-dark-2: #0055CC;
  --el-bg-color: #F2F2F7;
  --el-bg-color-overlay: #FFFFFF;
  --el-border-radius-base: 10px;
  --el-box-shadow-light: 0 2px 12px rgba(0,0,0,0.06);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'PingFang SC', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: var(--c-bg);
  color: var(--c-text);
}

.fade-slide-enter-active {
  transition: all 0.35s cubic-bezier(0.25, 0.1, 0.25, 1);
}
.fade-slide-leave-active {
  transition: all 0.25s cubic-bezier(0.25, 0.1, 0.25, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
