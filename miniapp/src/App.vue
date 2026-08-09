<script setup>
import { onLaunch } from '@dcloudio/uni-app'
import { getToken, getUserInfo } from '@/utils/storage'

onLaunch(() => {
  console.log('课堂智能考勤小程序启动')
  const token = getToken()
  if (!token) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
})
</script>

<style>
page {
  background-color: #F5F5F5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #303133;
  font-size: 14px;
}
</style>
