<template>
  <div class="home-page">
    <!-- 欢迎区域 -->
    <div class="welcome-banner">
      <div class="welcome-left">
        <h2 class="welcome-title">👋 你好，{{ userStore.username }}</h2>
        <p class="welcome-desc">欢迎来到 MediReserve 医疗预约平台</p>
      </div>
      <div class="welcome-right">
        <el-icon :size="48" color="#409EFF20"><Service /></el-icon>
      </div>
    </div>

    <!-- 热门科室 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">
          <el-icon color="#409EFF"><OfficeBuilding /></el-icon>
          热门科室
        </h3>
      </div>

      <div v-loading="deptLoading" class="dept-grid">
        <div v-if="departments.length === 0 && !deptLoading" class="empty-state">
          <el-icon :size="48" color="#c0c4cc"><FolderOpened /></el-icon>
          <p>暂无科室数据</p>
        </div>
        <div
          v-for="dept in departments"
          :key="dept.department"
          class="dept-card"
          @click="goToDoctors(dept.department)"
        >
          <div class="dept-icon">
            <el-icon :size="28" color="#409EFF">
              <component :is="deptIcon(dept.department)" />
            </el-icon>
          </div>
          <div class="dept-info">
            <span class="dept-name">{{ dept.department }}</span>
            <span class="dept-count">{{ dept.doctorCount }} 名医生</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 热门医生推荐 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">
          <el-icon color="#e6a23c"><TrendCharts /></el-icon>
          热门医生推荐
        </h3>
        <el-button type="primary" link @click="$router.push('/patient/hot')">
          查看更多
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div v-loading="doctorLoading" class="doctor-grid">
        <div v-if="hotDoctors.length === 0 && !doctorLoading" class="empty-state">
          <el-icon :size="48" color="#c0c4cc"><UserFilled /></el-icon>
          <p>暂无热门医生</p>
        </div>
        <div
          v-for="doctor in hotDoctors"
          :key="doctor.doctorId"
          class="doctor-card"
          @click="goToSchedule(doctor)"
        >
          <div class="doctor-avatar">
            <el-avatar :size="56" :src="doctor.avatar">
              <el-icon :size="28"><UserFilled /></el-icon>
            </el-avatar>
          </div>
          <div class="doctor-name">{{ doctor.doctorName }}</div>
          <div class="doctor-dept">
            <el-tag size="small" type="primary" effect="plain">
              {{ doctor.departmentName }}
            </el-tag>
          </div>
          <div class="doctor-title">{{ doctor.titleName }}</div>
          <div class="doctor-stats">
            <span class="hot-score">
              <el-icon color="#e6a23c"><StarFilled /></el-icon>
              {{ doctor.hotScore?.toFixed(1) || '-' }}
            </span>
            <span class="eval-count">{{ doctor.evaluationCount || 0 }} 评价</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  OfficeBuilding,
  TrendCharts,
  ArrowRight,
  Service,
  FolderOpened,
  UserFilled,
  StarFilled,
  FirstAidKit,
  Help,
  Headset,
  MagicStick,
  Timer,
  Suitcase,
  Pointer,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getDepartments, getHotDoctors } from '@/api/patient'

const router = useRouter()
const userStore = useUserStore()

const departments = ref([])
const hotDoctors = ref([])
const deptLoading = ref(false)
const doctorLoading = ref(false)

// 科室图标映射
const deptIconMap = {
  '内科': FirstAidKit,
  '外科': Suitcase,
  '妇产科': MagicStick,
  '儿科': Pointer,
  '眼科': Help,
  '耳鼻喉科': Headset,
  '口腔科': Timer,
  '皮肤科': MagicStick,
  '中医科': FirstAidKit,
  '精神科': Headset,
}

const deptIcon = (name) => {
  return deptIconMap[name] || FirstAidKit
}

// 跳转医生列表（带科室筛选）
const goToDoctors = (department) => {
  router.push({ path: '/patient/doctors', query: { department } })
}

// 跳转医生排班日历（携带医生信息）
const goToSchedule = (doctor) => {
  router.push({
    path: `/patient/schedule/${doctor.doctorId}`,
    query: {
      name: doctor.doctorName,
      department: doctor.departmentName,
      title: doctor.titleName,
      specialty: '',
    },
  })
}

// 获取科室列表
const fetchDepartments = async () => {
  deptLoading.value = true
  try {
    const res = await getDepartments()
    departments.value = res.data || []
  } catch {
    // 错误已由拦截器处理
  } finally {
    deptLoading.value = false
  }
}

// 获取热门医生
const fetchHotDoctors = async () => {
  doctorLoading.value = true
  try {
    const res = await getHotDoctors()
    // 只取前 6 位
    hotDoctors.value = (res.data || []).slice(0, 6)
  } catch {
    // 错误已由拦截器处理
  } finally {
    doctorLoading.value = false
  }
}

onMounted(() => {
  fetchDepartments()
  fetchHotDoctors()
})
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* ===== 欢迎横幅 ===== */
.welcome-banner {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 12px;
  padding: 24px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  color: #fff;
}

.welcome-title {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 600;
}

.welcome-desc {
  margin: 0;
  font-size: 14px;
  opacity: 0.85;
}

/* ===== 通用区域 ===== */
.section {
  margin-bottom: 28px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* ===== 科室网格 ===== */
.dept-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 80px;
}

.dept-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.dept-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.15);
  border-color: #409eff;
}

.dept-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.dept-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dept-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.dept-count {
  font-size: 12px;
  color: #909399;
}

/* ===== 热门医生网格 ===== */
.doctor-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 80px;
}

.doctor-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px 16px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.doctor-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.doctor-avatar {
  margin-bottom: 4px;
}

.doctor-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.doctor-dept {
  /* tag 自带样式 */
}

.doctor-title {
  font-size: 12px;
  color: #909399;
}

.doctor-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.hot-score {
  font-size: 13px;
  color: #e6a23c;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 3px;
}

.eval-count {
  font-size: 12px;
  color: #c0c4cc;
}

/* ===== 空状态 ===== */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #c0c4cc;
  font-size: 14px;
}

.empty-state p {
  margin: 10px 0 0;
}

/* ===== 响应式适配 ===== */
@media (max-width: 1024px) {
  .dept-grid,
  .doctor-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .welcome-banner {
    padding: 20px 24px;
  }

  .welcome-title {
    font-size: 18px;
  }

  .dept-grid,
  .doctor-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .welcome-banner {
    padding: 16px 20px;
    border-radius: 8px;
  }

  .welcome-right {
    display: none;
  }

  .dept-grid,
  .doctor-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .dept-card {
    padding: 16px 14px;
  }

  .doctor-card {
    padding: 20px 14px 16px;
  }
}
</style>