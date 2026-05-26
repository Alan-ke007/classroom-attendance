<template>
  <div class="behavior-monitor">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>实时行为监控</span>
          <el-button-group>
            <el-button 
              type="primary" 
              @click="startMonitoring" 
              :disabled="monitoring || algorithmServiceStatus !== 'online'"
            >
              <el-icon><VideoPlay /></el-icon>
              开始监控
            </el-button>
            <el-button type="danger" @click="stopMonitoring" :disabled="!monitoring">
              <el-icon><VideoPause /></el-icon>
              停止监控
            </el-button>
          </el-button-group>
        </div>
      </template>
      
      <el-alert
        title="功能说明"
        type="info"
        description="通过摄像头或上传图片检测学生课堂行为（举手、阅读、书写、使用手机、低头、趴桌）。请确保检测服务已启动在端口5000。"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />
      
      <el-tag
        v-if="algorithmServiceStatus === 'online'"
        type="success"
        style="margin-bottom: 15px;"
      >
        <span class="pulse-dot" style="margin-right: 6px;"></span> 算法服务已连接
      </el-tag>
      <el-tag 
        v-else-if="algorithmServiceStatus === 'offline'" 
        type="danger" 
        style="margin-bottom: 15px;"
      >
        ✗ 算法服务未连接
      </el-tag>
      <el-tag 
        v-else 
        type="info" 
        style="margin-bottom: 15px;"
      >
        ? 正在检查算法服务...
      </el-tag>
      
      <!-- 调试信息 -->
      <el-alert
        v-if="false"
        :title="`调试: monitoring=${monitoring}, status=${algorithmServiceStatus}`"
        type="warning"
        :closable="false"
        style="margin-bottom: 15px;"
      />
      
      <el-row :gutter="20">
        <el-col :span="16">
          <div class="video-container scan-lines">
            <!-- 摄像头视频（始终渲染，通过display控制显示） -->
            <video 
              ref="videoRef" 
              autoplay 
              playsinline 
              :style="{ display: monitoring ? 'block' : 'none', width: '100%', borderRadius: '8px' }"
            ></video>
            
            <!-- 非监控模式：显示上传区域 -->
            <div v-show="!monitoring" class="upload-placeholder">
              <el-upload
                class="behavior-uploader"
                drag
                action="#"
                :auto-upload="false"
                :on-change="handleImageUpload"
                accept="image/*"
              >
                <el-icon class="el-icon--upload" :size="50"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  拖拽图片到此处或 <em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 jpg/png 格式，无摄像头时可上传本地图片进行行为检测
                  </div>
                </template>
              </el-upload>
              
              <!-- 移动端拍照按钮 -->
              <div style="margin-top: 20px; text-align: center;">
                <el-button type="warning" @click="openMobileCamera">
                  <el-icon><Camera /></el-icon>
                  用手机拍摄课堂照片
                </el-button>
                <input 
                  ref="mobileCameraInput" 
                  type="file" 
                  accept="image/*" 
                  capture="environment"
                  style="display: none;"
                  @change="handleMobilePhoto"
                />
              </div>
            </div>
            
            <!-- 图片预览 -->
            <div v-show="!monitoring && uploadedImage" class="image-preview">
              <img :src="uploadedImage" alt="预览" />
            </div>
          </div>
        </el-col>
        
        <el-col :span="8">
          <el-card>
            <template #header>
              <span>实时监控统计</span>
            </template>
            
            <div class="monitor-stats">
              <div class="stat-item">
                <div class="stat-label">在线人数</div>
                <div class="stat-value">{{ monitorStats.online }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">异常行为</div>
                <div class="stat-value" style="color: var(--c-danger);">{{ monitorStats.abnormal }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">监控时长</div>
                <div class="stat-value">{{ monitorDuration }}</div>
              </div>
            </div>
          </el-card>
          
          <el-card style="margin-top: 20px;">
            <template #header>
              <span>最近 detected 行为</span>
            </template>
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in recentBehaviors"
                :key="index"
                :timestamp="activity.time"
                placement="top"
              >
                <el-card>
                  <h4>{{ activity.studentName }} - {{ activity.description || getBehaviorText(activity.behaviorType) }}</h4>
                  <p>置信度: {{ activity.confidence }}%</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onUnmounted, onMounted, nextTick } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { VideoPlay, VideoPause, UploadFilled, Camera } from '@element-plus/icons-vue'
import { detectBehavior, checkAlgorithmHealth } from '@/api/algorithm'
import behaviorAlertWS from '@/utils/websocket'
import request from '@/utils/request'

const videoRef = ref(null)
const canvasRef = ref(null) // 添加canvas引用
const monitoring = ref(false)
const monitorDuration = ref('00:00:00')
const algorithmServiceStatus = ref('unknown')
const detecting = ref(false)
const uploadedImage = ref(null)
const uploadedImageBase64 = ref(null)
const mobileCameraInput = ref(null)
const monitorStats = ref({
  online: 0,
  abnormal: 0
})

const recentBehaviors = ref([])

let stream = null
let timer = null
let detectionTimer = null
let seconds = 0

// 组件加载时检查算法服务状态
console.log('BehaviorMonitor 组件已加载')
checkAlgorithmService()

// WebSocket连接将在组件挂载后建立
let wsConnected = false

async function checkAlgorithmService() {
  try {
    const res = await checkAlgorithmHealth()
    if (res.status === 'ok') {
      algorithmServiceStatus.value = 'online'
      console.log('算法服务状态：在线')
    }
  } catch (error) {
    algorithmServiceStatus.value = 'offline'
    ElMessage.warning('算法服务未连接，请确保Flask服务已启动（端口5000）')
    console.error('算法服务连接失败:', error)
  }
}

const startMonitoring = async () => {
  // 如果有上传的图片，直接检测
  if (uploadedImageBase64.value) {
    await detectBehaviorFromImage(uploadedImageBase64.value)
    return
  }
  
  // 检查算法服务状态
  if (algorithmServiceStatus.value !== 'online') {
    ElMessage.error('算法服务未连接，请先启动Flask服务')
    return
  }
  
  try {
    console.log('开始请求摄像头权限...')
    
    // 检查浏览器支持
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      ElMessage.error('您的浏览器不支持摄像头功能')
      return
    }
    
    // 使用 nextTick 确保 DOM 已更新
    await nextTick()
    
    // 检查 videoRef 是否就绪
    if (!videoRef.value) {
      console.error('videoRef 未就绪')
      ElMessage.error('页面元素未准备好，请刷新页面后重试')
      return
    }
    
    console.log('videoRef 已就绪')
    
    stream = await navigator.mediaDevices.getUserMedia({ 
      video: { 
        width: { ideal: 1920 },
        height: { ideal: 1080 },
        frameRate: { ideal: 30 }
      } 
    })
    
    console.log('摄像头权限已获取')
    
    videoRef.value.srcObject = stream
    
    // 等待视频加载完成
    await new Promise((resolve, reject) => {
      videoRef.value.onloadedmetadata = () => {
        console.log('视频元数据加载完成')
        resolve()
      }
      videoRef.value.onerror = (error) => {
        console.error('视频加载错误:', error)
        reject(error)
      }
    })
    
    console.log('视频尺寸:', videoRef.value.videoWidth, 'x', videoRef.value.videoHeight)
    monitoring.value = true
    
    // 开始计时
    seconds = 0
    timer = setInterval(() => {
      seconds++
      const h = Math.floor(seconds / 3600).toString().padStart(2, '0')
      const m = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0')
      const s = (seconds % 60).toString().padStart(2, '0')
      monitorDuration.value = `${h}:${m}:${s}`
    }, 1000)
    
    // 开始定时检测（每3秒检测一次）
    startPeriodicDetection()
    
    ElMessage.success('监控已启动')
  } catch (error) {
    console.error('摄像头访问失败:', error)
    
    let errorMsg = '无法访问摄像头：'
    
    if (error.name === 'NotAllowedError' || error.name === 'PermissionDeniedError') {
      errorMsg = '权限被拒绝，请在浏览器地址栏允许摄像头访问权限'
    } else if (error.name === 'NotFoundError' || error.name === 'DevicesNotFoundError') {
      errorMsg = '未检测到摄像头设备'
    } else if (error.name === 'NotReadableError' || error.name === 'TrackStartError') {
      errorMsg = '摄像头被占用，请关闭其他使用摄像头的程序'
    } else {
      errorMsg += error.message || '未知错误'
    }
    
    ElMessage.error(errorMsg)
  }
}

