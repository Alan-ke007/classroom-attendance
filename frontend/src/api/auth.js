import request from '@/utils/request'

// 登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

// 忘记密码 — 验证身份
export function forgotPassword(data) {
  return request({
    url: '/auth/forgot-password',
    method: 'post',
    data
  })
}

// 忘记密码 — 重置密码
export function resetPassword(data) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data
  })
}

// 获取验证码
export function getCaptcha() {
  return request({
    url: '/captcha/generate',
    method: 'get'
  })
}
