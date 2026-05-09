<template>
  <!-- 侧边栏 (仅桌面端显示) -->
  <div class="sidebar desktop-only">
    <div class="top-section">
      <div class="logo-box" @click="router.push('/dashboard')">LF</div>
      <div class="nav-menu">
        <div class="nav-item" :class="{ active: currentPath === '/dashboard' }" @click="router.push('/dashboard')">
          <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg>
          <span>工作台</span>
        </div>
        <div class="nav-item" :class="{ active: currentPath === '/vocabulary' }" @click="router.push('/vocabulary')">
          <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"></path></svg>
          <span>生词本</span>
        </div>
        <div class="nav-item" :class="{ active: currentPath === '/history' }" @click="router.push('/history')">
          <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          <span>历史</span>
        </div>
        <div class="nav-item" :class="{ active: currentPath === '/message' }" @click="router.push('/message')">
          <div class="icon-wrapper">
            <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg>
            <div v-if="unreadCount > 0" class="unread-badge"></div>
          </div>
          <span>消息</span>
        </div>
      </div>
    </div>
    <div class="bottom-section">
      <div class="avatar" :class="{ active: currentPath === '/profile' }" @click="router.push('/profile')" title="用户中心">👨‍💻</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getUnreadCountApi } from '../api/message'

const router = useRouter()
const route = useRoute()

const currentPath = computed(() => route.path)

const unreadCount = ref(0)
const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCountApi()
    unreadCount.value = res
  } catch (err) { console.error(err) }
}

let timer = null

onMounted(() => {
  fetchUnreadCount()
  timer = setInterval(fetchUnreadCount, 10000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.sidebar { width: 80px; background-color: #ffffff; border-right: 1px solid #e5e7eb; display: flex; flex-direction: column; justify-content: space-between; align-items: center; padding: 24px 0; flex-shrink: 0; height: 100vh; position: sticky; top: 0; left: 0;}
.top-section { display: flex; flex-direction: column; align-items: center; width: 100%; }
.logo-box { width: 40px; height: 40px; background-color: #111827; color: #ffffff; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-weight: 800; cursor: pointer; margin-bottom: 60px; }
.nav-menu { display: flex; flex-direction: column; gap: 36px; width: 100%; }
.nav-item { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #9ca3af; cursor: pointer; transition: 0.2s;}
.nav-icon { width: 24px; height: 24px; }
.nav-item span { font-size: 12px; font-weight: 500; }
.nav-item:hover { color: #4b5563; }
.nav-item.active { color: #111827; }
.avatar { width: 40px; height: 40px; background-color: #f3f4f6; border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.2s; border: 2px solid transparent;}
.avatar:hover, .avatar.active { border-color: #111827; transform: scale(1.05); }

.icon-wrapper { position: relative; display: flex; align-items: center; justify-content: center; }
.unread-badge { position: absolute; top: -2px; right: -2px; width: 8px; height: 8px; background-color: #ef4444; border-radius: 50%; border: 2px solid #ffffff; }

@media (max-width: 768px) {
  .desktop-only { display: none !important; }
}
</style>
