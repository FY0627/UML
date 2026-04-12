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
            <span class="title">Join Us!</span>
            <div class="role-selector-container">
              <span class="role-label">选择账号类型</span>
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
            <input class="input-field" v-model="formData.username" type="text" placeholder="设置一个好记的用户名" />
          </div>

          <div class="form-group">
            <span class="label">密码</span>
            <div class="password-input-wrapper">
              <input 
                class="input-field pr-10" 
                v-model="formData.password" 
                :type="showPassword ? 'text' : 'password'" 
                placeholder="至少 6 位密码" 
                @focus="isPasswordFocused = true" 
                @blur="isPasswordFocused = false"
                @keyup.enter="handleRegister"
              />
              <div class="eye-icon" @click="showPassword = !showPassword">
                <div class="simple-eye-svg" :class="{ showing: showPassword }"></div>
              </div>
            </div>
          </div>

          <div class="form-row" v-if="selectedRole === 'user'">
            <div class="form-group half-width">
              <span class="label">目标语言</span>
              <select class="input-field select-field" v-model="formData.targetLanguage">
                <option value="EN">英语 (EN)</option>
              </select>
            </div>
            
            <div class="form-group half-width">
              <span class="label">当前水平</span>
              <select class="input-field select-field" v-model="formData.currentLevel" style="padding-left: 5px;">
                <optgroup label="国内考试">
                  <option value="中考英语水平">中考英语</option>
                  <option value="高考英语水平">高考英语</option>
                  <option value="大学英语四级(CET4)">CET 4 (四级)</option>
                  <option value="大学英语六级(CET6)">CET 6 (六级)</option>
                  <option value="考研英语水平">考研英语</option>
                  <option value="英语专业八级(TEM8)">专八 (TEM8)</option>
                </optgroup>
                <optgroup label="PTE 考试">
                  <option value="PTE 45分水平">PTE 45分 (基础)</option>
                  <option value="PTE 58分水平">PTE 58分 (常用)</option>
                  <option value="PTE 65分水平">PTE 65分 (进阶)</option>
                  <option value="PTE 79分水平">PTE 79分 (高阶)</option>
                </optgroup>
                <optgroup label="雅思 (IELTS)">
                  <option value="雅思 5.5分水平">雅思 5.5分</option>
                  <option value="雅思 6.0分水平">雅思 6.0分</option>
                  <option value="雅思 6.5分水平">雅思 6.5分</option>
                  <option value="雅思 7.0分水平">雅思 7.0分</option>
                  <option value="雅思 8.0分水平">雅思 8.0分</option>
                </optgroup>
                <optgroup label="托福 (TOEFL)">
                  <option value="托福 80分水平">托福 80分</option>
                  <option value="托福 90分水平">托福 90分</option>
                  <option value="托福 100分水平">托福 100分</option>
                  <option value="托福 110分水平">托福 110+分</option>
                </optgroup>
              </select>
            </div>
          </div>

          <button class="submit-btn" :disabled="isLoading" @click="handleRegister">
            {{ isLoading ? '注册中...' : '注 册' }}
          </button>

          <div class="signup-link">
            <span class="text">已经有账号了？</span>
            <span class="link" @click="goToLogin">直接登录</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import AnimatedBrand from '../components/AnimatedBrand.vue'
import { registerApi } from '../api/user' 

const router = useRouter()
const showPassword = ref(false)
const isLoading = ref(false)
const isPasswordFocused = ref(false)
const selectedRole = ref('user') // 默认选择普通用户

const formData = reactive({
  username: '',
  password: '',
  targetLanguage: 'EN', // 默认选英语
  currentLevel: ''
})

const handleRegister = async () => {
  if (!formData.username || !formData.password) {
    alert('账号和密码不能为空！')
    return
  }
  
  isLoading.value = true
  try {
    // 调用注册接口
    await registerApi({
      username: formData.username,
      password: formData.password,
      targetLanguage: formData.targetLanguage,
      currentLevel: formData.currentLevel,
      userRole: selectedRole.value.toUpperCase()
    })

    localStorage.setItem('lingoflow_default_difficulty', formData.currentLevel)
    
    alert('注册成功！快去登录吧！')
    router.push('/login') // 注册成功后自动跳回登录页
    
  } catch (error) {
    console.error('注册失败', error)
  } finally {
    isLoading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
/* 大部分样式和 Login.vue 保持一致 */
.main-viewport { width: 100%; height: 100%; display: flex; background-color: #ffffff; }
.login-container { display: flex; width: 100%; height: 100%; }
.right-panel { flex: 1; display: flex; align-items: center; justify-content: center; padding: 2rem; background-color: #ffffff; height: 100%; overflow: hidden;}
.form-wrapper { width: 100%; max-width: 360px; display: flex; flex-direction: column; height: 100%; justify-content: center;}
.mobile-logo { display: flex; justify-content: center; margin-bottom: 2rem; }
@media (min-width: 1024px) { .mobile-logo { display: none; } }
.brand-name-mobile { font-size: 1.5rem; font-weight: bold; color: #111827; }
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
.form-group { margin-bottom: 1.25rem; width: 100%; }

/* 新增：两列布局样式，让语言和水平并排显示 */
.form-row { display: flex; gap: 12px; }
.half-width { flex: 1; }
.select-field { background-color: #ffffff; cursor: pointer; }

.label { display: block; font-size: 0.875rem; font-weight: 500; margin-bottom: 0.5rem; color: #374151; }
.input-field { width: 100%; height: 44px; padding: 0 12px; border: 1px solid #e5e7eb; border-radius: 8px; font-size: 0.875rem; background-color: #ffffff; box-sizing: border-box;}
.input-field:focus { border-color: #000000; outline: none;}
.password-input-wrapper { position: relative; width: 100%; }
.eye-icon { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); cursor: pointer; z-index: 10; padding: 5px; color: #9ca3af; }
.simple-eye-svg { width: 18px; height: 18px; position: relative; }
.simple-eye-svg::before { content: ''; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 16px; height: 10px; border-radius: 50px; border: 1.5px solid currentColor; }
.simple-eye-svg::after { content: ''; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 5px; height: 5px; background-color: currentColor; border-radius: 50%; }
.simple-eye-svg.showing::before { content: ''; width: 16px; height: 1.5px; background-color: currentColor; border: none; border-radius: 1px; }
.simple-eye-svg.showing::after { content: none; }
.submit-btn { width: 100%; height: 44px; background-color: #111827; color: white; border-radius: 8px; font-size: 0.875rem; font-weight: 500; display: flex; align-items: center; justify-content: center; border: none; cursor: pointer; margin-top: 10px; }
.submit-btn:active { background-color: #374151; }
.submit-btn[disabled] { opacity: 0.7; }
.signup-link { text-align: center; margin-top: 1.5rem; font-size: 0.875rem; }
.signup-link .text { color: #6b7280; }
.signup-link .link { color: #111827; font-weight: 500; margin-left: 5px; cursor: pointer; text-decoration: none;}
</style>