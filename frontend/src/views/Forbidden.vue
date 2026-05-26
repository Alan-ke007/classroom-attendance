<template>
  <div class="page-403">
    <div class="content">
      <div class="code">403</div>
      <div class="title">权限不足</div>
      <div class="desc">抱歉，您没有权限访问该页面</div>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.page-403 {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--c-bg);
}
.content { text-align: center; }
.code { font-size: 120px; font-weight: 700; color: var(--c-warning); line-height: 1; }
.title { font-size: 24px; color: var(--c-text); margin: 16px 0 8px; }
.desc { font-size: 14px; color: var(--c-text-tertiary); margin-bottom: 24px; }
</style>
