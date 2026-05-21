import { get, post } from './request'

export function generateQRCode(courseId) {
  return get('/qrcode/generate', { courseId })
}

export function checkinByQRCode(token) {
  return post('/qrcode/checkin', { token })
}
