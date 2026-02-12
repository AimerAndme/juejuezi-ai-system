<template>
  <div ref="canvasContainer" class="starry-background"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'

const canvasContainer = ref(null)
let scene,
  camera,
  renderer,
  stars,
  meteors = []
let animationId = null
let meteorTimer = null

// 初始化场景
const initScene = () => {
  // 创建场景
  scene = new THREE.Scene()

  // 创建相机
  const width = canvasContainer.value.clientWidth
  const height = canvasContainer.value.clientHeight
  camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 2000)
  camera.position.z = 5

  // 创建渲染器
  renderer = new THREE.WebGLRenderer({
    antialias: true,
    alpha: true,
  })
  renderer.setSize(width, height)
  renderer.setPixelRatio(window.devicePixelRatio)
  canvasContainer.value.appendChild(renderer.domElement)
}

// 创建星星
const createStars = () => {
  const starCount = 800 // 控制在 1000 以内
  const geometry = new THREE.BufferGeometry()
  const positions = []
  const velocities = []

  for (let i = 0; i < starCount; i++) {
    const x = (Math.random() - 0.5) * 2000
    const y = (Math.random() - 0.5) * 2000
    const z = (Math.random() - 0.5) * 1500
    positions.push(x, y, z)
    velocities.push(Math.random() * 0.3 + 0.1)
  }

  geometry.setAttribute(
    'position',
    new THREE.Float32BufferAttribute(positions, 3)
  )

  // 创建星星材质
  const material = new THREE.PointsMaterial({
    color: 0xffffff,
    size: 2,
    sizeAttenuation: true,
    transparent: true,
    opacity: 0.8,
  })

  stars = new THREE.Points(geometry, material)
  stars.userData.velocities = velocities
  scene.add(stars)
}

// 创建流星
const createMeteor = () => {
  // 流星头部（小圆点）
  const meteorHead = new THREE.Group()

  // 紫色小圆点
  const headGeometry = new THREE.SphereGeometry(3, 16, 16)
  const headMaterial = new THREE.MeshBasicMaterial({
    color: 0x9d4edd, // 紫色
    transparent: true,
    opacity: 1,
  })
  const head = new THREE.Mesh(headGeometry, headMaterial)
  meteorHead.add(head)

  // 发光效果（外层光晕）
  const glowGeometry = new THREE.SphereGeometry(5, 16, 16)
  const glowMaterial = new THREE.MeshBasicMaterial({
    color: 0xc77dff, // 浅紫色
    transparent: true,
    opacity: 0.4,
  })
  const glow = new THREE.Mesh(glowGeometry, glowMaterial)
  meteorHead.add(glow)

  // 光尾（渐变拖尾效果）
  const tailLength = 120
  const tailSegments = 30
  const tailGeometry = new THREE.BufferGeometry()
  const tailPositions = []
  const tailColors = []

  for (let i = 0; i < tailSegments; i++) {
    const progress = i / (tailSegments - 1)
    tailPositions.push(-progress * tailLength, progress * tailLength * 0.3, 0)

    // 颜色从深紫渐变到透明
    const r = 0.61 + progress * 0.39 // 0x9d -> 1.0
    const g = 0.3 + progress * 0.7 // 0x4e -> 1.0
    const b = 0.87 + progress * 0.13 // 0xdd -> 1.0
    tailColors.push(r, g, b)
  }

  tailGeometry.setAttribute(
    'position',
    new THREE.Float32BufferAttribute(tailPositions, 3)
  )
  tailGeometry.setAttribute(
    'color',
    new THREE.Float32BufferAttribute(tailColors, 3)
  )

  const tailMaterial = new THREE.LineBasicMaterial({
    vertexColors: true,
    transparent: true,
    opacity: 0.8,
    linewidth: 2, // 纤细线条
  })

  const tail = new THREE.Line(tailGeometry, tailMaterial)
  meteorHead.add(tail)

  // 随机起始位置（从屏幕上方或右上方开始）
  const startX = 400 + Math.random() * 600
  const startY = 400 + Math.random() * 200
  const startZ = -800 - Math.random() * 400

  meteorHead.position.set(startX, startY, startZ)

  // 对角线方向（左下倾斜）
  const angle = -Math.PI / 4 + (Math.random() - 0.5) * 0.2 // -45° 左右

  meteorHead.userData = {
    speed: 6 + Math.random() * 3,
    life: 0,
    maxLife: 80,
    active: true,
    angle: angle,
    head: head,
    glow: glow,
    tail: tail,
  }

  meteors.push(meteorHead)
  scene.add(meteorHead)
}

