<template>
  <div style="display:none" />
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useTresContext, useLoop } from '@tresjs/core'
import * as THREE from 'three'
import { buildings, particlePaths, getRateColor } from './campusData.js'

const emit = defineEmits(['buildingSelect'])
const { scene, camera, renderer } = useTresContext()
const cleanupFns = []

onMounted(() => {
  // --- 雾 ---
  scene.value.fog = new THREE.Fog('#0a0a1a', 25, 55)

  // --- 手动创建 OrbitControls (避免 @tresjs/cientos 组件导致自定义元素渲染崩溃) ---
  if (renderer?.value?.domElement) {
    const controls = new THREE.OrbitControls(camera.value, renderer.value.domElement)
    controls.enableDamping = true
    controls.dampingFactor = 0.08
    controls.minDistance = 8
    controls.maxDistance = 40
    controls.maxPolarAngle = 1.4
    controls.autoRotate = true
    controls.autoRotateSpeed = 0.3
    controls.target.set(0, 1, 0)
    controls.update()
    cleanupFns.push(() => controls.dispose())
  }

  // --- 灯光 ---
  const ambient = new THREE.AmbientLight(0xffffff, 0.45)
  const dirLight = new THREE.DirectionalLight(0xffffff, 1.0)
  dirLight.position.set(15, 25, 10)
  const hemi = new THREE.HemisphereLight('#87CEEB', '#362c2c', 0.4)
  scene.value.add(ambient, dirLight, hemi)
  cleanupFns.push(() => scene.value.remove(ambient, dirLight, hemi))

  // --- 网格 ---
  const grid = new THREE.GridHelper(42, 22, '#007AFF', '#2a2a3e')
  scene.value.add(grid)
  cleanupFns.push(() => { scene.value.remove(grid); grid.dispose() })

  // --- 热力图地面 ---
  const groundGeo = new THREE.PlaneGeometry(42, 42)
  const heatTex = generateHeatTexture()
  const groundMat = heatTex
    ? new THREE.MeshStandardMaterial({ map: heatTex, color: '#1a1a2e', roughness: 0.85, metalness: 0.15 })
    : new THREE.MeshStandardMaterial({ color: '#1a1a2e', roughness: 0.85, metalness: 0.15 })
  const ground = new THREE.Mesh(groundGeo, groundMat)
  ground.rotation.x = -Math.PI / 2
  ground.position.y = -0.05
  scene.value.add(ground)
  cleanupFns.push(() => { scene.value.remove(ground); groundGeo.dispose(); groundMat.dispose() })

  // --- 建筑群 ---
  const buildingMeshes = []
  buildings.forEach(b => {
    const height = 0.5 + (b.size[1] - 0.5) * Math.max(0.3, b.attendanceRate)
    const color = new THREE.Color(getRateColor(b.attendanceRate))
    const geo = new THREE.BoxGeometry(b.size[0], height, b.size[2])
    const mat = new THREE.MeshStandardMaterial({
      color, emissive: color, emissiveIntensity: 0.12, metalness: 0.15, roughness: 0.75
    })
    const mesh = new THREE.Mesh(geo, mat)
    mesh.position.set(b.position[0], b.position[1] + height / 2, b.position[2])
    mesh.castShadow = true
    mesh.receiveShadow = true
    mesh.userData = { buildingData: b }

    // 顶部发光线框
    const edgeGeo = new THREE.EdgesGeometry(geo)
    const edgeMat = new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0.6 })
    const edgeLine = new THREE.LineSegments(edgeGeo, edgeMat)
    edgeLine.position.copy(mesh.position)
    mesh.add(edgeLine)

    scene.value.add(mesh)
    buildingMeshes.push(mesh)
    cleanupFns.push(() => { scene.value.remove(mesh); geo.dispose(); mat.dispose(); edgeGeo.dispose(); edgeMat.dispose() })
  })

  // --- 粒子系统 ---
  const pd = initParticles()
  const pointsGeo = new THREE.BufferGeometry()
  pointsGeo.setAttribute('position', new THREE.BufferAttribute(pd.positions, 3))
  const pointsMat = new THREE.PointsMaterial({
    size: 0.2,
    color: new THREE.Color('#00D4FF'),
    transparent: true, opacity: 0.7,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  })
  const points = new THREE.Points(pointsGeo, pointsMat)
  scene.value.add(points)
  const velocities = pd.velocities
  cleanupFns.push(() => { scene.value.remove(points); pointsGeo.dispose(); pointsMat.dispose() })

  // --- 建筑间路径 ---
  const seen = new Set()
  particlePaths.forEach(p => {
    const key = [p.from, p.to].sort().join('-')
    if (seen.has(key)) return
    seen.add(key)
    const a = buildings[p.from]; const b = buildings[p.to]
    const dx = b.position[0] - a.position[0]; const dz = b.position[2] - a.position[2]
    const length = Math.sqrt(dx * dx + dz * dz)
    const angle = Math.atan2(dx, dz)
    const midX = (a.position[0] + b.position[0]) / 2
    const midZ = (a.position[2] + b.position[2]) / 2
    const pathGeo = new THREE.PlaneGeometry(length, 0.35)
    const pathMat = new THREE.MeshBasicMaterial({
      color: getRateColor(Math.min(a.attendanceRate, b.attendanceRate)),
      transparent: true, opacity: 0.3, depthWrite: false
    })
    const pathPlane = new THREE.Mesh(pathGeo, pathMat)
    pathPlane.position.set(midX, 0.02, midZ)
    pathPlane.rotation.y = angle
    scene.value.add(pathPlane)
    cleanupFns.push(() => { scene.value.remove(pathPlane); pathGeo.dispose(); pathMat.dispose() })
  })

  // --- 渲染循环 ---
  const { onRender } = useLoop()
  onRender(({ delta }) => {
    if (!points) return
    const arr = pointsGeo.attributes.position.array
    const dt = Math.min(delta, 0.1)
    for (let i = 0; i < arr.length / 3; i++) {
      arr[i * 3] += velocities[i * 3] * dt
      arr[i * 3 + 1] += velocities[i * 3 + 1] * dt
      arr[i * 3 + 2] += velocities[i * 3 + 2] * dt
      const px = arr[i * 3]; const py = arr[i * 3 + 1]; const pz = arr[i * 3 + 2]
      if (px < -10 || px > 10 || pz < -7 || pz > 7 || py > 5 || py < 0) {
        const pathIdx = Math.floor(Math.random() * particlePaths.length)
        const path = particlePaths[pathIdx]
        const f = buildings[path.from]; const t = buildings[path.to]
        arr[i * 3] = f.position[0] + (Math.random() - 0.5) * 2.5
        arr[i * 3 + 1] = f.position[1] + 1 + Math.random() * 2.5
        arr[i * 3 + 2] = f.position[2] + (Math.random() - 0.5) * 2.5
        const vx = t.position[0] - arr[i * 3]; const vy = (t.position[1] + 1.5) - arr[i * 3 + 1]; const vz = t.position[2] - arr[i * 3 + 2]
        const len = Math.sqrt(vx * vx + vy * vy + vz * vz) || 1
        const speed = 0.015 + Math.random() * 0.025
        velocities[i * 3] = (vx / len) * speed; velocities[i * 3 + 1] = (vy / len) * speed; velocities[i * 3 + 2] = (vz / len) * speed
      }
    }
    pointsGeo.attributes.position.needsUpdate = true
  })

  // --- Raycaster ---
  const raycaster = new THREE.Raycaster()
  const mouse = new THREE.Vector2()
  function onClick(event) {
    mouse.x = (event.clientX / window.innerWidth) * 2 - 1
    mouse.y = -(event.clientY / window.innerHeight) * 2 + 1
    raycaster.setFromCamera(mouse, camera.value)
    const intersects = raycaster.intersectObjects(buildingMeshes)
    if (intersects.length > 0) {
      const data = intersects[0].object.userData.buildingData
      if (data) emit('buildingSelect', data)
    }
  }
  window.addEventListener('click', onClick)
  cleanupFns.push(() => window.removeEventListener('click', onClick))
})

