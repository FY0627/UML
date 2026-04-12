<template>
  <div class="main-viewport">
    <div class="login-container">
      <AnimatedBrand :isPasswordFocused="isPasswordFocused" />

      <div class="right-panel">
        <div class="form-wrapper">
          <div class="mobile-logo">
            <span class="brand-name-mobile">LingoFlow</span>
          </div>

          <div class="header-text">
            <span class="title">Welcome Back!</span>
            <div class="role-selector-container">
              <span class="role-label">选择登录身份</span>
              <div class="role-selector">
                <div class="role-btn" :class="{ active: selectedRole === 'user' }" @click="selectedRole = 'user'">
                  普通用户
                </div>
                <div class="role-btn" :class="{ active: selectedRole === 'admin' }" @click="selectedRole = 'admin'">
                  管理员
                </div>
                <div class="active-indicator" :class="selectedRole"></div>
              </div>
            </div>
          </div>

          <div class="form-group">
            <span class="label">账号</span>
            <input class="input-field" v-model="formData.username" type="text" placeholder="你的用户名" />
          </div>

          <div class="form-group">
            <div class="label-row">
              <span class="label" style="margin-bottom: 0;">密码</span>
              <span class="forgot-pwd-link" @click="handleForgotPassword">忘记密码？</span>
            </div>
            <div class="password-input-wrapper">
              <input class="input-field pr-10" v-model="formData.password" :type="showPassword ? 'text' : 'password'"
                placeholder="••••••••" @focus="isPasswordFocused = true" @blur="isPasswordFocused = false"
                @keyup.enter="handleLogin" />
              <div class="eye-icon" @click="showPassword = !showPassword">
                <div class="simple-eye-svg" :class="{ showing: showPassword }"></div>
              </div>
            </div>
          </div>

          <button class="submit-btn" :disabled="isLoading" @click="handleLogin">
            {{ isLoading ? '登录中...' : '登 录' }}
          </button>

          <div class="signup-link">
            <span class="text">还没有账号？</span>
            <span class="link" @click="goToRegister">立即注册</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import AnimatedBrand from '../components/AnimatedBrand.vue'
import { loginApi } from '../api/user' // 引入接口
import { useRouter } from 'vue-router'

const router = useRouter()
const showPassword = ref(false)
const isLoading = ref(false)
const isPasswordFocused = ref(false)
const selectedRole = ref('user') // 默认选择普通用户

const formData = reactive({
  username: '', // 注意这里对应后端的字段名叫 username，不是 email
  password: ''
})

const handleLogin = async () => {
  if (!formData.username || !formData.password) {
    alert('请输入账号和密码！')
    return
  }

  isLoading.value = true
  try {
    // 调用接口，等待后端返回 Token
    const token = await loginApi({
      username: formData.username,
      password: formData.password,
      userRole: selectedRole.value.toUpperCase()
    })

    // 将 Token 存入浏览器的本地存储中
    localStorage.setItem('lingoflow_token', token)
    localStorage.setItem('lingoflow_user_role', selectedRole.value.toUpperCase())

    if (selectedRole.value === 'admin') {
      router.push('/admin/correction')
    } else {
      router.push('/dashboard')
    }

  } catch (error) {
    // 错误已经在拦截器里 alert 过了，这里不需要额外处理
    console.error('登录失败', error)
  } finally {
    isLoading.value = false
  }
}

const handleForgotPassword = () => {
  // 以后这里可以弹出一个 Dialog 或者跳转到 /forgot-password 路由
  alert('找回密码功能即将上线，敬请期待！')
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
/* 这里只保留右侧表单的样式 */
.main-viewport {
  width: 100%;
  height: 100%;
  display: flex;
  background-color: #ffffff;
}

.login-container {
  display: flex;
  width: 100%;
  height: 100%;
}

.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background-color: #ffffff;
  height: 100%;
  overflow: hidden;
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: center;
}

.mobile-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 2rem;
}

@media (min-width: 1024px) {
  .mobile-logo {
    display: none;
  }
}

.brand-name-mobile {
  font-size: 1.5rem;
  font-weight: bold;
  color: #111827;
}

.header-text {
  text-align: center;
  margin-bottom: 1.5rem;
}

.title {
  display: block;
  font-size: 2.5rem;
  font-weight: bold;
  color: #111827;
  margin-bottom: 1.5rem;
}

.role-selector-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  width: 100%;
}

.role-label {
  font-size: 0.75rem;
  color: #9ca3af;
  margin-bottom: 8px;
  font-weight: 500;
  margin-left: 4px;
}

.role-selector {
  display: flex;
  background-color: #f3f4f6;
  border-radius: 12px;
  padding: 4px;
  position: relative;
  width: 100%;
  box-sizing: border-box;
}

.role-btn {
  flex: 1;
  text-align: center;
  font-size: 0.875rem;
  padding: 10px 0;
  cursor: pointer;
  z-index: 2;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #6b7280;
  font-weight: 500;
}

.role-btn.active {
  color: #111827;
  font-weight: 600;
}

.active-indicator {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background-color: #ffffff;
  border-radius: 9px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
}

.active-indicator.admin {
  transform: translateX(100%);
}

.form-group {
  margin-bottom: 1.25rem;
  width: 100%;
}

.label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
  color: #374151;
}

.input-field {
  width: 100%;
  height: 44px;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 0.875rem;
  background-color: #ffffff;
  box-sizing: border-box;
}

.input-field:focus {
  border-color: #000000;
  outline: none;
}

.password-input-wrapper {
  position: relative;
  width: 100%;
}

.eye-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  z-index: 10;
  padding: 5px;
  color: #9ca3af;
}

.simple-eye-svg {
  width: 18px;
  height: 18px;
  position: relative;
}

.simple-eye-svg::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 10px;
  border-radius: 50px;
  border: 1.5px solid currentColor;
}

.simple-eye-svg::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 5px;
  height: 5px;
  background-color: currentColor;
  border-radius: 50%;
}

.simple-eye-svg.showing::before {
  content: '';
  width: 16px;
  height: 1.5px;
  background-color: currentColor;
  border: none;
  border-radius: 1px;
}

.simple-eye-svg.showing::after {
  content: none;
}

.submit-btn {
  width: 100%;
  height: 44px;
  background-color: #111827;
  color: white;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
  margin-top: 10px;
}

.submit-btn:active {
  background-color: #374151;
}

.submit-btn[disabled] {
  opacity: 0.7;
}

.signup-link {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.875rem;
}

.signup-link .text {
  color: #6b7280;
}

.signup-link .link {
  color: #111827;
  font-weight: 500;
  margin-left: 5px;
  cursor: pointer;
  text-decoration: none;
}

/* 预留的忘记密码按钮样式 */
.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.forgot-pwd-link {
  font-size: 0.75rem;
  color: #6b7280;
  cursor: pointer;
  transition: color 0.2s;
}

.forgot-pwd-link:hover {
  color: #111827;
  text-decoration: underline;
}
</style>