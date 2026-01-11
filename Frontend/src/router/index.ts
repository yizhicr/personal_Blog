import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: true } // 需要登录才能访问
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { guestOnly: true } // 只有未登录用户才能访问
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/Register.vue'),
      meta: { guestOnly: true }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/About.vue')
    },
    {
      path: '/test',
      name: 'test',
      component: () => import('@/views/CommunicationTest.vue')
    }
  ]
})

// 路由守卫 - 检查用户是否登录
router.beforeEach((to, from, next) => {
  // 检查路由是否需要认证
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const guestOnly = to.matched.some(record => record.meta.guestOnly)
  
  // 检查用户是否已登录
  const isAuthenticated = !!localStorage.getItem('token')
  
  if (requiresAuth && !isAuthenticated) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if (guestOnly && isAuthenticated) {
    // 已经登录但访问登录/注册页，跳转到首页
    next('/')
  } else {
    // 正常访问
    next()
  }
})

export default router