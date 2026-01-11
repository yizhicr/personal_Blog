<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="logo">
            <h1>{{ appTitle }}</h1>
          </div>
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="user-name">
                <el-avatar :size="32" :src="userAvatar" />
                <span style="margin-left: 8px">{{ user?.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <el-aside width="200px">
          <el-menu
            default-active="1"
            class="sidebar-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="dashboard">
              <el-icon><House /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-sub-menu index="article">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>文章管理</span>
              </template>
              <el-menu-item index="article-list">文章列表</el-menu-item>
              <el-menu-item index="article-create">写文章</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="category">
              <el-icon><Folder /></el-icon>
              <span>分类管理</span>
            </el-menu-item>
            <el-menu-item index="tag">
              <el-icon><PriceTag /></el-icon>
              <span>标签管理</span>
            </el-menu-item>
            <el-menu-item index="comment">
              <el-icon><ChatDotRound /></el-icon>
              <span>评论管理</span>
            </el-menu-item>
            <el-menu-item index="file">
              <el-icon><FolderOpened /></el-icon>
              <span>文件管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main>
          <div class="main-content">
            <h2>欢迎使用博客管理系统</h2>
            <p>请从左侧菜单选择功能</p>
            <el-row :gutter="20" class="statistics">
              <el-col :span="6">
                <el-statistic title="文章总数" :value="120" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="评论总数" :value="560" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="用户总数" :value="45" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="今日访问" :value="1200" />
              </el-col>
            </el-row>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  House,
  Document,
  Folder,
  PriceTag,
  ChatDotRound,
  FolderOpened
} from '@element-plus/icons-vue'

const router = useRouter()

// 用户信息
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

// 应用标题
const appTitle = computed(() => import.meta.env.VITE_APP_TITLE || '博客系统')

// 用户头像
const userAvatar = computed(() => user.value.avatar || '')

// 菜单选择处理
const handleMenuSelect = (index: string) => {
  const routeMap: Record<string, string> = {
    'dashboard': '/',
    'article-list': '/articles',
    'article-create': '/articles/create',
    'category': '/categories',
    'tag': '/tags',
    'comment': '/comments',
    'file': '/files'
  }
  
  if (routeMap[index]) {
    router.push(routeMap[index])
  }
}

// 用户操作处理
const handleCommand = (command: string) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {
    // 用户取消
  })
}

// 页面加载时检查登录状态
onMounted(() => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
  }
})
</script>

<style scoped>
.home {
  height: 100vh;
}

.el-header {
  background-color: #001529;
  color: white;
  line-height: 60px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.logo h1 {
  margin: 0;
  font-size: 20px;
  color: white;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-name {
  display: flex;
  align-items: center;
  color: white;
}

.el-aside {
  background-color: #f5f5f5;
  border-right: 1px solid #e8e8e8;
}

.sidebar-menu {
  border-right: none;
}

.el-main {
  padding: 20px;
}

.main-content {
  padding: 20px;
  background: white;
  border-radius: 4px;
  min-height: 500px;
}

.main-content h2 {
  margin-top: 0;
  color: #333;
}

.statistics {
  margin-top: 40px;
}
</style>