<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.clearToken()
  router.push('/admin/login')
}
</script>

<template>
  <div class="home-page">
    <div class="top-bar">
      <h1>欢迎使用 MediReserve 管理端</h1>
      <el-button type="danger" @click="handleLogout">退出登录</el-button>
    </div>
    <p class="hint">管理端功能开发中...</p>
  </div>
</template>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  min-height: 100vh;
}

.top-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.top-bar h1 {
  margin: 0;
  color: #303133;
}

.hint {
  color: #909399;
  font-size: 14px;
}
</style>