<template>
  <div class="page-404">
    <div class="content">
      <div class="code">404</div>
      <div class="title">页面不存在</div>
      <div class="desc">抱歉，您访问的页面不存在或已被移除</div>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.page-404 {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f7fa;
}
.content { text-align: center; }
.code { font-size: 120px; font-weight: 700; color: #409EFF; line-height: 1; }
.title { font-size: 24px; color: #303133; margin: 16px 0 8px; }
.desc { font-size: 14px; color: #909399; margin-bottom: 24px; }
</style>
