<template>
  <el-dialog v-model="visible" title="个人中心" width="480px" :close-on-click-modal="true" @open="initForm" @closed="emit('close')">
    <div class="profile-body">
      <!-- 头像 -->
      <div class="profile-avatar">
        <el-upload
          class="avatar-uploader"
          action="/api/file/upload?category=avatar"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <el-avatar :size="80" :src="form.avatar" />
          <div class="avatar-tip">点击更换头像</div>
        </el-upload>
      </div>

      <!-- 基本信息 -->
      <el-form :model="form" label-width="80px" class="profile-form">
        <el-form-item label="用户名">
          <el-input :model-value="form.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag :type="roleTagType" disable-transitions>{{ roleLabel }}</el-tag>
        </el-form-item>
        <el-divider />
        <el-form-item label="姓名">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>
      </el-form>

      <el-divider>修改密码</el-divider>

      <el-form :model="pwdForm" label-width="80px" class="profile-form">
        <el-form-item label="原密码">
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="至少6位" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="warning" :loading="changingPwd" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { updateProfile, changePassword } from '@/api/user'

const props = defineProps({
  modelValue: Boolean,
  user: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'close', 'profileUpdated'])

const visible = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v)
})

const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + (localStorage.getItem('token') || '')
}))

const saving = ref(false)
const changingPwd = ref(false)

const form = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  avatar: '',
  role: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleLabel = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[form.role] || form.role
})

const roleTagType = computed(() => {
  const map = { admin: 'danger', teacher: 'warning', student: 'success' }
  return map[form.role] || 'info'
})

function initForm() {
  const info = props.user || JSON.parse(localStorage.getItem('userInfo') || '{}')
  form.username = info.username || ''
  form.realName = info.realName || ''
  form.email = info.email || ''
  form.phone = info.phone || ''
  form.avatar = info.avatar || ''
  form.role = info.role || ''
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
}

async function handleSave() {
  saving.value = true
  try {
    const res = await updateProfile({
      realName: form.realName,
      email: form.email,
      phone: form.phone
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      // 更新 localStorage
      const info = { ...(props.user || JSON.parse(localStorage.getItem('userInfo') || '{}')) }
      info.realName = form.realName
      info.email = form.email
      info.phone = form.phone
      if (res.data?.avatar) info.avatar = res.data.avatar
      localStorage.setItem('userInfo', JSON.stringify(info))
      emit('profileUpdated', info)
      visible.value = false
    }
  } catch (e) { /* ignore */ }
  finally { saving.value = false }
}

async function handleChangePassword() {
  if (!pwdForm.oldPassword) { ElMessage.warning('请输入原密码'); return }
  if (pwdForm.newPassword.length < 6) { ElMessage.warning('新密码至少6位'); return }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { ElMessage.warning('两次密码输入不一致'); return }

  changingPwd.value = true
  try {
    const res = await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    }
  } catch (e) { /* ignore */ }
  finally { changingPwd.value = false }
}

function handleAvatarSuccess(res) {
  if (res.code === 200 && res.data) {
    form.avatar = '/api/file/download/' + res.data.id
  }
}

function beforeAvatarUpload(file) {
  if (!file.type.startsWith('image/')) { ElMessage.error('请上传图片文件'); return false }
  return true
}
</script>

<style scoped>
.profile-body { padding: 0 8px; }
.profile-avatar { text-align: center; margin-bottom: 20px; }
.avatar-uploader { cursor: pointer; display: inline-block; }
.avatar-tip { font-size: 12px; color: var(--c-primary); margin-top: 6px; }
.profile-form { margin-top: 8px; }
:deep(.el-divider__text) { font-size: 13px; color: var(--c-text-tertiary); }
:deep(.el-dialog) {
  --el-dialog-bg-color: var(--c-glass-bg);
  border: 1px solid var(--c-glass-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--c-glass-shadow);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
}
:deep(.el-dialog__title) {
  color: var(--c-text);
  font-weight: 600;
}
:deep(.el-dialog__body) {
  color: var(--c-text-secondary);
}
</style>
