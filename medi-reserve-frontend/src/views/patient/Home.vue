<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.clearToken()
  router.push('/patient/login')
}
</script>

<template>
  <div class="home-page">
    <h1>患者首页 - 开发中</h1>
    <el-button type="danger" @click="handleLogout">退出登录</el-button>
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
</style>
