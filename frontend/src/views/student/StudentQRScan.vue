<template>
  <div class="qr-scan-page">
    <el-card>
      <template #header><span>扫码签到</span></template>
      <div class="scan-area">
        <div class="scan-hint">
          <div class="scan-icon-wrap">
            <svg viewBox="0 0 64 64" width="64" height="64" fill="none">
              <rect x="4" y="4" width="24" height="24" rx="4" stroke="#007AFF" stroke-width="2.5"/>
              <rect x="36" y="4" width="24" height="24" rx="4" stroke="#007AFF" stroke-width="2.5"/>
              <rect x="4" y="36" width="24" height="24" rx="4" stroke="#007AFF" stroke-width="2.5"/>
              <rect x="36" y="36" width="24" height="24" rx="4" stroke="#007AFF" stroke-width="2.5"/>
              <line x1="32" y1="8" x2="32" y2="24" stroke="#007AFF" stroke-width="2" stroke-dasharray="4 4"/>
            </svg>
          </div>
          <h3>请使用微信小程序扫描教师展示的二维码</h3>
          <p class="scan-sub">打开智课考勤小程序 → 扫码签到 → 扫描二维码</p>
          <el-divider />
          <p class="manual-hint">手机扫码后，在此输入签到信息</p>
          <el-input
            v-model="studentNo"
            placeholder="学号（如 202101001）"
            class="code-input"
            style="margin-bottom: 10px;"
            clearable
          />
          <el-input
            v-model="manualCode"
            placeholder="扫码得到的签到码"
            class="code-input"
            clearable
            @keyup.enter="submitManualCode"
          >
            <template #append>
              <el-button type="primary" @click="submitManualCode" :loading="submitting">提交</el-button>
            </template>
          </el-input>
          <div v-if="result" class="result-msg" :class="result.type">
            {{ result.message }}
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const manualCode = ref('')
const studentNo = ref('')
const submitting = ref(false)
const result = ref(null)

async function submitManualCode() {
  if (!studentNo.value.trim()) { ElMessage.warning('请输入学号'); return }
  if (!manualCode.value.trim()) { ElMessage.warning('请输入签到码'); return }
  submitting.value = true
  result.value = null
  try {
    const res = await request.post('/qrcode/checkin', {
      token: manualCode.value.trim(),
      studentNo: studentNo.value.trim()
    })
    result.value = { type: 'success', message: res.message || '签到成功！' }
  } catch (e) {
    result.value = { type: 'error', message: e.message || '签到失败，请检查学号和签到码是否正确' }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.qr-scan-page { max-width: 600px; margin: 0 auto; }
.scan-area { text-align: center; padding: 40px 0; }
.scan-icon-wrap { margin-bottom: 20px; }
.scan-hint h3 { font-size: 17px; font-weight: 600; color: #303133; margin: 0 0 8px; }
.scan-sub { font-size: 14px; color: #909399; margin: 0 0 12px; }
.manual-hint { font-size: 13px; color: #909399; margin-bottom: 10px; }
.code-input { width: 320px; display: inline-block; }
.result-msg { margin-top: 16px; font-size: 15px; font-weight: 500; }
.result-msg.success { color: #67c23a; }
.result-msg.error { color: #f56c6c; }
</style>
