import axios from 'axios'
import { ElMessage } from 'element-plus'

// 算法服务基础URL
const ALGORITHM_SERVICE_URL = import.meta.env.VITE_ALGORITHM_BASE_URL || 'http://localhost:5000'

// 创建专门用于算法服务的axios实例
const algorithmRequest = axios.create({
  baseURL: ALGORITHM_SERVICE_URL,
  timeout: 30000 // 算法处理可能需要更长时间
})

// 请求拦截器
algorithmRequest.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('算法服务请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
algorithmRequest.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    let message = '算法服务连接失败'
    
    if (error.code === 'ECONNABORTED') {
      message = '请求超时，算法处理时间过长'
    } else if (error.message.includes('Network Error')) {
      message = '无法连接到算法服务，请确保Flask服务已启动（端口5000）'
    } else if (error.response) {
      message = error.response.data?.message || `算法服务错误: ${error.response.status}`
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

/**
 * 行为检测
 * @param {string} image - base64编码的图片数据
 */
export function detectBehavior(image) {
  return algorithmRequest({
    url: '/api/behavior/detect',
    method: 'post',
    data: {
      image
    }
  })
}

/**
 * 健康检查
 */
export function checkAlgorithmHealth() {
  return algorithmRequest({
    url: '/health',
    method: 'get'
  })
}

/**
 * 上传模型文件
 * @param {FormData} formData - 包含模型文件的FormData对象
 */
export function uploadModel(formData) {
  return algorithmRequest({
    url: '/api/model/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

