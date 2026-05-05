<template>
  <div class="dashboard-layout">
    <MobileHeader />
    <Sidebar />

    <div class="main-content">
      <div class="page-header">
        <h1 class="page-title">个人中心</h1>
        <p class="page-subtitle">管理你的学习数据资产与账号安全。</p>
      </div>

      <div class="profile-cards-container">
        
        <div class="profile-card">
          <div class="card-title-row">
            <h2>数据资产管理</h2>
          </div>
          <p class="card-desc">数据主权归你所有。你可以随时将本地的生词本、测试记录和阅读历史导出为 JSON 格式备份，或在其他设备上导入恢复。</p>
          
          <div class="action-buttons">
            <button class="primary-btn" @click="handleExportData">一键导出我的数据</button>
            <button class="outline-btn" @click="$refs.fileInput.click()">导入本地 JSON 数据</button>
            <input type="file" ref="fileInput" accept=".json" style="display: none" @change="handleImportData" />
          </div>
        </div>

        <div class="profile-card">
          <div class="card-title-row">
            <h2>安全设置</h2>
          </div>
          <p class="card-desc">定期修改密码有助于保护您的账号安全。修改成功后，请妥善保管您的新密码。</p>
          
          <div class="action-buttons">
            <button class="outline-btn" @click="showPasswordModal = true">修改登录密码</button>
          </div>
        </div>

        <div class="profile-card">
          <div class="card-title-row">
            <h2>账号退出</h2>
          </div>
          <p class="card-desc">退出登录后，需重新验证密码才能访问你的学习舱。</p>
          
          <div class="action-buttons">
            <button class="danger-btn" @click="handleLogout">退出登录</button>
          </div>
        </div>

      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <div class="modal-overlay" v-if="showPasswordModal" @click.self="showPasswordModal = false">
      <div class="modal-card">
        <div class="modal-header">
          <h2>修改登录密码</h2>
          <button class="close-modal" @click="showPasswordModal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="input-group">
            <label>原密码</label>
            <input type="password" v-model="oldPassword" class="modal-input" placeholder="请输入当前使用的密码" />
          </div>
          <div class="input-group">
            <label>新密码</label>
            <input type="password" v-model="newPassword" class="modal-input" placeholder="请输入新密码" />
          </div>
          <div class="input-group">
            <label>确认新密码</label>
            <input type="password" v-model="confirmPassword" class="modal-input" placeholder="请再次输入新密码" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showPasswordModal = false">取消</button>
          <button class="btn-submit" @click="handleChangePassword">确认修改</button>
        </div>
      </div>
    </div>
    <Tabbar />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import MobileHeader from '../components/MobileHeader.vue'
import Tabbar from '../components/Tabbar.vue'
import { importDataApi, downloadUserData, changePasswordApi } from '../api/user'

const router = useRouter()
const fileInput = ref(null)

onMounted(() => {
})

// 导出逻辑
const handleExportData = async () => {
  alert('正在连接记忆宫殿，准备打包您的学习数据...')
  const success = await downloadUserData()
  if (!success) alert('导出失败，请检查后端服务。')
}

// 导入逻辑
const handleImportData = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('file', file)

  alert('正在将您的记忆档案上传至服务器...')
  try {
    await importDataApi(formData)
    alert('数据导入成功！')
  } catch (error) {
    alert('导入失败，请检查文件格式或后端接口。')
  } finally {
    event.target.value = '' // 清空 input
  }
}

// 退出登录逻辑
const handleLogout = () => {
  if (confirm('确定要退出 LingoFlow 吗？')) {
    localStorage.removeItem('lingoflow_token') // 清除登录令牌
    localStorage.removeItem('lingoflow_user_role') // 清除角色信息
    router.push('/login')
  }
}

// 修改密码逻辑
const showPasswordModal = ref(false)
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

const handleChangePassword = async () => {
  if (!oldPassword.value || !newPassword.value) {
    return alert('请输入完整密码信息')
  }
  if (newPassword.value !== confirmPassword.value) {
    return alert('两次输入的新密码不一致')
  }
  try {
    await changePasswordApi({
      oldPassword: oldPassword.value,
      newPassword: newPassword.value
    })
    alert('密码修改成功！请重新登录。')
    localStorage.removeItem('lingoflow_token')
    localStorage.removeItem('lingoflow_user_role')
    router.push('/login')
  } catch (error) {
    alert('修改失败，请检查原密码是否正确')
  }
}
</script>

<style scoped>
/* =========== 基础布局样式 (与其它页面保持一致) =========== */
.dashboard-layout { display: flex; height: 100vh; width: 100vw; background-color: #f3f4f6; font-family: -apple-system, sans-serif; overflow: hidden;}

/* Modal 样式 */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.4); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-card { background: white; width: 90%; max-width: 450px; border-radius: 20px; padding: 30px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
.modal-header h2 { font-size: 22px; font-weight: 800; color: #111827; margin: 0; }
.close-modal { background: none; border: none; font-size: 20px; cursor: pointer; color: #9ca3af; }

.input-group { margin-bottom: 20px; }
.input-group label { display: block; font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 8px; }
.modal-input { width: 100%; padding: 12px; border-radius: 10px; border: 1px solid #e5e7eb; outline: none; font-size: 14px; box-sizing: border-box; }
.modal-input:focus { border-color: #111827; }

.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 30px; }
.btn-cancel { padding: 10px 24px; background: white; border: 1px solid #e5e7eb; border-radius: 8px; font-weight: 600; cursor: pointer; }
.btn-submit { padding: 10px 24px; background: #111827; color: white; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; }


.main-content { flex: 1; display: flex; flex-direction: column; padding: 40px 60px; overflow-y: auto; }
.page-header { margin-bottom: 40px; }
.page-title { font-size: 32px; font-weight: 800; color: #111827; margin: 0 0 10px 0; }
.page-subtitle { font-size: 15px; color: #6b7280; margin: 0; }

.profile-cards-container { display: flex; flex-direction: column; gap: 30px; max-width: 800px; }
.profile-card { background: #ffffff; border-radius: 16px; border: 1px solid #e5e7eb; padding: 30px; display: flex; flex-direction: column; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02); }
.card-title-row h2 { font-size: 20px; font-weight: 700; color: #111827; margin: 0 0 15px 0; }
.card-desc { font-size: 15px; color: #4b5563; line-height: 1.6; margin: 0 0 25px 0; }

.action-buttons { display: flex; gap: 15px; }
.primary-btn { padding: 12px 24px; background: #111827; color: white; border-radius: 8px; border: none; font-weight: 600; cursor: pointer; transition: 0.2s; }
.primary-btn:hover { background: #374151; }
.outline-btn { padding: 12px 24px; background: white; color: #111827; border-radius: 8px; border: 1px solid #111827; font-weight: 600; cursor: pointer; transition: 0.2s; }
.outline-btn:hover { background: #f9fafb; }
.danger-btn { padding: 12px 24px; background: #fef2f2; color: #dc2626; border-radius: 8px; border: 1px solid #fecaca; font-weight: 600; cursor: pointer; transition: 0.2s; }
.danger-btn:hover { background: #fee2e2; border-color: #f87171; }

@media (max-width: 768px) {
  .dashboard-layout {
    flex-direction: column;
    overflow-y: auto;
  }
  .main-content { padding: 20px 15px; padding-bottom: 90px; }
  .action-buttons { flex-direction: column; }
  .action-buttons button { width: 100%; }
}
</style>
