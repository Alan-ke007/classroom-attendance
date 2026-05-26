<template>
  <router-view v-slot="{ Component }">
    <transition name="fade-slide" mode="out-in">
      <component :is="Component || 'div'" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'

useTheme()
</script>

<style>
@import './styles/sci-fi.css';

/* ========================================
   Light Theme (default)
   ======================================== */
:root,
[data-theme="light"] {
  --c-bg: #F2F2F7;
  --c-bg-alt: #E8E8ED;
  --c-card: #FFFFFF;
  --c-card-hover: #FAFAFA;
  --c-sidebar: #F8F8FA;
  --c-header: rgba(255, 255, 255, 0.85);

  --c-text: #1D1D1F;
  --c-text-secondary: #86868B;
  --c-text-tertiary: #C7C7CC;
  --c-text-inverse: #FFFFFF;

  --c-primary: #007AFF;
  --c-primary-dark: #0055CC;
  --c-primary-light: #66B2FF;
  --c-primary-bg: rgba(0, 122, 255, 0.08);

  --c-success: #34C759;
  --c-success-bg: rgba(52, 199, 89, 0.1);
  --c-warning: #FF9500;
  --c-warning-bg: rgba(255, 149, 0, 0.1);
  --c-danger: #FF3B30;
  --c-danger-bg: rgba(255, 59, 48, 0.1);

  --c-glass-bg: rgba(255, 255, 255, 0.60);
  --c-glass-bg-strong: rgba(255, 255, 255, 0.80);
  --c-glass-border: rgba(255, 255, 255, 0.35);
  --c-glass-border-strong: rgba(255, 255, 255, 0.50);
  --c-glass-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);

  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 8px 30px rgba(0, 0, 0, 0.10);
  --shadow-glow: 0 0 20px rgba(0, 122, 255, 0.15);
  --c-shadow: var(--shadow-md);

  --c-green: #52c41a;
  --c-orange: #fa8c16;
  --c-purple: #b37feb;

  --c-border: #E5E5EA;
  --c-border-light: #F2F2F7;

  --c-scan-line: rgba(0, 122, 255, 0.025);
  --c-neon-glow: rgba(0, 122, 255, 0.25);
  --c-neon-glow-hover: rgba(0, 122, 255, 0.45);

  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 20px;
  --transition: 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);

  /* Element Plus overrides */
  --el-color-primary: var(--c-primary);
  --el-color-primary-dark-2: var(--c-primary-dark);
  --el-color-primary-light-3: #66B2FF;
  --el-color-primary-light-5: #99CCFF;
  --el-color-primary-light-7: #C2DFFF;
  --el-color-primary-light-9: #EBF5FF;
  --el-bg-color: var(--c-bg);
  --el-bg-color-overlay: var(--c-card);
  --el-border-color: var(--c-border);
  --el-border-color-light: var(--c-border-light);
  --el-border-radius-base: var(--radius-md);
  --el-text-color-primary: var(--c-text);
  --el-text-color-secondary: var(--c-text-secondary);
  --el-text-color-placeholder: var(--c-text-tertiary);
  --el-fill-color: var(--c-bg-alt);
  --el-fill-color-light: var(--c-card-hover);
  --el-fill-color-blank: var(--c-card);
  --el-box-shadow-light: var(--shadow-md);
  --el-box-shadow: var(--shadow-lg);
  --el-font-size-base: 14px;
}

/* ========================================
   Dark Theme
   ======================================== */
[data-theme="dark"] {
  --c-bg: #0D0D0F;
  --c-bg-alt: #1C1C1E;
  --c-card: #1C1C1E;
  --c-card-hover: #2C2C2E;
  --c-sidebar: #161618;
  --c-header: rgba(22, 22, 24, 0.85);

  --c-text: #F5F5F7;
  --c-text-secondary: #98989D;
  --c-text-tertiary: #636366;
  --c-text-inverse: #000000;

  --c-primary: #0A84FF;
  --c-primary-dark: #007AFF;
  --c-primary-light: #66B2FF;
  --c-primary-bg: rgba(10, 132, 255, 0.12);

  --c-success: #30D158;
  --c-success-bg: rgba(48, 209, 88, 0.12);
  --c-warning: #FF9F0A;
  --c-warning-bg: rgba(255, 159, 10, 0.12);
  --c-danger: #FF453A;
  --c-danger-bg: rgba(255, 69, 58, 0.12);

  --c-glass-bg: rgba(255, 255, 255, 0.08);
  --c-glass-bg-strong: rgba(255, 255, 255, 0.14);
  --c-glass-border: rgba(255, 255, 255, 0.12);
  --c-glass-border-strong: rgba(255, 255, 255, 0.18);
  --c-glass-shadow: 0 8px 32px rgba(0, 0, 0, 0.30);

  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.20);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.25);
  --shadow-lg: 0 8px 30px rgba(0, 0, 0, 0.35);
  --shadow-glow: 0 0 24px rgba(10, 132, 255, 0.20);
  --c-shadow: var(--shadow-md);

  --c-border: #38383A;
  --c-border-light: #2C2C2E;

  --c-green: #73d13d;
  --c-orange: #ffa940;
  --c-purple: #d3adf7;

  --c-scan-line: rgba(10, 132, 255, 0.04);
  --c-neon-glow: rgba(10, 132, 255, 0.35);
  --c-neon-glow-hover: rgba(10, 132, 255, 0.55);

  --el-color-primary: var(--c-primary);
  --el-color-primary-dark-2: var(--c-primary-dark);
  --el-color-primary-light-3: #4DA6FF;
  --el-color-primary-light-5: #3388DD;
  --el-color-primary-light-7: #1A6BB8;
  --el-color-primary-light-9: #0D4D8C;
  --el-bg-color: var(--c-bg);
  --el-bg-color-overlay: var(--c-card);
  --el-border-color: var(--c-border);
  --el-border-color-light: var(--c-border-light);
  --el-text-color-primary: var(--c-text);
  --el-text-color-secondary: var(--c-text-secondary);
  --el-text-color-placeholder: var(--c-text-tertiary);
  --el-fill-color: var(--c-bg-alt);
  --el-fill-color-light: var(--c-card-hover);
  --el-fill-color-blank: var(--c-card);
  --el-box-shadow-light: var(--shadow-md);
  --el-box-shadow: var(--shadow-lg);
}

