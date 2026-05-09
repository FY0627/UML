<template>
  <div class="mobile-header">
    <div class="logo-box" @click="router.push('/profile')" title="用户中心">LF</div>
    <span class="mobile-title">LingoFlow</span>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped>
.mobile-header {
  display: none;
}

@media (max-width: 768px) {
  .mobile-header {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 15px 20px;
    background: white;
    border-bottom: 1px solid #e5e7eb;
    position: sticky;
    top: 0;
    z-index: 50;
    width: 100%;
    box-sizing: border-box;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  }
  .mobile-title {
    font-weight: 700;
    font-size: 18px;
    color: #111827;
  }
  .logo-box {
    width: 32px;
    height: 32px;
    background-color: #111827;
    color: #ffffff;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 800;
    font-size: 14px;
    cursor: pointer;
  }
}
</style>
