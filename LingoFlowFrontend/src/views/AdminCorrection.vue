<template>
  <div class="dashboard-layout">
    <!-- Sidebar -->
    <div class="sidebar">
      <div class="top-section">
        <div class="logo-box">LF</div>
        <div class="nav-menu">
          <div class="nav-item active">
            <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
            </svg>
            <span>纠错工单</span>
          </div>
        </div>
      </div>
      <div class="bottom-section">
        <div class="avatar" @click="handleLogout" title="退出登录">🚪</div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <div class="admin-header">
        <h1 class="page-title">纠错工单管理</h1>
      </div>

      <div class="table-card">
        <div class="table-header">
          <div class="filter-group">
            <span class="filter-label">状态筛选：</span>
            <select v-model="statusFilter" class="admin-select" @change="fetchTickets">
              <option value="">全部</option>
              <option value="PENDING">待处理</option>
              <option value="ACCEPTED">已采纳</option>
              <option value="REJECTED">已驳回</option>
            </select>
          </div>
        </div>

        <div class="table-container">
          <table class="admin-table">
            <thead>
              <tr>
                <th>工单 ID</th>
                <th>类型</th>
                <th>原始内容 (截断)</th>
                <th>状态</th>
                <th>提交时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="ticket in ticketList" :key="ticket.id">
                <td>#{{ ticket.id }}</td>
                <td>
                  <span class="type-tag">{{ ticket.type }}</span>
                </td>
                <td class="content-cell">{{ ticket.originalContent }}</td>
                <td>
                  <span class="status-badge" :class="ticket.status.toLowerCase()">
                    {{ formatStatus(ticket.status) }}
                  </span>
                </td>
                <td>{{ formatDate(ticket.createTime) }}</td>
                <td>
                  <button class="review-btn" @click="openDetail(ticket)">
                    {{ ticket.status === 'PENDING' ? '查看审核' : '详情' }}
                  </button>
                </td>
              </tr>
              <tr v-if="ticketList.length === 0">
                <td colspan="6" class="empty-row">暂无相关工单</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <button :disabled="page <= 1" @click="page--; fetchTickets()">上一页</button>
          <span class="page-info">第 {{ page }} 页</span>
          <button :disabled="ticketList.length < size" @click="page++; fetchTickets()">下一页</button>
        </div>
      </div>
    </div>

    <!-- Detail Modal -->
    <div class="modal-overlay" v-if="showModal" @click.self="closeModal">
      <div class="modal-card">
        <div class="modal-header">
          <h2>工单审核详情</h2>
          <button class="close-modal" @click="closeModal">✕</button>
        </div>
        
        <div class="modal-body">
          <div class="compare-container">
            <div class="compare-box">
              <label>原始内容</label>
              <div class="content-text">{{ currentTicket.originalContent }}</div>
            </div>
            <div class="compare-box highlight">
              <label>用户修改建议</label>
              <div class="content-text">{{ currentTicket.userSuggestion }}</div>
            </div>
          </div>

          <div class="remark-section">
            <label>管理员处理备注</label>
            <textarea v-model="adminRemark" placeholder="请输入处理备注（用户可见）" :disabled="currentTicket.status !== 'PENDING'"></textarea>
          </div>
        </div>

        <div class="modal-footer" v-if="currentTicket.status === 'PENDING'">
          <button class="btn-reject" @click="handleReject" :disabled="isProcessing">驳回</button>
          <button class="btn-adopt" @click="handleAdopt" :disabled="isProcessing">
            {{ isProcessing ? '处理中...' : '采纳并修正' }}
          </button>
        </div>
        <div class="modal-footer" v-else>
          <div class="processed-info">
            此工单已处理。备注：{{ currentTicket.adminRemark || '无' }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCorrectionListApi, adoptCorrectionApi, rejectCorrectionApi } from '../api/admin'

const router = useRouter()
const ticketList = ref([])
const page = ref(1)
const size = ref(10)
const statusFilter = ref('')
const isLoading = ref(false)

const showModal = ref(false)
const currentTicket = ref(null)
const adminRemark = ref('')
const isProcessing = ref(false)

const fetchTickets = async (silent = false) => {
  if (!silent) isLoading.value = true
  try {
    const res = await getCorrectionListApi({
      page: page.value,
      size: size.value,
      status: statusFilter.value
    })
    ticketList.value = res.records
  } catch (err) {
    console.error(err)
  } finally {
    if (!silent) isLoading.value = false
  }
}

let timer = null

