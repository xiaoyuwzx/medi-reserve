<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { doctorApi } from '@/api/doctor'
import type { Schedule } from '@/api/doctor/doctorApi'
import type { AppointmentListVO } from '@/api/patient/patientApi'
import { Calendar, User, DataLine, ChatLineSquare, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

function goToPatients() {
  router.push({ name: 'DoctorPatients' })
}

function goToStatistics() {
  router.push({ name: 'DoctorStatistics' })
}

function goToEvaluations() {
  router.push({ name: 'DoctorEvaluations' })
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

// ========== 统计指标卡片 ==========
const statCards = [
  { key: 'totalPatients', label: '总接诊数', icon: '👥', format: (v: any) => v ?? 0 },
  { key: 'todayPatients', label: '今日接诊', icon: '📅', format: (v: any) => v ?? 0 },
  { key: 'avgScore', label: '平均评分', icon: '⭐', format: (v: any) => `${(Number(v) || 0).toFixed(2)} 星` },
  { key: 'positiveRate', label: '好评率', icon: '👍', format: (v: any) => `${(Number(v) || 0).toFixed(1)}%` },
  { key: 'pendingConsultations', label: '待处理问诊', icon: '⏳', format: (v: any) => v ?? 0 }
]

// ========== 今日概览 ==========
const overviewLoading = ref(false)
const overview = ref<Record<string, unknown>>({})
const todayScheduleCount = ref(0)
const updateTime = ref('')
const pendingPatients = ref<AppointmentListVO[]>([])
const todaySchedules = ref<Schedule[]>([])

const today = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function loadOverview() {
  overviewLoading.value = true
  try {
    const [overviewRes, scheduleRes, patientRes] = await Promise.all([
      doctorApi.doctor.getOverview(),
      doctorApi.doctor.listSchedules({
        startDate: today(),
        endDate: today(),
        status: undefined
      }),
      doctorApi.doctor.getDoctorAppointments({
        date: today(),
        status: 1,
        page: 1,
        size: 5
      })
    ])
    overview.value = (overviewRes as unknown as Record<string, unknown>) ?? {}
    const schedules = (scheduleRes as unknown as Schedule[]) ?? []
    todayScheduleCount.value = schedules.length
    todaySchedules.value = schedules
    const patients = (patientRes as unknown as { list?: AppointmentListVO[] }) ?? {}
    pendingPatients.value = patients.list ?? []
    updateTime.value = new Date().toLocaleString('zh-CN')
  } catch {
    overview.value = {}
    todayScheduleCount.value = 0
    todaySchedules.value = []
    pendingPatients.value = []
  } finally {
    overviewLoading.value = false
  }
}

onMounted(() => {
  loadOverview()
})
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
      <div class="action-card" @click="goToStatistics">
        <el-icon :size="28"><DataLine /></el-icon>
        <span class="action-label">数据统计</span>
        <span class="action-desc">查看运营数据</span>
      </div>
      <div class="action-card" @click="goToEvaluations">
        <el-icon :size="28"><ChatLineSquare /></el-icon>
        <span class="action-label">患者评价</span>
        <span class="action-desc">查看所有评价</span>
      </div>
    </div>

    <!-- 统计指标卡片 -->
    <div class="stat-cards" v-loading="overviewLoading">
      <div class="stat-cards-wrapper">
        <div v-for="card in statCards" :key="card.key" class="stat-card">
          <div class="stat-icon">{{ card.icon }}</div>
          <div class="stat-value">{{ card.format(overview[card.key]) }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </div>

    <!-- 今日概览 -->
    <div class="today-overview" v-loading="overviewLoading">
      <div class="overview-title">📊 今日概览</div>
      <el-row :gutter="16">
        <el-col :span="8">
          <div class="overview-item">
            <div class="overview-value">{{ overview.todayPatients ?? 0 }}</div>
            <div class="overview-label">今日接诊</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="overview-item">
            <div class="overview-value">{{ overview.pendingConsultations ?? 0 }}</div>
            <div class="overview-label">待处理问诊</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="overview-item">
            <div class="overview-value">{{ todayScheduleCount }}</div>
            <div class="overview-label">今日排班</div>
          </div>
        </el-col>
      </el-row>

      <!-- 详细信息列表 -->
      <div class="detail-section">
        <el-row :gutter="24">
          <!-- 待处理问诊列表 -->
          <el-col :span="12">
            <div class="detail-box">
              <div class="detail-title">待处理问诊</div>
              <template v-if="pendingPatients.length > 0">
                <div
                  v-for="item in pendingPatients"
                  :key="item.id"
                  class="detail-item"
                >
                  <span class="detail-name">{{ item.patientName || '未知患者' }}</span>
                  <span class="detail-time">{{ item.periodText }}</span>
                </div>
                <div class="detail-more">
                  <el-button text size="small" type="primary" @click="goToPatients">
                    查看全部 →
                  </el-button>
                </div>
              </template>
              <div v-else class="detail-empty">暂无待处理问诊</div>
            </div>
          </el-col>

          <!-- 今日排班列表 -->
          <el-col :span="12">
            <div class="detail-box">
              <div class="detail-title">今日排班</div>
              <template v-if="todaySchedules.length > 0">
                <div
                  v-for="item in todaySchedules"
                  :key="item.scheduleId"
                  class="detail-item"
                >
                  <span class="detail-name">{{ item.periodText || (item.period === 1 ? '上午' : '下午') }}</span>
                  <span class="detail-status" :class="
                    item.status === 1 ? 'status-available' :
                    item.status === 3 ? 'status-full' :
                    'status-closed'
                  ">
                    {{ item.status === 1 ? `剩余${item.remainingCount ?? 0}号` :
                       item.status === 3 ? '已满' : (item.statusText || '停诊') }}
                  </span>
                </div>
                <div class="detail-more">
                  <el-button text size="small" type="primary" @click="router.push({ name: 'DoctorSchedules' })">
                    查看全部 →
                  </el-button>
                </div>
              </template>
              <div v-else class="detail-empty">今日暂无排班</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="overview-time">数据更新时间：{{ updateTime || '加载中...' }}</div>
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
  flex-wrap: wrap;
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

/* 统计指标卡片 */
.stat-cards {
  margin-bottom: 24px;
}

.stat-cards-wrapper {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1 0 0;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}

.stat-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

/* 今日概览 */
.today-overview {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.overview-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.overview-item {
  text-align: center;
  padding: 12px 0;
}

.overview-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.overview-label {
  font-size: 13px;
  color: #909399;
}

/* 详细信息 */
.detail-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.detail-box {
  min-height: 120px;
}

.detail-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 10px;
}

.detail-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid #f5f5f5;
}

.detail-item:last-of-type {
  border-bottom: none;
}

.detail-name {
  font-size: 13px;
  color: #303133;
}

.detail-time {
  font-size: 12px;
  color: #909399;
}

.detail-status {
  font-size: 12px;
  font-weight: 500;
}

.detail-status.status-available {
  color: #67c23a;
}

.detail-status.status-full {
  color: #f56c6c;
}

.detail-status.status-closed {
  color: #b0b0b0;
}

.detail-more {
  text-align: center;
  margin-top: 6px;
}

.detail-empty {
  text-align: center;
  font-size: 13px;
  color: #c0c4cc;
  padding: 20px 0;
}

.overview-time {
  text-align: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  font-size: 12px;
  color: #c0c4cc;
}
</style>