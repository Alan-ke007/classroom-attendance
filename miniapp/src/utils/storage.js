export function getToken() {
  try {
    return uni.getStorageSync('token') || ''
  } catch {
    return ''
  }
}

export function setToken(token) {
  uni.setStorageSync('token', token)
}

export function removeToken() {
  uni.removeStorageSync('token')
}

export function getUserInfo() {
  try {
    return uni.getStorageSync('userInfo') || null
  } catch {
    return null
  }
}

export function setUserInfo(info) {
  uni.setStorageSync('userInfo', info)
}

export function removeUserInfo() {
  uni.removeStorageSync('userInfo')
}

export function logout() {
  removeToken()
  removeUserInfo()
  uni.reLaunch({ url: '/pages/login/index' })
}
