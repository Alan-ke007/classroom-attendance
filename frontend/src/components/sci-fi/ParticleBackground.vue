<template>
  <VueParticles
    :id="`particles-${instanceId}`"
    :options="particleConfig"
    :style="containerStyle"
  />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  mode: {
    type: String,
    default: 'network',
    validator: v => ['network', 'dataflow', 'floating'].includes(v)
  },
  color: { type: String, default: '#007AFF' },
  positionType: { type: String, default: 'fixed' }
})

const instanceId = Math.random().toString(36).slice(2, 8)

const containerStyle = {
  position: props.positionType,
  inset: 0,
  zIndex: 0,
  pointerEvents: 'none'
}

const particleConfig = computed(() => {
  const base = {
    fullScreen: false,
    fpsLimit: 60,
    detectRetina: true
  }
  const configs = {
    network: {
      ...base,
      particles: {
        number: { value: 60, density: { enable: true, area: 800 } },
        color: { value: props.color },
        links: {
          enable: true,
          distance: 150,
          color: props.color,
          opacity: 0.25,
          width: 1
        },
        move: { enable: true, speed: 1.2, direction: 'none', random: true, outModes: 'bounce' },
        size: { value: 3, random: true },
        opacity: { value: 0.4 }
      }
    },
    dataflow: {
      ...base,
      particles: {
        number: { value: 40, density: { enable: true, area: 800 } },
        color: { value: props.color },
        links: {
          enable: true,
          distance: 200,
          color: props.color,
          opacity: 0.15,
          width: 0.5
        },
        move: {
          enable: true,
          speed: 2,
          direction: 'top',
          straight: true,
          outModes: 'out'
        },
        size: { value: 2, random: true },
        opacity: { value: 0.25 }
      }
    },
    floating: {
      ...base,
      particles: {
        number: { value: 30, density: { enable: true, area: 800 } },
        color: { value: props.color },
        links: { enable: false },
        move: {
          enable: true,
          speed: 0.3,
          direction: 'none',
          random: true,
          outModes: 'bounce'
        },
        size: { value: { min: 1, max: 3 }, random: true },
        opacity: { value: { min: 0.1, max: 0.3 } }
      }
    }
  }
  return configs[props.mode]
})
</script>
