<template>
  <div class="track-glass">
    <router-view v-slot="{ Component }">
      <transition name="fade-slide" mode="out-in">
        <component :is="Component || 'div'" />
      </transition>
    </router-view>
  </div>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
useTheme()
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&family=JetBrains+Mono:wght@400;500;600;700&display=swap');
@import './styles/sci-fi.css';
@import './styles/tokens.css';

/* ========================================
   BRUTALIST TERMINAL — DARK-FIRST
   Cyan #00E5FF + Magenta #E040FB + Yellow #FFD600
   （注：粗野风已收编至 .track-brutal 作用域，见文末 Element Plus 段）
   ======================================== */

/* ========================================
   DARK MODE (default)
   ======================================== */
:root {
  /* Base */
  --c-bg: #0D0D0D;
  --c-bg-alt: #131313;
  --c-sidebar: #0F0F0F;
  --c-card: #161616;
  --c-card-hover: #1C1C1C;
  --c-header: rgba(13, 13, 13, 0.94);

  /* Text */
  --c-text: #F0F0F0;
  --c-text-secondary: #999999;
  --c-text-tertiary: #666666;
  --c-text-inverse: #0D0D0D;

  /* Accent 1 — Electric Cyan */
  --c-accent: #00E5FF;
  --c-accent-dark: #00B8D4;
  --c-accent-light: #40EEFF;
  --c-accent-bg: rgba(0, 229, 255, 0.08);
  --c-accent-border: rgba(0, 229, 255, 0.30);

  /* Accent 2 — Magenta */
  --c-accent-2: #E040FB;
  --c-accent-2-dark: #C020E0;
  --c-accent-2-bg: rgba(224, 64, 251, 0.08);
  --c-accent-2-border: rgba(224, 64, 251, 0.30);

  /* Accent 3 — Yellow */
  --c-accent-3: #FFD600;
  --c-accent-3-dark: #E6C000;
  --c-accent-3-bg: rgba(255, 214, 0, 0.08);
  --c-accent-3-border: rgba(255, 214, 0, 0.30);

  /* Semantic */
  --c-success: #00E676;
  --c-success-bg: rgba(0, 230, 118, 0.08);
  --c-danger: #FF1744;
  --c-danger-bg: rgba(255, 23, 68, 0.08);
  --c-warn: var(--c-accent-3);
  --c-warn-bg: var(--c-accent-3-bg);

  /* Neubrutalist tokens */
  --bw: 3px;                           /* thicker borders */
  --bw-bold: 4px;
  --shadow-hard: 4px 4px 0 0 #000;
  --shadow-hover: 6px 6px 0 0 #000;
  --radius: 0px;
  --radius-sm: 2px;
  --radius-card: 6px;

  /* Standard shadows */
  --shadow-sm: 3px 3px 0 0 rgba(0,0,0,0.8);
  --shadow-md: 5px 5px 0 0 rgba(0,0,0,0.8);
  --shadow-lg: 8px 8px 0 0 rgba(0,0,0,0.8);
  --shadow-glow-cyan: 0 0 24px rgba(0, 229, 255, 0.25);
  --shadow-glow-magenta: 0 0 24px rgba(224, 64, 251, 0.25);
  --shadow-glow: var(--shadow-glow-cyan);
  --c-shadow: var(--shadow-sm);

  /* Borders */
  --c-border: #2A2A2A;
  --c-border-light: #1F1F1F;

  /* Glass (only for overlays) */
  --c-glass-bg: rgba(22, 22, 22, 0.92);
  --c-glass-bg-strong: rgba(22, 22, 22, 0.97);
  --c-glass-border: rgba(255, 255, 255, 0.06);
  --c-glass-border-strong: rgba(255, 255, 255, 0.10);
  --c-glass-shadow: var(--shadow-md);

  /* Misc */
  --c-green: #00E676;
  --c-orange: #FFD600;
  --c-purple: var(--c-accent-2);
  --c-scan-line: rgba(0, 229, 255, 0.04);
  --c-neon-glow: rgba(0, 229, 255, 0.30);
  --c-neon-glow-hover: rgba(0, 229, 255, 0.55);

  /* Radii & Motion */
  --radius-md: 4px;
  --radius-lg: 8px;
  --radius-xl: 12px;
  --transition: 0.12s cubic-bezier(0.4, 0, 0.2, 1);

  /* Typography */
  --font-mono: 'JetBrains Mono', 'Cascadia Code', 'Fira Code', monospace;

  /* Backward compat aliases */
  --c-primary: var(--c-accent);
  --c-primary-dark: var(--c-accent-dark);
  --c-primary-light: var(--c-accent-light);
  --c-primary-bg: var(--c-accent-bg);
  --c-warning: var(--c-warn);
  --c-warning-bg: var(--c-warn-bg);

  /* Element Plus overrides (theme variables only — component shapes handled per-track) */
  --el-color-primary: var(--c-accent);
  --el-color-primary-dark-2: var(--c-accent-dark);
  --el-color-primary-light-3: #40EEFF;
  --el-color-primary-light-5: #80F4FF;
  --el-color-primary-light-7: #B3F8FF;
  --el-color-primary-light-9: #E0FCFF;
  --el-color-success: var(--c-success);
  --el-color-warning: var(--c-warn);
  --el-color-danger: var(--c-danger);
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
  --el-box-shadow-light: var(--shadow-sm);
  --el-box-shadow: var(--shadow-md);
  --el-font-size-base: 14px;
}

