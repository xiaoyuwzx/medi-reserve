<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import HotDoctors from '@/components/patient/HotDoctors.vue'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.clearToken()
  router.push('/patient/login')
}

function goToDoctorList() {
  router.push({ name: 'DoctorList' })
}

function goToMyAppointments() {
  // 我的预约（后续阶段实现，当前先占位）
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
        👋 欢迎回来，<strong>{{ userStore.username || '患者' }}</strong>
      </div>
      <el-button text type="danger" @click="handleLogout">退出登录</el-button>
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