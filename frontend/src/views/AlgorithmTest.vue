<template>
  <div class="algorithm-test">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>算法服务测试</span>
          <el-button type="primary" @click="checkHealth">
            <el-icon><Refresh /></el-icon>
            刷新状态
          </el-button>
        </div>
      </template>
      
      <el-alert
        title="测试说明"
        type="info"
        description="此页面用于测试前端与Flask算法服务的连接是否正常。确保Flask服务已在端口5000启动。"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>服务状态</span>
            </template>
            
            <div class="status-info">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="算法服务">
                  <el-tag :type="healthStatus === 'online' ? 'success' : 'danger'">
                    {{ healthStatus === 'online' ? '✓ 在线' : '✗ 离线' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="行为检测模型">
                  <el-tag :type="behaviorModelLoaded ? 'success' : 'warning'">
                    {{ behaviorModelLoaded ? '✓ 已加载' : '⚠ 未加载' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="服务地址">
                  http://localhost:5000
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>功能测试</span>
            </template>
            
            <div class="test-section">
              <h4>图片文件测试</h4>
              <el-upload
                class="upload-demo"
                drag
                :auto-upload="false"
                :on-change="handleFileChange"
                accept="image/*"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  拖拽图片到此处或 <em>点击上传</em>
                </div>
              </el-upload>
              
              <div v-if="selectedImage" class="image-preview">
                <img :src="selectedImage" alt="预览" style="max-width: 100%; margin-top: 10px; border-radius: 8px;" />
              </div>
              
              <div class="test-buttons" style="margin-top: 15px;">
                <el-button
                  type="success"
                  @click="testBehaviorDetection"
                  :disabled="!selectedImage || testing"
                  :loading="testing"
                >
                  测试行为检测
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-card style="margin-top: 20px;">
        <template #header>
          <span>测试结果</span>
        </template>
        
        <el-empty v-if="!testResult" description="暂无测试结果" />
        
        <div v-else class="result-container">
          <el-alert
            :title="testResult.success ? '测试成功' : '测试失败'"
            :type="testResult.success ? 'success' : 'error'"
            :description="testResult.message"
            show-icon
            :closable="false"
            style="margin-bottom: 15px;"
          />
          
          <pre v-if="testResult.data" class="result-data">{{ JSON.stringify(testResult.data, null, 2) }}</pre>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, UploadFilled } from '@element-plus/icons-vue'
import { checkAlgorithmHealth, detectBehavior } from '@/api/algorithm'

const healthStatus = ref('unknown')
const behaviorModelLoaded = ref(false)
const selectedImage = ref(null)
const selectedImageBase64 = ref(null)
const testing = ref(false)
const testResult = ref(null)

// 检查健康状态
const checkHealth = async () => {
  try {
    const res = await checkAlgorithmHealth()
    if (res.status === 'ok') {
      healthStatus.value = 'online'
      ElMessage.success('算法服务连接正常')
    }
  } catch (error) {
    healthStatus.value = 'offline'
    ElMessage.error('算法服务连接失败')
  }
}

// 处理文件选择
const handleFileChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    selectedImage.value = e.target.result
    // 提取base64部分（去掉data:image/jpeg;base64,前缀）
    selectedImageBase64.value = e.target.result.split(',')[1]
  }
  reader.readAsDataURL(file.raw)
}

// 测试行为检测
const testBehaviorDetection = async () => {
  if (!selectedImageBase64.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  
  testing.value = true
  testResult.value = null
  
  try {
    const res = await detectBehavior(selectedImageBase64.value)
    const behaviorCount = res.data?.behaviors?.length || 0
    testResult.value = {
      success: true,
      message: `检测成功！识别到 ${behaviorCount} 个行为`,
      data: res
    }
    ElMessage.success(testResult.value.message)
  } catch (error) {
    testResult.value = {
      success: false,
      message: error.message || '检测失败',
      data: null
    }
  } finally {
    testing.value = false
  }
}

// 组件加载时检查状态
checkHealth()
</script>

<style scoped>
.algorithm-test {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-info {
  padding: 10px;
}

.test-section {
  padding: 10px;
}

.image-preview {
  margin-top: 15px;
  text-align: center;
}

.test-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.result-container {
  padding: 10px;
}

.result-data {
  background-color: var(--c-bg, #F2F2F7);
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.6;
}
</style>