/* ========================================
   LIGHT MODE
   ======================================== */
[data-theme="light"] {
  --c-bg: #F2F2F2;
  --c-bg-alt: #E6E6E6;
  --c-sidebar: #EAEAEA;
  --c-card: #FFFFFF;
  --c-card-hover: #F7F7F7;
  --c-header: rgba(255, 255, 255, 0.94);

  --c-text: #111111;
  --c-text-secondary: #666666;
  --c-text-tertiary: #999999;
  --c-text-inverse: #FFFFFF;

  --c-accent: #0097A7;
  --c-accent-dark: #006974;
  --c-accent-light: #00BCD4;
  --c-accent-bg: rgba(0, 151, 167, 0.08);
  --c-accent-border: rgba(0, 151, 167, 0.30);

  --c-accent-2: #9C27B0;
  --c-accent-2-dark: #7B1FA2;
  --c-accent-2-bg: rgba(156, 39, 176, 0.08);
  --c-accent-2-border: rgba(156, 39, 176, 0.30);

  --c-accent-3: #F9A825;
  --c-accent-3-dark: #F57F17;
  --c-accent-3-bg: rgba(249, 168, 37, 0.08);
  --c-accent-3-border: rgba(249, 168, 37, 0.30);

  --c-success: #00C853;
  --c-success-bg: rgba(0, 200, 83, 0.08);
  --c-danger: #D50000;
  --c-danger-bg: rgba(213, 0, 0, 0.08);
  --c-warn: var(--c-accent-3);
  --c-warn-bg: var(--c-accent-3-bg);

  --shadow-hard: 4px 4px 0 0 #BBB;
  --shadow-hover: 6px 6px 0 0 #AAA;
  --shadow-sm: 3px 3px 0 0 rgba(0,0,0,0.12);
  --shadow-md: 5px 5px 0 0 rgba(0,0,0,0.12);
  --shadow-lg: 8px 8px 0 0 rgba(0,0,0,0.12);
  --shadow-glow-cyan: 0 0 20px rgba(0, 151, 167, 0.18);
  --shadow-glow-magenta: 0 0 20px rgba(156, 39, 176, 0.18);
  --shadow-glow: var(--shadow-glow-cyan);

  --c-border: #D5D5D5;
  --c-border-light: #E8E8E8;

  --c-glass-bg: rgba(255, 255, 255, 0.90);
  --c-glass-bg-strong: rgba(255, 255, 255, 0.97);
  --c-glass-border: rgba(0, 0, 0, 0.08);
  --c-glass-border-strong: rgba(0, 0, 0, 0.14);
  --c-glass-shadow: var(--shadow-sm);

  --c-scan-line: rgba(0, 151, 167, 0.03);
  --c-neon-glow: rgba(0, 151, 167, 0.22);
  --c-neon-glow-hover: rgba(0, 151, 167, 0.40);

  /* Backward compat */
  --c-primary: var(--c-accent);
  --c-primary-dark: var(--c-accent-dark);
  --c-primary-light: var(--c-accent-light);
  --c-primary-bg: var(--c-accent-bg);
  --c-warning: var(--c-warn);
  --c-warning-bg: var(--c-warn-bg);

  /* Element Plus light overrides */
  --el-color-primary: var(--c-accent);
  --el-color-primary-dark-2: var(--c-accent-dark);
  --el-color-primary-light-3: #40C4D8;
  --el-color-primary-light-5: #80DEEA;
  --el-color-primary-light-7: #B3E5F0;
  --el-color-primary-light-9: #E0F4F8;
  --el-color-success: var(--c-success);
  --el-color-warning: var(--c-warn);
  --el-color-danger: var(--c-danger);
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
  --el-box-shadow-light: var(--shadow-sm);
  --el-box-shadow: var(--shadow-md);
}

