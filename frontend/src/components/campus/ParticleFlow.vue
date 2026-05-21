<template>
  <div style="display:none" />
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useLoop, useTresContext } from '@tresjs/core'
import * as THREE from 'three'
import { buildings, particlePaths } from './campusData.js'

const { scene } = useTresContext()

let pointsMesh = null
const velocities = []

function createParticles() {
  const posArray = []
  particlePaths.forEach(path => {
    const fromB = buildings[path.from]
    const toB = buildings[path.to]
    for (let i = 0; i < path.count; i++) {
      const fx = fromB.position[0] + (Math.random() - 0.5) * 2.5
      const fz = fromB.position[2] + (Math.random() - 0.5) * 2.5
      const fy = fromB.position[1] + 1 + Math.random() * 2.5
      posArray.push(fx, fy, fz)

      const dx = toB.position[0] - fx
      const dy = (toB.position[1] + 1.5) - fy
      const dz = toB.position[2] - fz
      const len = Math.sqrt(dx * dx + dy * dy + dz * dz) || 1
      const speed = 0.015 + Math.random() * 0.025
      velocities.push((dx / len) * speed, (dy / len) * speed, (dz / len) * speed)
    }
  })

  const geometry = new THREE.BufferGeometry()
  geometry.setAttribute('position', new THREE.BufferAttribute(new Float32Array(posArray), 3))

  const material = new THREE.PointsMaterial({
    size: 0.2,
    color: new THREE.Color('#00D4FF'),
    transparent: true,
    opacity: 0.7,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    sizeAttenuation: true
  })

  pointsMesh = new THREE.Points(geometry, material)
  scene.value.add(pointsMesh)
}

const { onRender } = useLoop()

onRender(({ delta }) => {
  if (!pointsMesh) return
  const arr = pointsMesh.geometry.attributes.position.array
  const dt = Math.min(delta, 0.1)

  for (let i = 0; i < arr.length / 3; i++) {
    arr[i * 3] += velocities[i * 3] * dt
    arr[i * 3 + 1] += velocities[i * 3 + 1] * dt
    arr[i * 3 + 2] += velocities[i * 3 + 2] * dt

    const px = arr[i * 3], py = arr[i * 3 + 1], pz = arr[i * 3 + 2]
    if (px < -10 || px > 10 || pz < -7 || pz > 7 || py > 5 || py < 0) {
      const pathIdx = Math.floor(Math.random() * particlePaths.length)
      const path = particlePaths[pathIdx]
      const fromB = buildings[path.from]
      arr[i * 3] = fromB.position[0] + (Math.random() - 0.5) * 2.5
      arr[i * 3 + 1] = fromB.position[1] + 1 + Math.random() * 2.5
      arr[i * 3 + 2] = fromB.position[2] + (Math.random() - 0.5) * 2.5
      const toB = buildings[path.to]
      const dx = toB.position[0] - arr[i * 3]
      const dy = (toB.position[1] + 1.5) - arr[i * 3 + 1]
      const dz = toB.position[2] - arr[i * 3 + 2]
      const len = Math.sqrt(dx * dx + dy * dy + dz * dz) || 1
      const speed = 0.015 + Math.random() * 0.025
      velocities[i * 3] = (dx / len) * speed
      velocities[i * 3 + 1] = (dy / len) * speed
      velocities[i * 3 + 2] = (dz / len) * speed
    }
  }

  pointsMesh.geometry.attributes.position.needsUpdate = true
})

onMounted(() => {
  createParticles()
})

onUnmounted(() => {
  if (pointsMesh) {
    scene.value.remove(pointsMesh)
    pointsMesh.geometry.dispose()
    pointsMesh.material.dispose()
    pointsMesh = null
  }
})
</script>
