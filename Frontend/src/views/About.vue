<template>
  <div class="about-container">
    <div class="about-content">
      <h1>关于我们</h1>
      <p>欢迎来到我们的博客平台！</p>
      
      <section class="app-info">
        <h2>应用信息</h2>
        <p>这是一个现代化的博客系统，旨在为用户提供高质量的内容分享平台。</p>
        <ul>
          <li>支持文章发布与管理</li>
          <li>分类和标签系统</li>
          <li>评论互动功能</li>
          <li>用户友好的界面设计</li>
        </ul>
      </section>
      
      <section class="features">
        <h2>主要特性</h2>
        <p>我们的博客系统具备以下特色功能：</p>
        <ul>
          <li>响应式设计，支持多设备浏览</li>
          <li>用户身份验证与授权</li>
          <li>安全的文章管理机制</li>
          <li>高效的搜索功能</li>
        </ul>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
// 关于页面的逻辑可以在这里添加
</script>

<style scoped>
.about-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
}

.about-content {
  line-height: 1.6;
}

.about-content h1 {
  color: #333;
  text-align: center;
  margin-bottom: 1.5rem;
}

.about-content h2 {
  color: #555;
  margin-top: 1.5rem;
  margin-bottom: 0.5rem;
}

.features, .app-info {
  margin-top: 2rem;
}

.features ul, .app-info ul {
  padding-left: 1.5rem;
}

.features li, .app-info li {
  margin-bottom: 0.5rem;
}
</style>