/* ========================================
   GLOBAL STYLES
   ======================================== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont,
               'PingFang SC', 'Helvetica Neue', sans-serif;
  font-size: 14px;
  line-height: 1.6;
  letter-spacing: -0.01em;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: var(--c-bg);
  color: var(--c-text);
}

#app {
  min-height: 100vh;
  background: var(--c-bg);
}

/* ========================================
   TYPOGRAPHY
   ======================================== */
h1, h2, h3, h4 {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont,
               'PingFang SC', 'Helvetica Neue', sans-serif;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--c-text);
}

h1 { font-size: 30px; line-height: 1.15; }
h2 { font-size: 22px; line-height: 1.2; }
h3 { font-size: 16px; line-height: 1.3; }
h4 { font-size: 14px; line-height: 1.4; }

/* Mono for data */
.mono, .data-label, .stat-value {
  font-family: var(--font-mono);
}

/* ========================================
   ROUTE TRANSITIONS
   ======================================== */
.fade-slide-enter-active { transition: all 0.12s ease; }
.fade-slide-leave-active { transition: all 0.08s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(6px); }
.fade-slide-leave-to { opacity: 0; }

/* ========================================
   UTILITY CLASSES（粗野组件，供 Track B 使用）
   ======================================== */
.brutal-card {
  background: var(--c-card);
  border: var(--bw) solid var(--c-border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-hard);
  transition: box-shadow 0.12s ease, transform 0.12s ease;
}

.brutal-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translate(-2px, -2px);
}

