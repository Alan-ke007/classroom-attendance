// HTTP 请求封装
import { BASE_URL as CONFIG_BASE_URL } from '@/config' // 地址外置，禁止写死内网 IP（见 config.js）

// H5 走本地代理 /api（开发期 vite proxy）；生产可改为真实 https 域名（由 config 注入）
// #ifdef H5
const BASE_URL = '/api'
// #endif
// 小程序端使用外置配置地址：必须为 https 公网域名，环境差异走构建变量，不写死内网 IP
// #ifdef MP-WEIXIN
const BASE_URL = CONFIG_BASE_URL
// #endif

function getToken() {
  try {
    return uni.getStorageSync('token')
  } catch {
    return ''
  }
}

function buildUrl(url, params) {
  if (!params) return url
  const parts = []
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
      parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(params[key]))
    }
  })
  if (parts.length === 0) return url
  return url + (url.indexOf('?') > -1 ? '&' : '?') + parts.join('&')
}

function request(method, url, data, options = {}) {
  const token = getToken()
  const header = { 'Content-Type': 'application/json' }
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }

  const isGet = method === 'GET'
  const fullUrl = BASE_URL + (isGet ? buildUrl(url, data) : url)
  console.log('[Request]', method, fullUrl)

  return new Promise((resolve, reject) => {
    uni.request({
      url: fullUrl,
      method,
      data: isGet ? undefined : data,
      header: { ...header, ...options.header },
      timeout: options.timeout || 15000,
      success: (res) => {
        console.log('[Response]', res.statusCode, JSON.stringify(res.data).substring(0, 100))
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.redirectTo({ url: '/pages/login/index' })
          uni.showToast({ title: '登录已过期', icon: 'none' })
          reject(new Error('Unauthorized'))
          return
        }
        if (res.data.code === 200) {
          resolve(res.data.data)
        } else {
          uni.showToast({ title: res.data.message || '请求失败', icon: 'none' })
          reject(new Error(res.data.message || '请求失败'))
        }
      },
      fail: (err) => {
        console.error('[Request Fail]', fullUrl, JSON.stringify(err))
        uni.showToast({ title: '网络错误: ' + (err.errMsg || '连接失败'), icon: 'none' })
        reject(err)
      }
    })
  })
}

export const get = (url, params) => request('GET', url, params)
export const post = (url, data) => request('POST', url, data)
export const put = (url, data) => request('PUT', url, data)
export const del = (url) => request('DELETE', url)

export default { get, post, put, del }