/* ========================================
   Global Styles
   ======================================== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text',
               'PingFang SC', 'Helvetica Neue', sans-serif;
  font-size: 15px;
  line-height: 1.6;
  letter-spacing: 0.02em;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: var(--c-bg);
  color: var(--c-text);
  transition: background-color 0.3s ease, color 0.3s ease;
}

#app {
  min-height: 100vh;
  background:
    radial-gradient(ellipse at 15% 20%, rgba(0, 122, 255, 0.08) 0%, transparent 55%),
    radial-gradient(ellipse at 85% 75%, rgba(179, 127, 235, 0.06) 0%, transparent 55%),
    radial-gradient(ellipse at 50% 100%, rgba(0, 122, 255, 0.04) 0%, transparent 50%),
    var(--c-bg);
  transition: background-color 0.3s ease;
}

[data-theme="dark"] #app {
  background:
    radial-gradient(ellipse at 15% 20%, rgba(10, 132, 255, 0.10) 0%, transparent 55%),
    radial-gradient(ellipse at 85% 75%, rgba(211, 173, 247, 0.06) 0%, transparent 55%),
    radial-gradient(ellipse at 50% 100%, rgba(10, 132, 255, 0.05) 0%, transparent 50%),
    var(--c-bg);
}

/* Transitions for themed elements */
.el-card,
.el-dialog,
.el-message-box,
.el-dropdown-menu,
.el-aside,
.el-header,
.el-menu,
.el-table,
.el-form-item__label,
.el-input__wrapper {
  transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
}

/* ========================================
   Typography
   ======================================== */
h1, h2, h3, h4 {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display',
               'PingFang SC', 'Helvetica Neue', sans-serif;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: var(--c-text);
}

h1 { font-size: 28px; line-height: 1.3; }
h2 { font-size: 22px; line-height: 1.35; }
h3 { font-size: 18px; line-height: 1.4; }
h4 { font-size: 16px; line-height: 1.45; }

/* ========================================
   Route Transition
   ======================================== */
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

/* ========================================
   Global Utility Classes
   ======================================== */
.glass-card {
  background: var(--c-glass-bg);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--c-glass-shadow);
}

.glass-card-solid {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.text-gradient {
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-light));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* ========================================
   Glass Morphism — 全局毛玻璃样式
   ======================================== */

/* 所有 Element Plus 卡片使用毛玻璃 */
.el-card {
  background: var(--c-glass-bg) !important;
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border: 1px solid var(--c-glass-border) !important;
  border-radius: var(--radius-lg);
  box-shadow: var(--c-glass-shadow) !important;
  transition: all 0.35s cubic-bezier(0.25, 0.1, 0.25, 1) !important;
}

.el-card:hover {
  border-color: var(--c-primary) !important;
  box-shadow: var(--c-glass-shadow), 0 0 0 1px var(--c-primary) inset !important;
}

.el-card__header {
  border-bottom: 1px solid var(--c-glass-border) !important;
  padding: 16px 20px !important;
}

.el-card__body {
  padding: 20px !important;
}

/* 对话框毛玻璃 */
.el-dialog {
  background: var(--c-glass-bg-strong) !important;
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid var(--c-glass-border-strong) !important;
  border-radius: var(--radius-xl) !important;
  box-shadow: var(--c-glass-shadow) !important;
}

.el-dialog__header {
  border-bottom: 1px solid var(--c-glass-border);
  padding: 16px 24px !important;
  margin-right: 0 !important;
}

.el-dialog__body {
  padding: 24px !important;
}

.el-dialog__footer {
  border-top: 1px solid var(--c-glass-border);
  padding: 12px 24px !important;
}

.el-message-box {
  background: var(--c-glass-bg-strong) !important;
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid var(--c-glass-border-strong) !important;
  border-radius: var(--radius-xl) !important;
  box-shadow: var(--c-glass-shadow) !important;
}

.el-dropdown-menu {
  background: var(--c-glass-bg-strong) !important;
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid var(--c-glass-border) !important;
}

.el-pagination {
  --el-pagination-bg-color: transparent;
}

.el-table {
  background: transparent !important;
}
.el-table__header-wrapper,
.el-table__body-wrapper {
  background: transparent !important;
}
.el-table th.el-table__cell {
  background: var(--c-glass-bg) !important;
  backdrop-filter: blur(8px) saturate(180%);
}
.el-table tr {
  background: transparent !important;
}
.el-table td.el-table__cell {
  background: var(--c-glass-bg) !important;
  border-bottom: 1px solid var(--c-glass-border) !important;
}
.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background: var(--c-glass-bg-strong) !important;
}

.el-steps { background: transparent; }
.el-step__head { background: transparent; }
</style>
