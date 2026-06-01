<template>
  <div class="glowing-border" :style="borderStyle">
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  colorStart: { type: String, default: '#00E5FF' },
  colorEnd: { type: String, default: '#E040FB' },
  thickness: { type: Number, default: 3 },
  borderRadius: { type: String, default: '12px' },
  backgroundColor: { type: String, default: 'var(--c-card, #161616)' }
})

const borderStyle = computed(() => ({
  '--gb-start': props.colorStart,
  '--gb-end': props.colorEnd,
  '--gb-thick': props.thickness + 'px',
  '--gb-radius': props.borderRadius,
  '--gb-bg': props.backgroundColor
}))
</script>

<style scoped>
.glowing-border {
  position: relative;
  border-radius: var(--gb-radius);
  overflow: hidden;
}
.glowing-border::before {
  content: '';
  position: absolute;
  inset: calc(-1 * var(--gb-thick));
  border-radius: inherit;
  background: conic-gradient(
    from 0deg,
    var(--gb-start),
    var(--gb-end),
    var(--gb-start),
    #FFD600,
    var(--gb-start)
  );
  animation: spinBorder 3s linear infinite;
  z-index: 0;
}
.glowing-border::after {
  content: '';
  position: absolute;
  inset: var(--gb-thick);
  border-radius: inherit;
  background: var(--gb-bg);
  z-index: 0;
}
.glowing-border > :deep(*) {
  position: relative;
  z-index: 1;
}
@keyframes spinBorder {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
