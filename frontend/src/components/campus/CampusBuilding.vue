<template>
  <TresMesh
    :position="[pos[0], pos[1] + mappedHeight / 2, pos[2]]"
    cast-shadow
    receive-shadow
    @click="onClick"
  >
    <TresBoxGeometry :args="[size[0], mappedHeight, size[2]]" />
    <TresMeshStandardMaterial
      :color="displayColor"
      :emissive="displayColor"
      :emissive-intensity="0.12"
      :metalness="0.15"
      :roughness="0.75"
    />
    <!-- 建筑顶部发光边框 -->
    <TresEdgesGeometry v-if="false" :args="[boxGeo]" />
  </TresMesh>
</template>

<script setup>
import { computed } from 'vue'
import { getRateColor } from './campusData.js'

const props = defineProps({
  data: { type: Object, required: true }
})

const emit = defineEmits(['select'])

const pos = computed(() => props.data.position)
const size = computed(() => props.data.size)

const mappedHeight = computed(() => {
  const rate = props.data.attendanceRate
  return 0.5 + (props.data.size[1] - 0.5) * Math.max(0.3, rate)
})

const displayColor = computed(() => getRateColor(props.data.attendanceRate))

function onClick() {
  emit('select', props.data)
}
</script>