.brutal-btn {
  background: var(--c-accent);
  color: var(--c-text-inverse);
  border: var(--bw) solid #000;
  border-radius: var(--radius);
  font-weight: 800;
  font-size: 14px;
  padding: 10px 20px;
  cursor: pointer;
  box-shadow: var(--shadow-hard);
  transition: all 0.12s ease;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.brutal-btn:hover {
  box-shadow: var(--shadow-hover), var(--shadow-glow-cyan);
  transform: translate(-2px, -2px);
}

.brutal-btn:active {
  box-shadow: none;
  transform: translate(2px, 2px);
}

.brutal-btn-magenta {
  background: var(--c-accent-2);
}

.brutal-btn-magenta:hover {
  box-shadow: var(--shadow-hover), var(--shadow-glow-magenta);
}

.brutal-btn-yellow {
  background: var(--c-accent-3);
}

/* Accent text utilities */
.text-cyan { color: var(--c-accent); }
.text-magenta { color: var(--c-accent-2); }
.text-yellow { color: var(--c-accent-3); }
.text-mono { font-family: var(--font-mono); }

/* ============================================================
   ELEMENT PLUS — 双轨作用域覆盖
   规则：组件形状按容器轨道切换，撤销旧系统全局 !important 强制。
   - .track-glass  → 玻璃后台（圆角/毛玻璃/柔和影/无大写）
   - .track-brutal → 粗野大屏（硬边/硬影/大写/尖角）
   默认轨道为玻璃（App.vue 根容器 .track-glass）。
   ============================================================ */

/* ---------- TRACK A · 玻璃后台（先定义）---------- */
.track-glass {
  /* Cards */
  .el-card {
    background: var(--surface) !important;
    backdrop-filter: blur(var(--blur)) !important;
    -webkit-backdrop-filter: blur(var(--blur)) !important;
    border: 1px solid var(--border) !important;
    border-radius: var(--radius-lg) !important;
    box-shadow: var(--shadow-2) !important;
    color: var(--text) !important;
    transition: box-shadow var(--motion) var(--ease), transform var(--motion) var(--ease) !important;
  }
  .el-card:hover {
    box-shadow: var(--shadow-2), var(--glow) !important;
    transform: translateY(-2px);
  }
  .el-card__header {
    border-bottom: 1px solid var(--border) !important;
    padding: 14px 18px !important;
    font-weight: 600;
    text-transform: none;
    letter-spacing: normal;
  }
  .el-card__body { padding: 18px !important; }

  /* Buttons */
  .el-button--primary {
    background: var(--primary) !important;
    border: 1px solid transparent !important;
    border-radius: var(--radius-md) !important;
    color: var(--brand-ink) !important;
    font-weight: 600 !important;
    box-shadow: var(--glow) !important;
    transition: box-shadow var(--motion-fast) var(--ease), transform var(--motion-fast) var(--ease) !important;
    text-transform: none;
    letter-spacing: normal;
  }
  .el-button--primary:hover {
    box-shadow: var(--glow), 0 0 32px rgba(0,229,255,.30) !important;
    transform: translateY(-1px);
  }
  .el-button--primary:active { transform: translateY(1px); box-shadow: var(--glow) !important; }
  .el-button--primary:focus-visible { outline: 2px solid var(--primary); outline-offset: 2px; }

  .el-button--danger {
    background: var(--danger) !important;
    border: 1px solid transparent !important;
    border-radius: var(--radius-md) !important;
    color: #fff !important;
    font-weight: 600 !important;
    text-transform: none;
    letter-spacing: normal;
  }
  .el-button--danger:hover { box-shadow: 0 0 24px rgba(255,92,122,.40) !important; }
  .el-button--danger:focus-visible { outline: 2px solid var(--danger); outline-offset: 2px; }

  .el-button {
    border-radius: var(--radius-md) !important;
    font-weight: 500 !important;
    border-width: 1px !important;
  }
  .el-button:focus-visible { outline: 2px solid var(--primary); outline-offset: 2px; }

  /* Inputs */
  .el-input__wrapper {
    background: var(--surface) !important;
    border: 1px solid var(--border) !important;
    border-radius: var(--radius-md) !important;
    box-shadow: none !important;
    transition: border-color var(--motion-fast) var(--ease) !important;
  }
  .el-input__wrapper:hover { border-color: var(--text-2) !important; }
  .el-input__wrapper.is-focus {
    border-color: var(--primary) !important;
    box-shadow: 0 0 0 3px rgba(0,229,255,.18) !important;
  }
  .el-select .el-input__wrapper { border-radius: var(--radius-md) !important; }

  /* Table（数据密集 — 保证可读性，走玻璃）*/
  .el-table {
    background: transparent !important;
    --el-table-tr-bg-color: transparent;
    --el-table-header-bg-color: var(--surface);
    font-family: var(--font-sans);
    font-size: 14px;
  }
  .el-table th.el-table__cell {
    background: var(--surface) !important;
    font-weight: 600;
    font-size: 12px;
    text-transform: none;
    letter-spacing: normal;
    color: var(--text-2) !important;
    border-bottom: 1px solid var(--border) !important;
  }
  .el-table td.el-table__cell {
    border-bottom: 1px solid var(--border) !important;
    color: var(--text);
  }
  .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
    background: rgba(255,255,255,.03) !important;
  }

  /* Dialog */
  .el-dialog {
    background: var(--surface-strong) !important;
    backdrop-filter: blur(var(--blur));
    -webkit-backdrop-filter: blur(var(--blur));
    border: 1px solid var(--border-strong) !important;
    border-radius: var(--radius-lg) !important;
    box-shadow: var(--shadow-2) !important;
  }
  .el-dialog__header {
    border-bottom: 1px solid var(--border) !important;
    padding: 16px 20px !important;
    margin-right: 0 !important;
  }
  .el-dialog__body { padding: 20px !important; }
  .el-dialog__footer { border-top: 1px solid var(--border) !important; padding: 12px 20px !important; }

  /* Message Box */
  .el-message-box {
    background: var(--surface-strong) !important;
    border: 1px solid var(--border-strong) !important;
    border-radius: var(--radius-lg) !important;
    box-shadow: var(--shadow-2) !important;
  }

  /* Dropdown */
  .el-dropdown-menu {
    background: var(--surface-strong) !important;
    border: 1px solid var(--border) !important;
    border-radius: var(--radius-md) !important;
    box-shadow: var(--shadow-2) !important;
  }

  /* Menu（玻璃侧栏）*/
  .el-menu { border-right: none !important; background: transparent !important; }
  .el-menu-item, .el-sub-menu__title {
    border-radius: var(--radius-md) !important;
    margin: 2px 6px !important;
    font-weight: 500 !important;
  }
  .el-menu-item.is-active {
    background: rgba(0,229,255,.12) !important;
    color: var(--primary) !important;
    font-weight: 700 !important;
    border-left: 3px solid var(--primary) !important;
  }

  /* Tags（圆角胶囊 + 语义淡底）*/
  .el-tag {
    border-radius: var(--radius-pill) !important;
    border: 1px solid !important;
    font-weight: 600 !important;
    text-transform: none;
    font-size: 12px;
    letter-spacing: normal;
  }

  /* Pagination */
  .el-pagination { --el-pagination-bg-color: transparent; }
  .el-pagination .btn-prev, .el-pagination .btn-next, .el-pager li {
    border: 1px solid var(--border) !important;
    border-radius: var(--radius-md) !important;
    background: var(--surface) !important;
    font-weight: 500 !important;
  }
  .el-pager li.is-active {
    background: var(--primary) !important;
    color: var(--brand-ink) !important;
    border-color: transparent !important;
  }

  /* Steps */
  .el-steps { background: transparent; }
  .el-step__head { background: transparent; }

  /* Tabs */
  .el-tabs__header { border-bottom: 1px solid var(--border) !important; }
  .el-tabs__item.is-active { color: var(--primary) !important; font-weight: 700 !important; }
  .el-tabs__active-bar { background: var(--brand-magenta) !important; height: 2px !important; }
}