const stopMonitoring = () => {
  if (stream) {
    stream.getTracks().forEach(track => track.stop())
    stream = null
    monitoring.value = false
    
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    
    if (detectionTimer) {
      clearInterval(detectionTimer)
      detectionTimer = null
    }
    
    ElMessage.info('监控已停止')
  }
}

// 开始定时检测
const startPeriodicDetection = () => {
  detectionTimer = setInterval(async () => {
    if (!monitoring.value || detecting.value) return
    await captureAndDetect()
  }, 3000) // 每3秒检测一次（给模型更多处理时间）
}

// 捕获帧并检测行为
const captureAndDetect = async () => {
  if (!videoRef.value) return
  
  // 创建临时canvas
  const canvas = document.createElement('canvas')
  const context = canvas.getContext('2d')
  
  canvas.width = videoRef.value.videoWidth
  canvas.height = videoRef.value.videoHeight
  context.drawImage(videoRef.value, 0, 0, canvas.width, canvas.height)
  
  // 转换为base64
  const imageData = canvas.toDataURL('image/jpeg', 0.7).split(',')[1]
  
  await detectBehaviorFromImage(imageData)
}

// 从图片检测行为
const detectBehaviorFromImage = async (imageData) => {
  detecting.value = true
  
  try {
    const res = await detectBehavior(imageData)
    
    if (res.code === 200 && res.data.behaviors.length > 0) {
      // 更新统计数据
      monitorStats.value.online = res.data.totalCount
      
      // 统计异常行为（使用手机、低头、趴桌）
      const abnormalTypes = ['using_phone', 'bowing_head', 'leaning_over']
      const abnormalCount = res.data.behaviors.filter(b => 
        abnormalTypes.includes(b.type)
      ).length
      monitorStats.value.abnormal = abnormalCount
      
      // 添加到最近行为列表（只保留最新的10条）
      res.data.behaviors.forEach((behavior, idx) => {
        recentBehaviors.value.unshift({
          studentName: `未识别学生#${recentBehaviors.value.length + idx + 1}`,
          behaviorType: behavior.type,
          description: behavior.description,
          confidence: Math.round(behavior.confidence * 100),
          time: new Date().toLocaleTimeString()
        })
      })
      
      // 限制列表长度
      if (recentBehaviors.value.length > 10) {
        recentBehaviors.value = recentBehaviors.value.slice(0, 10)
      }
      
      console.log(`检测到 ${res.data.totalCount} 个行为，${abnormalCount} 个异常`)
      ElMessage.success(`检测完成！发现 ${res.data.totalCount} 个行为`)

      // 自动保存检测结果到数据库
      saveDetectionResults(res.data.behaviors)
    } else {
      monitorStats.value.online = 0
      monitorStats.value.abnormal = 0
      ElMessage.info('未检测到行为')
    }
  } catch (error) {
    console.error('行为检测错误:', error)
    ElMessage.error('检测失败：' + (error.message || '未知错误'))
  } finally {
    detecting.value = false
  }
}

