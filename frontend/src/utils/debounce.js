// 按钮防抖指令 - 防止重复点击
const vDebounce = {
  mounted(el, binding) {
    const delay = binding.value || 500
    let disabled = false
    el.addEventListener('click', (e) => {
      if (disabled) {
        e.preventDefault()
        e.stopPropagation()
        return
      }
      disabled = true
      setTimeout(() => { disabled = false }, delay)
    }, true)
  }
}

export function setupDebounce(app) {
  app.directive('debounce', vDebounce)
}
