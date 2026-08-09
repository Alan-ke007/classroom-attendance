import axios from 'axios'
import { ElMessage } from 'element-plus'

// ③ 安全：算法请求不再直连 Flask 服务，统一经后端 /api/algorithm 代理转发。
// 算法服务的地址与密钥仅存于后端（AlgorithmClient），浏览器/小程序永不可见（NF1）。
// 仍使用独立 axios 实例（与统一 request 不同），因为算法服务返回原始 JSON（非 Result 包裹），
// 不进统一 code 拦截器；仅携带 Cookie（withCredentials）以通过后端鉴权。
const ALGORITHM_PROXY_BASE = (import.meta.env.VITE_API_BASE_URL || '/api') + '/algorithm'

const algorithmRequest = axios.create({
  baseURL: ALGORITHM_PROXY_BASE,
  timeout: 30000, // 算法处理可能需要更长时间
  withCredentials: true
})

// 请求拦截器
algorithmRequest.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('算法代理请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器（透传算法原始 JSON）
algorithmRequest.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    let message = '算法服务连接失败'

    if (error.code === 'ECONNABORTED') {
      message = '请求超时，算法处理时间过长'
    } else if (error.message.includes('Network Error')) {
      message = '无法连接到算法代理（后端 /api/algorithm），请确保后端已启动且算法服务可达'
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
    url: '/detect',
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
    url: '/model-upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
