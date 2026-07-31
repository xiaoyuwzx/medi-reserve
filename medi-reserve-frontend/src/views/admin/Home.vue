<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<template>
  <div class="admin-home">
    <h2 class="welcome-title">👋 欢迎回来，{{ userStore.name || userStore.username }}</h2>
    <p class="welcome-desc">MediReserve 管理端为您提供医生审核、证件审核、管理员管理等管理功能。</p>
    <el-divider />
    <p class="welcome-hint">请从左侧菜单选择功能模块</p>
  </div>
</template>

<style scoped>
.admin-home {
  padding: 24px 0;
}

.welcome-title {
  margin: 0 0 8px;
  font-size: 22px;
  color: #303133;
}

.welcome-desc {
  margin: 0 0 0;
  font-size: 14px;
  color: #909399;
}

.welcome-hint {
  text-align: center;
  font-size: 14px;
  color: #c0c4cc;
  padding: 32px 0;
}
</style>