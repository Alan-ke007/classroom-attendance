<template>
  <TresCanvas
    :camera-position="[18, 14, 18]"
    :fov="50"
    alpha
  >
  </TresCanvas>
</template>

<script setup>
import { TresCanvas } from '@tresjs/core'
</script>
