import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(getUserInfo() || null)
  const token = ref(uni.getStorageSync('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isTeacher = computed(() => userInfo.value?.role === 'teacher' || userInfo.value?.role === 'admin')
  const isStudent = computed(() => userInfo.value?.role === 'student')
  const realName = computed(() => userInfo.value?.realName || '')
  const role = computed(() => userInfo.value?.role || '')

  function setUser(info) {
    userInfo.value = info
  }

  function setTokenValue(t) {
    token.value = t
  }

  function logout() {
    userInfo.value = null
    token.value = ''
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    uni.reLaunch({ url: '/pages/login/index' })
  }

  return { userInfo, token, isLoggedIn, isTeacher, isStudent, realName, role, setUser, setTokenValue, logout }
})
