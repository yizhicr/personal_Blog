<template>
  <div class="test-container">
    <h1>前后端通信测试</h1>
    
    <div class="test-section">
      <h2>1. 测试后端API连通性</h2>
      <button @click="testBackendConnection" :disabled="loading.connection" class="test-btn">
        {{ loading.connection ? '测试中...' : '测试连接' }}
      </button>
      <div v-if="connectionResult" class="result">
        <h3>结果:</h3>
        <pre>{{ connectionResult }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h2>2. JWT认证测试</h2>
      <div class="form-group">
        <input v-model="testUser.username" placeholder="用户名" class="form-input" />
        <input v-model="testUser.email" placeholder="邮箱" class="form-input" />
        <input v-model="testUser.password" type="password" placeholder="密码" class="form-input" />
      </div>
      <button @click="testRegister" :disabled="loading.register" class="test-btn">
        {{ loading.register ? '注册中...' : '测试注册' }}
      </button>
      <div class="form-group" style="margin-top: 1rem;">
        <input v-model="loginCredentials.username" placeholder="用户名" class="form-input" />
        <input v-model="loginCredentials.password" type="password" placeholder="密码" class="form-input" />
      </div>
      <button @click="testLogin" :disabled="loading.login" class="test-btn">
        {{ loading.login ? '登录中...' : '测试登录' }}
      </button>
      <button @click="testValidateJWT" :disabled="loading.validate" class="test-btn" style="margin-left: 0.5rem;">
        {{ loading.validate ? '验证中...' : '验证JWT' }}
      </button>
      <div v-if="authResult" class="result">
        <h3>认证结果:</h3>
        <pre>{{ authResult }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h2>3. 文章操作测试</h2>
      <div class="form-group">
        <input v-model="articleData.title" placeholder="文章标题" class="form-input" />
        <textarea v-model="articleData.content" placeholder="文章内容" class="form-input" style="height: 100px;"></textarea>
        <input v-model="articleData.summary" placeholder="文章摘要" class="form-input" />
      </div>
      <button @click="testCreateArticle" :disabled="loading.createArticle" class="test-btn">
        {{ loading.createArticle ? '创建中...' : '创建文章' }}
      </button>
      <button @click="testGetArticles" :disabled="loading.getArticles" class="test-btn" style="margin-left: 0.5rem;">
        {{ loading.getArticles ? '获取中...' : '获取文章列表' }}
      </button>
      <div v-if="articleResult" class="result">
        <h3>文章操作结果:</h3>
        <pre>{{ articleResult }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h2>4. 评论与删除测试</h2>
      <div class="form-group">
        <input v-model="commentData.articleId" placeholder="文章ID" class="form-input" />
        <textarea v-model="commentData.content" placeholder="评论内容" class="form-input" style="height: 60px;"></textarea>
      </div>
      <button @click="testCreateComment" :disabled="loading.createComment" class="test-btn">
        {{ loading.createComment ? '评论中...' : '添加评论' }}
      </button>
      <button @click="testDeleteArticle" :disabled="loading.deleteArticle" class="test-btn" style="margin-left: 0.5rem;">
        {{ loading.deleteArticle ? '删除中...' : '删除文章' }}
      </button>
      <div v-if="commentResult" class="result">
        <h3>评论与删除结果:</h3>
        <pre>{{ commentResult }}</pre>
      </div>
    </div>

    <!-- 获取评论列表 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span style="color: #409EFF;">获取评论列表</span>
        </div>
      </template>
      <el-form :model="commentListData" label-width="120px">
        <el-form-item label="文章ID">
          <el-input v-model.number="commentListData.articleId" placeholder="请输入文章ID"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            @click="testGetComments" 
            :loading="loadingList"
            :disabled="!commentListData.articleId || !checkToken()"
          >
            {{ loadingList ? '加载中...' : '获取评论列表' }}
          </el-button>
        </el-form-item>
        <el-form-item label="结果">
          <el-input
            v-if="commentListResult"
            type="textarea"
            :rows="6"
            placeholder="评论列表结果"
            v-model="commentListResult"
            readonly
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 删除评论功能 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span style="color: #409EFF;">删除评论测试</span>
        </div>
      </template>
      <el-form :model="deleteCommentData" label-width="120px">
        <el-form-item label="评论ID">
          <el-input v-model.number="deleteCommentData.commentId" placeholder="请输入评论ID"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="danger" 
            @click="testDeleteComment" 
            :loading="loadingDelete"
            :disabled="(deleteCommentData.commentId === '' || deleteCommentData.commentId === null || deleteCommentData.commentId === undefined) || !checkToken()">
              删除评论
            </el-button>
        </el-form-item>
        <el-form-item label="结果">
          <el-input
            v-if="deleteCommentResult"
            type="textarea"
            :rows="4"
            placeholder="删除评论结果"
            v-model="deleteCommentResult"
            readonly
          />
        </el-form-item>
      </el-form>
    </el-card>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import request from '@/utils/request' // 使用封装的请求实例
import { ElMessage } from 'element-plus'
import axios from 'axios'; // 导入axios

// 测试状态
const loading = ref({
  connection: false,
  register: false,
  login: false,
  validate: false,
  createArticle: false,
  getArticles: false,
  createComment: false,
  deleteArticle: false
})

// 测试结果
const connectionResult = ref('')
const authResult = ref('')
const articleResult = ref('')
const commentResult = ref('')

// 测试用户数据
const testUser = ref({
  username: 'testuser',
  email: 'test@example.com',
  password: 'password123'
})

// 登录凭据
const loginCredentials = ref({
  username: 'testuser',
  password: 'password123'
})

// 文章数据
const articleData = ref({
  title: '测试文章标题',
  content: '这是测试文章的内容',
  summary: '测试摘要'
})

// 评论数据
const commentData = ref({
  articleId: '',
  content: 'This is a comment'
})

// 获取评论列表相关
const commentListData = ref({
  articleId: ''
})
const commentListResult = ref('')
const loadingList = ref(false)

// 删除评论相关
const deleteCommentData = ref({
  commentId: ''
})
const deleteCommentResult = ref('')
const loadingDelete = ref(false)

// 测试后端连通性
const testBackendConnection = async () => {
  loading.value.connection = true
  try {
    // 直接使用axios，不通过封装的request，因为这个API不需要认证
    const axios = await import('axios');
    const response = await axios.default.get('http://localhost:8080/')
    connectionResult.value = response.data
    ElMessage.success('后端连接测试成功')
  } catch (error: any) {
    connectionResult.value = error.message || '连接失败'
    ElMessage.error('后端连接测试失败')
  } finally {
    loading.value.connection = false
  }
}

// 测试用户注册
const testRegister = async () => {
  loading.value.register = true
  try {
    const response = await request.post('/auth/register', {
      username: testUser.value.username,
      email: testUser.value.email,
      password: testUser.value.password
    })
    authResult.value = JSON.stringify(response, null, 2)
    ElMessage.success('注册测试成功')
  } catch (error: any) {
    if (error.response) {
      authResult.value = JSON.stringify(error.response.data, null, 2)
    } else {
      authResult.value = error.message || '注册失败'
    }
    ElMessage.error('注册测试失败')
  } finally {
    loading.value.register = false
  }
}

// 测试用户登录
const testLogin = async () => {
  loading.value.login = true
  try {
    const response = await axios.post('http://localhost:8080/api/auth/login', {
      username: loginCredentials.value.username,
      password: loginCredentials.value.password
    })
    
    // 保存token到localStorage
    if (response.data.data && response.data.data.token) {
      localStorage.setItem('token', response.data.data.token)
      ElMessage.success('登录成功，JWT已保存到localStorage')
    }
    
    authResult.value = JSON.stringify(response.data, null, 2)
  } catch (error: any) {
    if (error.response) {
      authResult.value = JSON.stringify(error.response.data, null, 2)
    } else {
      authResult.value = error.message || '登录失败'
    }
    ElMessage.error('登录测试失败')
  } finally {
    loading.value.login = false
  }
}

// 测试JWT验证
const testValidateJWT = async () => {
  loading.value.validate = true
  try {
    const response = await request.get('/auth/validate')
    authResult.value = JSON.stringify(response, null, 2)
    ElMessage.success('JWT验证成功')
  } catch (error: any) {
    if (error.response) {
      authResult.value = JSON.stringify(error.response.data, null, 2)
    } else {
      authResult.value = error.message || 'JWT验证失败'
    }
    ElMessage.error('JWT验证失败')
  } finally {
    loading.value.validate = false
  }
}

// 测试创建文章
const testCreateArticle = async () => {
  loading.value.createArticle = true
  try {
    const response = await request.post('/articles', {
      title: articleData.value.title,
      content: articleData.value.content,
      summary: articleData.value.summary
    })
    
    articleResult.value = JSON.stringify(response, null, 2)
    ElMessage.success('文章创建成功')
  } catch (error: any) {
    if (error.response) {
      articleResult.value = JSON.stringify(error.response.data, null, 2)
    } else {
      articleResult.value = error.message || '创建文章失败'
    }
    ElMessage.error('创建文章失败')
  } finally {
    loading.value.createArticle = false
  }
}

// 测试获取文章列表
const testGetArticles = async () => {
  loading.value.getArticles = true
  try {
    const response = await request.get('/articles')
    articleResult.value = JSON.stringify(response, null, 2)
    ElMessage.success('获取文章列表成功')
    
    // 如果获取到了文章列表，尝试获取第一篇文章的ID用于评论和删除测试
    if (response && response.data && Array.isArray(response.data) && response.data.length > 0) {
      const firstArticle = response.data[0]
      commentData.value.articleId = String(firstArticle.id)
    }
  } catch (error: any) {
    if (error.response) {
      articleResult.value = JSON.stringify(error.response.data, null, 2)
    } else {
      articleResult.value = error.message || '获取文章列表失败'
    }
    ElMessage.error('获取文章列表失败')
  } finally {
    loading.value.getArticles = false
  }
}

// 测试创建评论
const testCreateComment = async () => {
  loading.value.createComment = true
  try {
    if (!commentData.value.articleId) {
      commentResult.value = 'Please get article ID or enter manually'
      ElMessage.warning('Please get article ID first')
      return
    }
    
    // 先验证JWT是否存在
    const token = localStorage.getItem('token')
    if (!token) {
      commentResult.value = 'JWT token not found, please log in first'
      ElMessage.warning('Please log in first to add comment')
      return
    }
    
    // 验证文章ID是否存在
    try {
      console.log('Validating article ID:', parseInt(commentData.value.articleId))
      const articleResponse = await request.get(`/articles/${commentData.value.articleId}`)
      console.log('Article validation successful:', articleResponse.data)
    } catch (articleError: any) {
      commentResult.value = `Article validation failed: ${articleError.message || 'Article does not exist or cannot be accessed'}`
      ElMessage.error('Article validation failed, please check if the article ID is correct')
      return
    }
    
    console.log('Sending comment request with token:', token.substring(0, 20) + '...')
    console.log('Article ID:', parseInt(commentData.value.articleId))
    console.log('Content:', commentData.value.content)
    
    const response = await request.post('/comments', {
      articleId: parseInt(commentData.value.articleId),
      content: commentData.value.content,
      parentId: null  // 可选字段，主评论不需要父评论ID
    })
    
    commentResult.value = JSON.stringify(response, null, 2)
    ElMessage.success('Comment added successfully')
  } catch (error: any) {
    console.error('Comment creation failed:', error)
    if (error.response) {
      commentResult.value = `Status Code: ${error.response.status}\n${JSON.stringify(error.response.data, null, 2)}`
      ElMessage.error(`Adding comment failed: ${error.response.status}`)
    } else {
      commentResult.value = error.message || 'Adding comment failed'
      ElMessage.error('Adding comment failed')
    }
  } finally {
    loading.value.createComment = false
  }
}

// 测试删除文章
const testDeleteArticle = async () => {
  loading.value.deleteArticle = true
  try {
    if (!commentData.value.articleId) {
      commentResult.value = 'Please specify article ID to delete'
      ElMessage.warning('Please get or enter article ID first')
      return
    }
    
    const response = await request.delete(`/articles/${commentData.value.articleId}`)
    
    commentResult.value = JSON.stringify(response, null, 2)
    ElMessage.success('Article deleted successfully')
  } catch (error: any) {
    if (error.response) {
      commentResult.value = JSON.stringify(error.response.data, null, 2)
    } else {
      commentResult.value = error.message || 'Delete article failed'
    }
    ElMessage.error('Delete article failed')
  } finally {
    loading.value.deleteArticle = false
  }
}

// 测试获取评论列表
const testGetComments = async () => {
  loadingList.value = true
  try {
    if (commentListData.value.articleId === '' || commentListData.value.articleId === null || commentListData.value.articleId === undefined) {
      commentListResult.value = '请输入文章ID'
      ElMessage.warning('请输入文章ID')
      return
    }
    
    // 先验证JWT是否存在
    const token = localStorage.getItem('token')
    if (!token) {
      commentListResult.value = 'JWT token not found, please log in first'
      ElMessage.warning('请先登录')
      return
    }
    
    console.log('Sending get comments request with token:', token.substring(0, 20) + '...')
    console.log('Article ID:', commentListData.value.articleId)
    
    const response = await request.get(`/comments/article/${commentListData.value.articleId}`)
    
    // 检查响应结构
    console.log('Full response object:', response)
    console.log('Response data property:', response.data)
    console.log('Is response itself the data?', Array.isArray(response))
    
    // 根据实际响应结构来处理数据
    let commentData;
    if (Array.isArray(response)) {
      // 如果响应本身就是数组
      commentData = response;
    } else if (response && response.data !== undefined) {
      // 如果响应包含data属性
      commentData = response.data;
    } else {
      // 其他情况，可能需要进一步检查
      commentData = response;
    }
    
    commentListResult.value = JSON.stringify(commentData, null, 2)
    ElMessage.success('评论列表获取成功')
    
    // 如果响应中包含评论，提取评论ID供删除功能使用
    if (commentData && Array.isArray(commentData) && commentData.length > 0) {
      const commentIds = commentData.map((comment: any) => comment.id).join(', ')
      console.log('Available comment IDs:', commentIds)
      
      // 显示有多少条评论被获取
      console.log(`Total comments received: ${commentData.length}`)
      commentData.forEach((comment: any, index: number) => {
        console.log(`Comment ${index + 1}: ID=${comment.id}, Content="${comment.content}", Author="${comment.authorName}"`)
      })
    } else {
      console.log('No comments received or response is not an array')
    }
  } catch (error: any) {
    console.error('Get comments failed:', error)
    if (error.response) {
      commentListResult.value = `Status Code: ${error.response.status}\n${JSON.stringify(error.response.data, null, 2)}`
      ElMessage.error(`获取评论列表失败: ${error.response.status}`)
    } else {
      commentListResult.value = error.message || '获取评论列表失败'
      ElMessage.error('获取评论列表失败')
    }
  } finally {
    loadingList.value = false
  }
}

// 测试删除评论
const testDeleteComment = async () => {
  // 先强制获取当前值
  const currentCommentId = deleteCommentData.value.commentId;
  
  loadingDelete.value = true
  
  // 添加调试日志来检查值
  console.log('Current deleteCommentData:', deleteCommentData.value)
  console.log('Current deleteCommentData.commentId:', currentCommentId)
  console.log('Type of deleteCommentData.commentId:', typeof currentCommentId)
  
  try {
    if (currentCommentId === '' || currentCommentId === null || currentCommentId === undefined) {
      console.log('Validation failed - commentId is empty')
      deleteCommentResult.value = '请输入评论ID'
      ElMessage.warning('请输入评论ID')
      return
    }
    
    console.log('Validation passed - proceeding with delete')
    
    // 先验证JWT是否存在
    const token = localStorage.getItem('token')
    if (!token) {
      deleteCommentResult.value = 'JWT token not found, please log in first'
      ElMessage.warning('请先登录')
      return
    }
    
    console.log('Sending delete comment request with token:', token.substring(0, 20) + '...')
    console.log('Comment ID:', currentCommentId)
    
    const response = await request.delete(`/comments/${currentCommentId}`)
    
    deleteCommentResult.value = JSON.stringify(response.data, null, 2)
    ElMessage.success('评论删除成功')
    
    // 清空输入框
    deleteCommentData.value.commentId = ''
  } catch (error: any) {
    console.error('Comment deletion failed:', error)
    if (error.response) {
      deleteCommentResult.value = `Status Code: ${error.response.status}\n${JSON.stringify(error.response.data, null, 2)}`
      ElMessage.error(`删除评论失败: ${error.response.status}`)
    } else {
      deleteCommentResult.value = error.message || '删除评论失败'
      ElMessage.error('删除评论失败')
    }
  } finally {
    loadingDelete.value = false
  }
}

// 检查JWT token是否存在
const checkToken = () => {
  return !!localStorage.getItem('token');
}

</script>

<style scoped>
.test-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
  font-family: Arial, sans-serif;
}

.test-section {
  margin-bottom: 3rem;
  padding: 1.5rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.test-btn {
  padding: 0.5rem 1rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  margin-right: 0.5rem;
  margin-bottom: 0.5rem;
}

.test-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-group {
  margin: 1rem 0;
}

.form-input {
  display: block;
  width: 100%;
  max-width: 500px;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

.result {
  margin-top: 1rem;
  padding: 1rem;
  background-color: #f1f1f1;
  border-radius: 4px;
  white-space: pre-wrap;
}

.result h3 {
  margin-top: 0;
}
</style>