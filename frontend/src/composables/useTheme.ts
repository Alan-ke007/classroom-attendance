import { ref, watchEffect } from 'vue'

const isDark = ref(true)

export function useTheme() {
  const stored = localStorage.getItem('theme')
  if (stored === 'light' || stored === 'dark') {
    isDark.value = stored === 'dark'
  }

  watchEffect(() => {
    const theme = isDark.value ? 'dark' : 'light'
    document.documentElement.dataset.theme = theme
    localStorage.setItem('theme', theme)
  })

  function toggleTheme() {
    isDark.value = !isDark.value
  }

  return { isDark, toggleTheme }
}
