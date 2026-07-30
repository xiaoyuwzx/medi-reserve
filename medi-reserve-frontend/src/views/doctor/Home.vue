<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Calendar, User, DataLine, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

function goToPatients() {
  router.push({ name: 'DoctorPatients' })
}

function handleDropdownCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push({ name: 'DoctorProfile' })
      break
    case 'password':
      router.push({ name: 'DoctorPassword' })
      break
    case 'logout':
      userStore.clearToken()
      router.push('/doctor/login')
      break
  }
}
</script>

<template>
  <div class="home-page">
    <!-- 顶部导航 -->
    <div class="top-bar">
      <div class="welcome">
        👋 欢迎使用 MediReserve 医生端
      </div>
      <el-dropdown @command="handleDropdownCommand">
        <span class="user-info">
          <el-avatar :size="32">{{ (userStore.name || userStore.username || 'D').charAt(0) }}</el-avatar>
          <span class="username">{{ userStore.name || userStore.username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">查看个人信息</el-dropdown-item>
            <el-dropdown-item command="password">修改密码</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-actions">
      <div class="action-card" @click="router.push({ name: 'DoctorSchedules' })">
        <el-icon :size="28"><Calendar /></el-icon>
        <span class="action-label">排班管理</span>
        <span class="action-desc">管理我的排班</span>
      </div>
      <div class="action-card" @click="goToPatients">
        <el-icon :size="28"><User /></el-icon>
        <span class="action-label">问诊患者</span>
        <span class="action-desc">查看今日问诊患者</span>
      </div>
      <div class="action-card">
        <el-icon :size="28"><DataLine /></el-icon>
        <span class="action-label">数据统计</span>
        <span class="action-desc">开发中...</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.welcome {
  font-size: 18px;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-info .username {
  font-size: 14px;
  color: #303133;
}

.quick-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.action-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 20px 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: box-shadow 0.2s;
  color: #409eff;
}

.action-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
}

.action-label {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.action-desc {
  font-size: 12px;
  color: #909399;
}
</style>