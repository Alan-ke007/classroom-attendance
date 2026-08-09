// 算法服务 API (Flask YOLOv8)
import { ALGORITHM_BASE_URL } from '@/config'
// 注意：当前前端直连算法服务仅用于开发/演示；
// 生产环境应由后端中转鉴权，前端不直连（P1 整改项）。地址必须为 https。

function request(method, url, data) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: ALGORITHM_BASE_URL + url,
      method,
      data,
      header: { 'Content-Type': 'application/json' },
      timeout: 30000,
      success: (res) => {
        resolve(res.data)
      },
      fail: (err) => {
        console.error('算法服务连接失败，请确保Flask服务已启动', err)
        reject(err)
      }
    })
  })
}

/**
 * 人脸识别考勤
 * @param {string} image - base64 编码的图片数据
 */
export function recognizeFace(image) {
  return request('POST', '/api/attendance/recognize', { image })
}

/**
 * 行为检测
 * @param {string} image - base64 编码的图片数据
 */
export function detectBehavior(image) {
  return request('POST', '/api/behavior/detect', { image })
}

/**
 * 健康检查
 */
export function checkAlgorithmHealth() {
  return request('GET', '/health')
}