onMounted(() => {
  fetchTickets()
  timer = setInterval(() => {
    fetchTickets(true)
  }, 10000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const openDetail = (ticket) => {
  currentTicket.value = ticket
  adminRemark.value = ticket.adminRemark || ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  currentTicket.value = null
}

const handleAdopt = async () => {
  if (isProcessing.value) return
  isProcessing.value = true
  try {
    await adoptCorrectionApi(currentTicket.value.id, adminRemark.value)
    alert('已采纳该建议，系统已发送通知！')
    closeModal()
    fetchTickets()
  } catch (err) {
    alert('操作失败')
  } finally {
    isProcessing.value = false
  }
}

const handleReject = async () => {
  if (isProcessing.value) return
  isProcessing.value = true
  try {
    await rejectCorrectionApi(currentTicket.value.id, adminRemark.value)
    alert('工单已驳回')
    closeModal()
    fetchTickets()
  } catch (err) {
    alert('操作失败')
  } finally {
    isProcessing.value = false
  }
}

const handleLogout = () => {
  localStorage.removeItem('lingoflow_token')
  localStorage.removeItem('lingoflow_user_role')
  router.push('/login')
}

const formatStatus = (s) => {
  const map = { PENDING: '待处理', ACCEPTED: '已采纳', REJECTED: '已驳回' }
  return map[s] || s
}

const formatDate = (dateArr) => {
    if (!dateArr) return '-'
    // 假设后端返回的是 LocalDateTime 数组格式 [2026, 4, 29, 9, 0, 0]
    if (Array.isArray(dateArr)) {
        return `${dateArr[0]}-${String(dateArr[1]).padStart(2, '0')}-${String(dateArr[2]).padStart(2, '0')} ${String(dateArr[3]).padStart(2, '0')}:${String(dateArr[4]).padStart(2, '0')}`
    }
    return dateArr
}

onMounted(fetchTickets)
</script>

<style scoped>
/* 继承 Dashboard 的布局基础 */
.dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f3f4f6;
  overflow: hidden;
}

.sidebar {
  width: 80px;
  background-color: #111827; /* 管理员侧边栏使用深色，以示区别 */
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  padding: 24px 0;
  flex-shrink: 0;
}

.logo-box {
  width: 40px;
  height: 40px;
  background-color: #ffffff;
  color: #111827;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  margin-bottom: 60px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #9ca3af;
  cursor: pointer;
  transition: 0.2s;
}

.nav-icon { width: 24px; height: 24px; }
.nav-item span { font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
.nav-item.active { color: #ffffff; }

.avatar {
  width: 40px;
  height: 40px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 20px;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 32px;
  overflow-y: auto;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #111827;
}

.role-badge {
  background: #111827;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
}

/* 表格卡片 */
.table-card {
  background: white;
  border-radius: 20px;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.table-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
}

.filter-label {
  font-size: 14px;
  font-weight: 600;
  color: #4b5563;
}

.admin-select {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  outline: none;
  background: #f9fafb;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

th {
  text-align: left;
  padding: 16px 24px;
  background: #f9fafb;
  font-size: 12px;
  font-weight: 700;
  color: #6b7280;
  text-transform: uppercase;
}

td {
  padding: 16px 24px;
  border-bottom: 1px solid #f3f4f6;
  font-size: 14px;
  color: #374151;
}

.content-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.pending { background: #fef3c7; color: #92400e; }
.status-badge.accepted { background: #d1fae5; color: #065f46; }
.status-badge.rejected { background: #fee2e2; color: #991b1b; }

.review-btn {
  background: #111827;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

/* 分页 */
.pagination {
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
}

.pagination button {
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 6px;
  cursor: pointer;
}

.pagination button:disabled { opacity: 0.5; cursor: not-allowed; }

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-card {
  background: white;
  width: 90%;
  max-width: 800px;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.close-modal {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #9ca3af;
}

.compare-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.compare-box {
  background: #f9fafb;
  padding: 20px;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
}

.compare-box.highlight {
  border-color: #111827;
  background: #f8fafc;
}

.compare-box label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  color: #6b7280;
  margin-bottom: 12px;
}

.content-text {
  font-size: 14px;
  line-height: 1.6;
  color: #111827;
}

.remark-section {
  margin-bottom: 24px;
}

.remark-section label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.remark-section textarea {
  width: 100%;
  height: 80px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  outline: none;
  resize: none;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-reject {
  padding: 10px 24px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

.btn-adopt {
  padding: 10px 24px;
  background: #111827;
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

.processed-info {
  width: 100%;
  text-align: center;
  padding: 12px;
  background: #f3f4f6;
  border-radius: 10px;
  font-size: 14px;
  color: #6b7280;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .dashboard-layout {
    flex-direction: column;
  }

  /* 侧边栏变底部导航 */
  .sidebar {
    width: 100%;
    height: 65px;
    flex-direction: row;
    padding: 0 20px;
    order: 2; /* 放到下面 */
    border-top: 1px solid #1f2937;
    position: fixed;
    bottom: 0;
    z-index: 50;
  }

  .logo-box {
    display: none;
  }

  .nav-menu {
    flex-direction: row;
    width: auto;
  }

  .nav-item {
    flex-direction: row;
    padding: 8px 12px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 8px;
  }

  .nav-item span {
    font-size: 14px;
    margin-left: 6px;
  }

  .avatar {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  /* 主内容区调整 */
  .main-content {
    padding: 20px 15px 85px 15px; /* 给底部导航栏留出空间 */
    order: 1;
    overflow-x: hidden;
  }

  .admin-header {
    margin-bottom: 0;
  }

  .page-title {
    font-size: 24px;
  }

  /* 表格卡片适配 */
  .table-card {
    border-radius: 12px;
  }

  .filter-group {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .admin-select {
    width: 65%;
  }

  /* 允许表格在移动端横向滑动 */
  .table-container {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .admin-table {
    min-width: 600px;
  }

  th, td {
    padding: 12px 16px;
  }

  .content-cell {
    max-width: 150px;
  }

  /* 模态框适配 */
  .modal-card {
    width: 92%;
    padding: 20px;
    max-height: 85vh;
    overflow-y: auto;
  }

  .compare-container {
    grid-template-columns: 1fr; /* 从左右排版变成上下排版 */
    gap: 15px;
  }

  .modal-footer {
    flex-direction: column;
    gap: 10px;
  }
  
  .btn-reject, .btn-adopt {
    width: 100%;
  }
}
</style>