// 将 AI 检测结果保存到后端数据库
async function saveDetectionResults(behaviors) {
  if (!behaviors || behaviors.length === 0) return

  try {
    const detections = behaviors.map(b => ({
      behaviorType: b.type,
      confidence: b.confidence
    }))
    await request.post('/behavior/detection/save', detections)
    console.log(`已保存 ${detections.length} 条检测结果到数据库`)
  } catch (error) {
    console.error('保存检测结果失败:', error)
    // 不阻断检测流程，仅打印错误日志
  }
}

// 处理图片上传
const handleImageUpload = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    uploadedImage.value = e.target.result
    uploadedImageBase64.value = e.target.result.split(',')[1]
    ElMessage.success('图片上传成功，点击“开始监控”进行检测')
  }
  reader.readAsDataURL(file.raw)
}

// 打开手机摄像头
const openMobileCamera = () => {
  if (mobileCameraInput.value) {
    mobileCameraInput.value.click()
  }
}

// 处理手机拍照
const handleMobilePhoto = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const reader = new FileReader()
  reader.onload = (e) => {
    uploadedImage.value = e.target.result
    uploadedImageBase64.value = e.target.result.split(',')[1]
    ElMessage.success('照片已拍摄，正在检测行为...')
    // 自动开始检测
    setTimeout(() => {
      startMonitoring()
    }, 500)
  }
  reader.readAsDataURL(file)
  
  // 清空input，允许重复拍摄
  event.target.value = ''
}