onUnmounted(() => {
  cleanupFns.forEach(fn => fn())
})

// --- helpers ---
function generateHeatTexture() {
  const size = 1024
  const canvas = document.createElement('canvas')
  canvas.width = size; canvas.height = size
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#0f0f1a'; ctx.fillRect(0, 0, size, size)
  const step = size / 22
  ctx.strokeStyle = 'rgba(0, 122, 255, 0.04)'; ctx.lineWidth = 1
  for (let i = 0; i <= 22; i++) {
    ctx.beginPath(); ctx.moveTo(i * step, 0); ctx.lineTo(i * step, size); ctx.stroke()
    ctx.beginPath(); ctx.moveTo(0, i * step); ctx.lineTo(size, i * step); ctx.stroke()
  }
  const mr = (v, iMin, iMax, oMin, oMax) => ((v - iMin) / (iMax - iMin)) * (oMax - oMin) + oMin
  buildings.forEach(b => {
    const tx = mr(b.position[0], -21, 21, 0, size)
    const ty = mr(b.position[2], -21, 21, 0, size)
    const intensity = 1 - b.attendanceRate
    const radius = 60 + intensity * 120
    const gradient = ctx.createRadialGradient(tx, ty, 0, tx, ty, radius)
    gradient.addColorStop(0, `rgba(0,122,255,${intensity * 0.5})`)
    gradient.addColorStop(0.6, `rgba(0,122,255,${intensity * 0.15})`)
    gradient.addColorStop(1, 'rgba(0,122,255,0)')
    ctx.fillStyle = gradient
    ctx.beginPath(); ctx.arc(tx, ty, radius, 0, Math.PI * 2); ctx.fill()
  })
  const tex = new THREE.CanvasTexture(canvas)
  tex.wrapS = tex.wrapT = THREE.ClampToEdgeWrapping
  return tex
}

function initParticles() {
  const posArray = []; const vels = []
  particlePaths.forEach(path => {
    const fromB = buildings[path.from]; const toB = buildings[path.to]
    for (let i = 0; i < path.count; i++) {
      const fx = fromB.position[0] + (Math.random() - 0.5) * 2.5
      const fz = fromB.position[2] + (Math.random() - 0.5) * 2.5
      const fy = fromB.position[1] + 1 + Math.random() * 2.5
      posArray.push(fx, fy, fz)
      const vx = toB.position[0] - fx; const vy = (toB.position[1] + 1.5) - fy; const vz = toB.position[2] - fz
      const len = Math.sqrt(vx * vx + vy * vy + vz * vz) || 1
      const speed = 0.015 + Math.random() * 0.025
      vels.push((vx / len) * speed, (vy / len) * speed, (vz / len) * speed)
    }
  })
  return { positions: new Float32Array(posArray), velocities: vels }
}
</script>
