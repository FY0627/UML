import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Dashboard from '../views/Dashboard.vue'
import Vocabulary from '../views/Vocabulary.vue'
import History from '../views/History.vue'
import Profile from '../views/Profile.vue'
import AdminCorrection from '../views/AdminCorrection.vue'
import MessageCenter from '../views/MessageCenter.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { 
    path: '/dashboard', 
    name: 'Dashboard', 
    component: Dashboard,
    meta: { requiresAuth: true, role: 'USER' }
  },
  { 
    path: '/vocabulary', 
    name: 'Vocabulary', 
    component: Vocabulary,
    meta: { requiresAuth: true, role: 'USER' }
  },
  { 
    path: '/history', 
    name: 'History', 
    component: History,
    meta: { requiresAuth: true, role: 'USER' }
  },
  { 
    path: '/profile', 
    name: 'Profile', 
    component: Profile,
    meta: { requiresAuth: true }
  },
  { 
    path: '/message', 
    name: 'MessageCenter', 
    component: MessageCenter,
    meta: { requiresAuth: true, role: 'USER' }
  },
  { 
    path: '/admin/correction', 
    name: 'AdminCorrection', 
    component: AdminCorrection,
    meta: { requiresAuth: true, role: 'ADMIN' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：拦截非法访问
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('lingoflow_token')
  const userRole = localStorage.getItem('lingoflow_user_role')

  // 1. 如果页面需要登录
  if (to.meta.requiresAuth) {
    if (!token) {
      alert('请先登录！')
      return next('/login')
    }

    // 2. 如果页面有角色限制
    if (to.meta.role && to.meta.role !== userRole) {
      alert('你没有权限访问此页面！')
      // 根据角色回退到对应的首页
      return next(userRole === 'ADMIN' ? '/admin/correction' : '/dashboard')
    }
  }

  // 3. 已登录状态下防止回跳登录页
  if (token && (to.path === '/login' || to.path === '/register')) {
    return next(userRole === 'ADMIN' ? '/admin/correction' : '/dashboard')
  }

  next()
})

export default router