// 更新流星
const updateMeteors = () => {
  meteors = meteors.filter((meteor) => {
    if (!meteor.userData.active) {
      scene.remove(meteor)
      // 清理资源
      meteor.children.forEach((child) => {
        if (child.geometry) child.geometry.dispose()
        if (child.material) child.material.dispose()
      })
      return false
    }

    meteor.userData.life += 1
    const progress = meteor.userData.life / meteor.userData.maxLife

    // 流星沿对角线移动（左下方向）
    const speed = meteor.userData.speed
    const angle = meteor.userData.angle
    meteor.position.x += Math.cos(angle) * speed
    meteor.position.y += Math.sin(angle) * speed
    meteor.position.z += speed * 0.3

    // 头部渐隐效果
    const fadeStart = 0.7
    if (progress > fadeStart) {
      const fadeProgress = (progress - fadeStart) / (1 - fadeStart)
      meteor.userData.head.material.opacity = 1 - fadeProgress
      meteor.userData.glow.material.opacity = 0.4 * (1 - fadeProgress)
    }

    // 光尾渐隐
    meteor.userData.tail.material.opacity = Math.max(0, 0.8 - progress * 0.5)

    // 脉动效果（光晕轻微缩放）
    const pulse = 1 + Math.sin(meteor.userData.life * 0.3) * 0.1
    meteor.userData.glow.scale.set(pulse, pulse, pulse)

    // 流星消失条件
    if (
      meteor.userData.life >= meteor.userData.maxLife ||
      meteor.position.y < -600 ||
      meteor.position.x < -800
    ) {
      meteor.userData.active = false
      return false
    }

    return true
  })
}

// 动画循环
const animate = () => {
  animationId = requestAnimationFrame(animate)

  // 更新星星位置（缓慢移动）
  if (stars) {
    const positions = stars.geometry.attributes.position.array
    const velocities = stars.userData.velocities

    for (let i = 0; i < positions.length; i += 3) {
      positions[i + 2] += velocities[i / 3]

      // 重置超出范围的星星
      if (positions[i + 2] > 750) {
        positions[i + 2] = -750
        positions[i] = (Math.random() - 0.5) * 2000
        positions[i + 1] = (Math.random() - 0.5) * 2000
      }
    }

    stars.geometry.attributes.position.needsUpdate = true
  }

  // 更新流星
  updateMeteors()

  // 渲染场景
  renderer.render(scene, camera)
}

// 随机生成流星
const startMeteorTimer = () => {
  const createRandomMeteor = () => {
    createMeteor()
    // 5-10 秒随机间隔
    const nextDelay = 5000 + Math.random() * 5000
    meteorTimer = setTimeout(createRandomMeteor, nextDelay)
  }

  // 首次延迟 2 秒
  meteorTimer = setTimeout(createRandomMeteor, 2000)
}

// 响应式窗口大小调整
const handleResize = () => {
  if (!canvasContainer.value || !renderer) return

  const width = canvasContainer.value.clientWidth
  const height = canvasContainer.value.clientHeight

  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
}

onMounted(() => {
  initScene()
  createStars()
  animate()
  startMeteorTimer()

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  if (meteorTimer) {
    clearTimeout(meteorTimer)
  }
  window.removeEventListener('resize', handleResize)

  // 清理资源
  if (stars) {
    stars.geometry.dispose()
    stars.material.dispose()
  }
  meteors.forEach((meteor) => {
    scene.remove(meteor)
    meteor.children.forEach((child) => {
      if (child.geometry) child.geometry.dispose()
      if (child.material) child.material.dispose()
    })
  })

  if (renderer) {
    renderer.dispose()
    if (canvasContainer.value && renderer.domElement) {
      canvasContainer.value.removeChild(renderer.domElement)
    }
  }
})
</script>

<style scoped>
.starry-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
  background: transparent;
}

.starry-background canvas {
  width: 100%;
  height: 100%;
  pointer-events: none;
}
</style>
