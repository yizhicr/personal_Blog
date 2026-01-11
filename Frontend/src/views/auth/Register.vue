<template>
  <div class="register-container">
    <form @submit.prevent="handleRegister" class="register-form">
      <h2>注册账号</h2>
      
      <div class="form-group">
        <label for="username">用户名:</label>
        <input 
          id="username" 
          type="text" 
          v-model="username" 
          required 
          class="form-input"
        />
      </div>
      
      <div class="form-group">
        <label for="email">邮箱:</label>
        <input 
          id="email" 
          type="email" 
          v-model="email" 
          required 
          class="form-input"
        />
      </div>
      
      <div class="form-group">
        <label for="password">密码:</label>
        <input 
          id="password" 
          type="password" 
          v-model="password" 
          required 
          class="form-input"
        />
      </div>
      
      <button type="submit" :disabled="loading" class="submit-btn">
        {{ loading ? '注册中...' : '注册' }}
      </button>
    </form>
    
    <p class="auth-link">
      已有账户? <router-link to="/login">立即登录</router-link>
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref('')
const email = ref('')
const password = ref('')
const loading = ref(false)

const handleRegister = async () => {
  if (!username.value || !email.value || !password.value) {
    alert('请填写所有字段')
    return
  }

  loading.value = true
  
  try {
    // 这里应该调用实际的后端API
    const response = await axios.post('/api/auth/register', {
      username: username.value,
      email: email.value,
      password: password.value
    })
    
    // 注册成功后跳转到登录页面
    alert('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
    alert('注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  max-width: 400px;
  margin: 2rem auto;
  padding: 2rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #fff;
}

.form-group {
  margin-bottom: 1rem;
}

.form-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

.submit-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-link {
  text-align: center;
  margin-top: 1rem;
}
</style>