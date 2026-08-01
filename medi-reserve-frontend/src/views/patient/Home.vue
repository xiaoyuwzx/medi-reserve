<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import HotDoctors from '@/components/patient/HotDoctors.vue'
import { Search, Calendar, Star, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

function handleDropdownCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push({ name: 'PatientProfile' })
      break
    case 'password':
      router.push({ name: 'PatientPassword' })
      break
    case 'logout':
      userStore.clearToken()
      router.push('/patient/login')
      break
  }
}

function goToDoctorList() {
  router.push({ name: 'DoctorList' })
}

function goToMyAppointments() {
  router.push({ name: 'MyAppointments' })
}

function goToMyEvaluations() {
  router.push({ name: 'MyEvaluations' })
}

function handleHotDoctorNavigate(doctorId: number) {
  router.push({
    name: 'DoctorDetail',
    params: { doctorId }
  })
}
</script>

<template>
  <div class="home-page">
    <!-- 顶部导航 -->
    <div class="top-bar">
      <div class="welcome">
        👋 欢迎使用 MediReserve 患者端
      </div>
      <el-dropdown @command="handleDropdownCommand">
        <span class="user-info">
          <el-avatar :size="32">{{ (userStore.name || userStore.username || 'U').charAt(0) }}</el-avatar>
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
      <div class="action-card" @click="goToDoctorList">
        <el-icon :size="28"><Search /></el-icon>
        <span class="action-label">预约挂号</span>
        <span class="action-desc">选择科室和医生</span>
      </div>
      <div class="action-card" @click="goToMyAppointments">
        <el-icon :size="28"><Calendar /></el-icon>
        <span class="action-label">我的预约</span>
        <span class="action-desc">查看预约记录</span>
      </div>
      <div class="action-card" @click="goToMyEvaluations">
        <el-icon :size="28"><Star /></el-icon>
        <span class="action-label">我的评价</span>
        <span class="action-desc">查看已发表的评价</span>
      </div>
    </div>

    <!-- 热门医生排行榜 -->
    <div class="hot-section">
      <HotDoctors @navigate="handleHotDoctorNavigate" />
    </div>

    <!-- 查看全部医生 -->
    <div class="view-all">
      <el-button type="primary" plain @click="goToDoctorList">
        查看全部医生 →
      </el-button>
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

.hot-section {
  margin-bottom: 24px;
}

.view-all {
  display: flex;
  justify-content: center;
}
</style>