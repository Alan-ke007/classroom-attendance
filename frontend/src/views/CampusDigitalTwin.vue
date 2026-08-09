<template>
  <div class="campus-placeholder">
    <el-result icon="warning" title="功能暂不可用" sub-title="数字孪生 3D 场景正在维护中，请稍后再试">
      <template #extra>
        <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
</script>

<style scoped>
.campus-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: calc(100vh - 120px);
  background: var(--c-bg);
}
</style>
