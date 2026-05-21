import { post, get } from './request'

export function login(username, password) {
  return post('/auth/login', { username, password })
}

export function register(data) {
  return post('/auth/register', data)
}

export function getUserInfo() {
  return get('/auth/info')
}

export function getCaptcha() {
  return get('/captcha/generate')
}