/* ---------- TRACK B · 粗野大屏（后定义，内层优先胜出）---------- */
.track-brutal {
  /* Cards */
  .el-card {
    background: var(--c-card) !important;
    border: var(--bw) solid var(--c-border) !important;
    border-radius: var(--radius-card) !important;
    box-shadow: var(--shadow-hard) !important;
    transition: box-shadow 0.12s ease, transform 0.12s ease !important;
  }
  .el-card:hover {
    box-shadow: var(--shadow-hover) !important;
    transform: translate(-1px, -1px);
  }
  .el-card__header {
    border-bottom: var(--bw) solid var(--c-border) !important;
    padding: 14px 18px !important;
    font-weight: 800;
    text-transform: uppercase;
    letter-spacing: 0.04em;
  }
  .el-card__body { padding: 18px !important; }

  /* Buttons */
  .el-button--primary {
    background: var(--c-accent) !important;
    border: var(--bw) solid #000 !important;
    border-radius: var(--radius-md) !important;
    color: var(--c-text-inverse) !important;
    font-weight: 800 !important;
    box-shadow: 4px 4px 0 0 #000 !important;
    transition: all 0.12s ease !important;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }
  .el-button--primary:hover {
    box-shadow: 6px 6px 0 0 #000, var(--shadow-glow-cyan) !important;
    transform: translate(-2px, -2px);
  }
  .el-button--primary:active {
    box-shadow: none !important;
    transform: translate(2px, 2px);
  }
  .el-button--danger {
    background: var(--c-danger) !important;
    border: var(--bw) solid #000 !important;
    border-radius: var(--radius-md) !important;
    color: #fff !important;
    font-weight: 800 !important;
    box-shadow: 4px 4px 0 0 #000 !important;
    transition: all 0.12s ease !important;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }
  .el-button--danger:hover {
    box-shadow: 6px 6px 0 0 #000 !important;
    transform: translate(-2px, -2px);
  }
  .el-button {
    border-radius: var(--radius-md) !important;
    font-weight: 700 !important;
    border-width: 2px !important;
  }

  /* Inputs */
  .el-input__wrapper {
    background: var(--c-bg-alt) !important;
    border: var(--bw) solid var(--c-border) !important;
    border-radius: var(--radius-md) !important;
    box-shadow: none !important;
    transition: border-color 0.12s ease !important;
  }
  .el-input__wrapper:hover { border-color: var(--c-text-tertiary) !important; }
  .el-input__wrapper.is-focus {
    border-color: var(--c-accent) !important;
    box-shadow: 4px 4px 0 0 rgba(0,0,0,0.25) !important;
  }

  /* Select */
  .el-select .el-input__wrapper { border-radius: var(--radius-md) !important; }

  /* Table */
  .el-table {
    background: transparent !important;
    --el-table-tr-bg-color: transparent;
    --el-table-header-bg-color: var(--c-bg-alt);
    font-family: var(--font-mono);
    font-size: 13px;
  }
  .el-table th.el-table__cell {
    background: var(--c-bg-alt) !important;
    font-weight: 800;
    font-size: 11px;
    text-transform: uppercase;
    letter-spacing: 0.10em;
    color: var(--c-accent) !important;
    border-bottom: var(--bw) solid var(--c-border) !important;
  }
  .el-table td.el-table__cell { border-bottom: 1px solid var(--c-border-light) !important; }
  .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
    background: var(--c-bg-alt) !important;
  }

  /* Dialog */
  .el-dialog {
    background: var(--c-card) !important;
    border: var(--bw-bold) solid var(--c-border) !important;
    border-radius: var(--radius-card) !important;
    box-shadow: var(--shadow-lg) !important;
  }
  .el-dialog__header {
    border-bottom: var(--bw) solid var(--c-border) !important;
    padding: 16px 20px !important;
    margin-right: 0 !important;
  }
  .el-dialog__body { padding: 20px !important; }
  .el-dialog__footer {
    border-top: var(--bw) solid var(--c-border) !important;
    padding: 12px 20px !important;
  }

  /* Message Box */
  .el-message-box {
    background: var(--c-card) !important;
    border: var(--bw-bold) solid var(--c-border) !important;
    border-radius: var(--radius-card) !important;
    box-shadow: var(--shadow-lg) !important;
  }

  /* Dropdown */
  .el-dropdown-menu {
    background: var(--c-card) !important;
    border: var(--bw) solid var(--c-border) !important;
    border-radius: var(--radius-md) !important;
    box-shadow: var(--shadow-md) !important;
  }

  /* Menu */
  .el-menu { border-right: none !important; background: transparent !important; }
  .el-menu-item, .el-sub-menu__title {
    border-radius: var(--radius-md) !important;
    margin: 2px 6px !important;
    font-weight: 600 !important;
  }
  .el-menu-item.is-active {
    background: var(--c-accent-bg) !important;
    color: var(--c-accent) !important;
    font-weight: 800 !important;
    border-left: var(--bw) solid var(--c-accent) !important;
  }

  /* Tags */
  .el-tag {
    border-radius: var(--radius-sm) !important;
    border: 2px solid !important;
    font-weight: 700 !important;
    text-transform: uppercase;
    font-size: 11px;
    letter-spacing: 0.05em;
  }

  /* Pagination */
  .el-pagination { --el-pagination-bg-color: transparent; }
  .el-pagination .btn-prev, .el-pagination .btn-next, .el-pager li {
    border: var(--bw) solid var(--c-border) !important;
    border-radius: var(--radius-md) !important;
    background: var(--c-card) !important;
    font-weight: 700 !important;
  }
  .el-pager li.is-active {
    background: var(--c-accent) !important;
    color: var(--c-text-inverse) !important;
    border-color: #000 !important;
  }

  /* Steps */
  .el-steps { background: transparent; }
  .el-step__head { background: transparent; }

  /* Tabs */
  .el-tabs__header { border-bottom: var(--bw) solid var(--c-border) !important; }
  .el-tabs__item.is-active { color: var(--c-accent) !important; font-weight: 800 !important; }
  .el-tabs__active-bar { background: var(--c-accent-2) !important; height: 3px !important; }
}

/* ========================================
   SCROLLBAR
   ======================================== */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb {
  background: var(--c-border);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--c-text-tertiary);
}

/* ========================================
   REDUCED MOTION（覆盖双轨 hover 位移）
   ======================================== */
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
  .brutal-card:hover,
  .track-glass .el-card:hover,
  .track-glass .el-button--primary:hover,
  .track-brutal .el-card:hover,
  .track-brutal .el-button--primary:hover { transform: none !important; }
}
</style>