const getBehaviorText = (type) => {
  const textMap = {
    raising_hand: '举手',
    reading: '阅读',
    writing: '书写',
    using_phone: '使用手机',
    bowing_head: '低头',
    leaning_over: '趴桌'
  }
  return textMap[type] || type
}

// 连接WebSocket
const connectWebSocket = () => {
  if (wsConnected) {
    console.log('WebSocket已连接，跳过')
    return
  }
  
  // 监听行为预警
  behaviorAlertWS.on('behaviorAlert', (data) => {
    console.log('收到行为预警:', data)
    
    // 显示通知
    ElNotification({
      title: '⚠️ 行为预警',
      message: `${data.message}（置信度：${Math.round(data.confidence * 100)}%）`,
      type: data.level === 'high' ? 'error' : 'warning',
      duration: 5000,
      position: 'top-right'
    })
    
    // 添加到最近行为列表
    recentBehaviors.value.unshift({
      studentName: `学生${data.studentId || '未知'}`,
      behaviorType: data.behaviorType,
      confidence: Math.round(data.confidence * 100),
      time: new Date(data.behaviorTime).toLocaleTimeString()
    })
    
    // 限制列表长度
    if (recentBehaviors.value.length > 10) {
      recentBehaviors.value = recentBehaviors.value.slice(0, 10)
    }
    
    // 更新异常统计
    monitorStats.value.abnormal++
  })
  
  // 监听连接成功
  behaviorAlertWS.on('connected', () => {
    console.log('WebSocket已连接')
    wsConnected = true
  })
  
  // 监听断开连接
  behaviorAlertWS.on('disconnected', () => {
    console.log('WebSocket已断开')
    wsConnected = false
  })
  
  // 监听重连失败
  behaviorAlertWS.on('reconnectFailed', () => {
    ElMessage.error('WebSocket重连失败，请刷新页面')
  })
  
  // 建立连接
  behaviorAlertWS.connect()
}

// 组件挂载后连接WebSocket
onMounted(() => {
  console.log('组件已挂载，连接WebSocket...')
  connectWebSocket()
})

// 组件卸载时清理
onUnmounted(() => {
  stopMonitoring()
  behaviorAlertWS.disconnect()
})
</script>

<style scoped>
/* 页面加载动画 */
.behavior-monitor {
  padding: 20px;
  background: transparent;
  min-height: calc(100vh - 60px);
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  font-size: 16px;
  color: var(--c-text);
}

.video-container {
  position: relative;
  background: var(--c-glass-bg);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  border-radius: 12px;
  overflow: hidden;
  min-height: 500px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.video-container:hover {
  border-color: var(--c-primary);
  box-shadow: 0 4px 20px var(--c-shadow);
  transform: translateY(-2px);
}

.overlay-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: var(--c-text-tertiary);
  font-size: 16px;
  text-align: center;
}

.monitor-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
  padding: 20px 0;
  gap: 15px;
}

.stat-item {
  padding: 20px 15px;
  background: var(--c-glass-bg);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  border-radius: 12px;
  color: var(--c-text-secondary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex: 1;
  animation: scaleIn 0.5s ease-out backwards;
}

.stat-item:nth-child(1) { animation-delay: 0.1s; }
.stat-item:nth-child(2) { animation-delay: 0.2s; }
.stat-item:nth-child(3) { animation-delay: 0.3s; }

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.stat-item:hover {
  border-color: var(--c-primary);
  box-shadow: 0 4px 16px var(--c-shadow);
  transform: translateY(-4px);
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  margin-top: 10px;
  color: var(--c-text);
  transition: all 0.3s ease;
}

.stat-item:hover .stat-value {
  color: var(--c-primary);
  transform: scale(1.05);
}

.stat-label {
  font-size: 13px;
  color: var(--c-text-tertiary);
  transition: all 0.3s ease;
}

.stat-item:hover .stat-label {
  color: var(--c-text-secondary);
}

.upload-placeholder {
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--c-glass-bg);
  backdrop-filter: blur(8px) saturate(180%);
  -webkit-backdrop-filter: blur(8px) saturate(180%);
  border-radius: 8px;
  width: 100%;
  flex-direction: column;
  animation: fadeIn 0.5s ease-out 0.1s both;
}

