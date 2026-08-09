import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  // ② 安全：携带 httpOnly Cookie（JWT 存于 Cookie，JS 不可读，由浏览器自动随同源请求发送）
  withCredentials: true
})

// 防止重复提示的标志
let isRedirectingToLogin = false

// 请求拦截器
request.interceptors.request.use(
  config => {
    // ② 安全：鉴权改走 httpOnly Cookie，浏览器自动随同源请求携带，前端不再手动注入 Bearer。
    // （保留后端对 Authorization 头的兼容读取，便于非浏览器/API 调用方使用。）
    return config
  },
  error => {
    console.error('请求错误:', error)
    ElMessage.error('请求发送失败')
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // blob 响应直接返回原始 response
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    
    // 根据后端返回的code判断请求是否成功
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // Token失效或用户未登录
      if (res.code === 401) {
        handleTokenExpired()
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    let message = '网络错误'
    
    if (error.response) {
      // 服务器返回了错误状态码
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          handleTokenExpired()
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = `连接错误${error.response.status}`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    } else if (error.message.includes('Network Error')) {
      message = '网络连接异常，请检查网络'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

/**
 * 处理Token过期（后端 401）
 * ② 安全：token 已不在前端可见（httpOnly Cookie），此处只需清除本地 userInfo 软状态并跳转登录。
 * httpOnly Cookie 由后端 /auth/logout 或自然过期失效，前端 JS 无法清除。
 */
function handleTokenExpired() {
  if (isRedirectingToLogin) {
    return
  }
  
  isRedirectingToLogin = true
  
  // 清除本地的 userInfo 软状态（不含 token，token 仅存 httpOnly Cookie）
  localStorage.removeItem('userInfo')
  
  // 显示提示消息
  ElMessage.warning('登录已过期，请重新登录')
  
  // 延迟跳转到登录页
  setTimeout(() => {
    isRedirectingToLogin = false
    router.push('/login').catch(() => {})
  }, 1000)
}

export default request
