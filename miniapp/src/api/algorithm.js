// 算法服务 API (Flask YOLOv8)
const ALGORITHM_BASE_URL = 'http://172.20.72.42:5000'

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
