// 人脸相关接口（P1）
// 全部走后端 BASE_URL；request.js 自动带 Authorization: Bearer <JWT>。
// 小程序不直连算法服务、不持有算法服务密钥（NF1）。
import { post } from '@/api/request'

/**
 * 人脸建档：上传 1~3 张正脸照（base64 数组），由后端提取特征并落库（覆盖式 upsert）
 * @param {string[]} images - 1~3 张 base64 图片
 * @returns {Promise<{studentId:number, enrolledAt:string, faceCount:number, source:string, modelVersion:string}>}
 */
export function enrollFace(images) {
  return post('/face/enroll', { images })
}

/**
 * 签到核验（推荐主链路合并端点）：图片随签到一并上传后端，
 * 后端完成「提取+比对+写签到」，按 faceStatus 明确降级/拒绝。
 * @param {{courseId:string|number, image:string}} payload
 * @returns {Promise<{faceStatus:'VERIFIED'|'NEED_REVIEW'|'REJECTED', confidence:number, status:string, checkInTime:string, message:string}>}
 */
export function faceCheckin({ courseId, image }) {
  return post('/attendance/face-checkin', { courseId, image })
}

/**
 * 单独核验（可选/复用/诊断）：由后端代理完成 extract+本地 1:1 比对（不直连算法）
 * @param {string} image - base64 图片
 * @returns {Promise<{matched:boolean, confidence:number, studentId:number, reason:string|null}>}
 */
export function recognizeFace(image) {
  return post('/algorithm/recognize', { image })
}
