<template>
  <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '切换到亮色模式' : '切换到暗色模式'">
    <span class="toggle-track">
      <span class="toggle-icon sun" :class="{ active: !isDark }">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <circle cx="12" cy="12" r="5" />
          <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
        </svg>
      </span>
      <span class="toggle-icon moon" :class="{ active: isDark }">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
        </svg>
      </span>
    </span>
  </button>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'

const { isDark, toggleTheme } = useTheme()
</script>

<style scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  background: var(--c-glass-bg);
  border: 1px solid var(--c-border);
  border-radius: 50%;
  cursor: pointer;
  transition: all var(--transition);
  color: var(--c-text-secondary);
}

.theme-toggle:hover {
  background: var(--c-primary-bg);
  border-color: var(--c-primary);
  color: var(--c-primary);
  box-shadow: var(--shadow-glow);
}

.toggle-track {
  position: relative;
  width: 14px;
  height: 14px;
}

.toggle-icon {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: rotate(90deg) scale(0.5);
  transition: all 0.3s ease;
}

.toggle-icon.active {
  opacity: 1;
  transform: rotate(0deg) scale(1);
}
</style>