.behavior-uploader {
  width: 100%;
}

.behavior-uploader :deep(.el-upload-dragger) {
  width: 100%;
  padding: 60px 40px;
  background: var(--c-card);
  border: 2px dashed var(--c-border);
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.behavior-uploader :deep(.el-upload-dragger:hover) {
  border-color: var(--c-primary);
  background: var(--c-primary-bg);
  transform: scale(1.02);
  box-shadow: 0 4px 12px var(--c-shadow);
}

.image-preview {
  margin-top: 20px;
  text-align: center;
  padding: 10px;
  animation: fadeIn 0.5s ease-out 0.2s both;
}

.image-preview img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  border: 1px solid var(--c-border);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.image-preview img:hover {
  border-color: var(--c-primary);
  box-shadow: 0 4px 16px var(--c-shadow);
  transform: scale(1.02);
}

/* 卡片样式优化 - glass morphism */
:deep(.el-card) {
  border-radius: 12px;
  box-shadow: var(--c-glass-shadow);
  border: 1px solid var(--c-glass-border);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--c-glass-bg);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  animation: slideUp 0.6s ease-out backwards;
}

:deep(.el-card:nth-child(1)) { animation-delay: 0.2s; }
:deep(.el-card:nth-child(2)) { animation-delay: 0.3s; }

:deep(.el-card:hover) {
  box-shadow: 0 6px 24px var(--c-shadow);
  border-color: var(--c-primary);
  transform: translateY(-2px);
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--c-border-light);
  padding: 15px 20px;
  font-weight: 500;
  color: var(--c-text);
}

/* 按钮样式 */
:deep(.el-button-group) {
  border-radius: 4px;
  overflow: hidden;
}

:deep(.el-button) {
  border-radius: 4px;
  font-weight: 400;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--c-shadow);
}

:deep(.el-button:active) {
  transform: scale(0.95);
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 4px;
  padding: 4px 12px;
  font-weight: 400;
  transition: all 0.3s ease;
}

:deep(.el-tag:hover) {
  transform: scale(1.05);
}

/* 提示框样式 */
:deep(.el-alert) {
  border-radius: 8px;
  border: 1px solid var(--c-border);
  animation: slideDown 0.5s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 时间线样式 */
:deep(.el-timeline-item) {
  animation: slideInLeft 0.5s ease-out backwards;
}

:deep(.el-timeline-item:nth-child(1)) { animation-delay: 0.1s; }
:deep(.el-timeline-item:nth-child(2)) { animation-delay: 0.15s; }
:deep(.el-timeline-item:nth-child(3)) { animation-delay: 0.2s; }
:deep(.el-timeline-item:nth-child(4)) { animation-delay: 0.25s; }
:deep(.el-timeline-item:nth-child(5)) { animation-delay: 0.3s; }

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

:deep(.el-timeline-item__node) {
  width: 12px;
  height: 12px;
  transition: all 0.3s ease;
}

:deep(.el-timeline-item:hover .el-timeline-item__node) {
  transform: scale(1.3);
  box-shadow: 0 0 0 4px var(--c-primary-bg);
}

:deep(.el-timeline-item__timestamp) {
  font-weight: 400;
  color: var(--c-text-tertiary);
  transition: all 0.3s ease;
}

:deep(.el-timeline-item:hover .el-timeline-item__timestamp) {
  color: var(--c-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .behavior-monitor {
    padding: 10px;
  }

  .video-container {
    min-height: 350px;
  }

  .stat-value {
    font-size: 28px;
  }

  .monitor-stats {
    flex-direction: column;
  }
}
</style>
