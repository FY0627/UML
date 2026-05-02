<template>
  <div class="dashboard-layout">
    <MobileHeader />
    <Sidebar />

    <!-- Main Content -->
    <div class="main-content">
      <div class="page-header">
        <h1 class="page-title">系统通知中心</h1>
        <p class="page-subtitle">及时掌握纠错审核反馈与系统动态。</p>
      </div>

      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>正在拉取消息...</p>
      </div>

      <div v-else-if="messageList.length === 0" class="empty-state">
        <div class="empty-icon">🔔</div>
        <h2>暂无系统消息</h2>
        <p>当您的纠错反馈被处理后，这里会收到通知。</p>
      </div>

      <div v-else class="message-list">
        <div v-for="msg in messageList" :key="msg.id" class="message-card" :class="{ 'unread': msg.isRead === 0 }" @click="handleRead(msg)">
          <div class="message-header">
            <div class="status-dot" v-if="msg.isRead === 0"></div>
            <span class="message-time">{{ formatDate(msg.createTime) }}</span>
          </div>
          <div class="message-body">
            {{ msg.content }}
          </div>
        </div>

        <div class="pagination" v-if="totalPages > 1">
          <button :disabled="page <= 1" @click="page--; fetchMessages()">上一页</button>
          <span class="page-info">第 {{ page }} / {{ totalPages }} 页</span>
          <button :disabled="page >= totalPages" @click="page++; fetchMessages()">下一页</button>
        </div>
      </div>
    </div>
    <Tabbar />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import MobileHeader from '../components/MobileHeader.vue'
import Tabbar from '../components/Tabbar.vue'
import { getMessageListApi, markAsReadApi } from '../api/message'

const router = useRouter()
const messageList = ref([])
const isLoading = ref(true)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)

const fetchMessages = async (silent = false) => {
  if (!silent) isLoading.value = true
  try {
    const res = await getMessageListApi(page.value, size.value)
    messageList.value = res.records
    totalPages.value = res.pages
  } catch (err) {
    console.error(err)
  } finally {
    if (!silent) isLoading.value = false
  }
}

let timer = null

onMounted(() => {
  fetchMessages()
  timer = setInterval(() => {
    fetchMessages(true)
  }, 10000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const handleRead = async (msg) => {
  if (msg.isRead === 1) return
  try {
    await markAsReadApi(msg.id)
    msg.isRead = 1
  } catch (err) {
    console.error(err)
  }
}

const formatDate = (dateArr) => {
    if (!dateArr) return '-'
    if (Array.isArray(dateArr)) {
        return `${dateArr[0]}-${String(dateArr[1]).padStart(2, '0')}-${String(dateArr[2]).padStart(2, '0')} ${String(dateArr[3]).padStart(2, '0')}:${String(dateArr[4]).padStart(2, '0')}`
    }
    return dateArr
}

onMounted(fetchMessages)
</script>

<style scoped>
.dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f3f4f6;
  overflow: hidden;
}

.dashboard-layout { display: flex; height: 100vh; width: 100vw; background-color: #f3f4f6; overflow: hidden;}

/* Main Content */
.main-content {
  flex: 1;
  padding: 40px 60px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.page-header { margin-bottom: 40px; }
.page-title { font-size: 32px; font-weight: 800; color: #111827; margin: 0 0 10px 0; }
.page-subtitle { font-size: 15px; color: #6b7280; margin: 0; }

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 800px;
}

.message-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  transition: 0.2s;
  cursor: pointer;
  position: relative;
}

.message-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.message-card.unread {
  border-left: 4px solid #111827;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #111827;
  border-radius: 50%;
  position: absolute;
  top: 20px;
  left: 8px;
}

.message-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.message-time {
  font-size: 12px;
  color: #9ca3af;
}

.message-body {
  font-size: 15px;
  color: #374151;
  line-height: 1.6;
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-top: 100px;
  color: #9ca3af;
}

.empty-icon { font-size: 48px; margin-bottom: 16px; }

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top: 4px solid #111827;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .dashboard-layout {
    flex-direction: column;
    overflow-y: auto;
  }
  .main-content {
    padding: 20px 15px;
    padding-bottom: 90px;
  }
  .message-list {
    max-width: 100%;
  }
}
</style>
