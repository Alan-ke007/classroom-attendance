<template>
  <span class="data-flow-text" :class="{ glitch: glitch }" :style="{ '--df-color': color }">
    {{ text }}
  </span>
</template>

<script setup>
defineProps({
  text: { type: String, default: '' },
  color: { type: String, default: '#007AFF' },
  glitch: { type: Boolean, default: false }
})
</script>

<style scoped>
.data-flow-text {
  font-family: 'Courier New', 'Source Code Pro', monospace;
  color: var(--df-color);
  letter-spacing: 0.05em;
  animation: dataPulse 2s ease-in-out infinite;
}
.data-flow-text.glitch {
  animation: glitchSkew 0.3s ease-in-out infinite alternate, dataPulse 2s ease-in-out infinite;
}
@keyframes dataPulse {
  0%, 100% { opacity: 0.8; text-shadow: 0 0 4px var(--df-color); }
  50% { opacity: 1; text-shadow: 0 0 12px var(--df-color), 0 0 24px var(--df-color); }
}
@keyframes glitchSkew {
  0% { transform: translate(0); }
  20% { transform: translate(-2px, 1px); }
  40% { transform: translate(2px, -1px); }
  60% { transform: translate(-1px, 2px); }
  80% { transform: translate(1px, -2px); }
  100% { transform: translate(0); }
}
</style>
