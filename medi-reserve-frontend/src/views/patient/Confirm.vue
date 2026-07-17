<template>
  <div class="confirm-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回排班日历
      </el-button>
    </div>

    <!-- 页面标题 -->
    <h2 class="page-title">确认预约</h2>

    <div v-loading="loading" class="confirm-body">
      <!-- 错误提示（排班不可预约） -->
      <el-alert
        v-if="schedule && schedule.status !== 1"
        :title="schedule.status === 2 ? '该排班已停诊，无法预约' : '该时段号源已满，无法预约'"
        type="error"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <!-- 就诊信息 -->
      <div v-if="schedule" class="info-card">
        <h3 class="card-title">就诊信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">医生</span>
            <span class="info-value doctor-name">{{ schedule.doctorName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">科室</span>
            <span class="info-value">
              <el-tag size="small" type="primary" effect="plain">
                {{ schedule.departmentName }}
              </el-tag>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">职称</span>
            <span class="info-value">{{ schedule.titleName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">日期</span>
            <span class="info-value">{{ formatFullDate(schedule.scheduleDate) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">时段</span>
            <span class="info-value">
              <el-tag
                :type="schedule.period === 1 ? 'warning' : 'info'"
                size="small"
              >
                {{ schedule.periodText }}
              </el-tag>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">剩余号源</span>
            <span class="info-value remaining">
              <strong>{{ schedule.remainingCount }}</strong> 号
            </span>
          </div>
        </div>
      </div>

      <!-- 患者信息 -->
      <div v-if="userStore.userInfo" class="info-card">
        <h3 class="card-title">患者信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">姓名</span>
            <span class="info-value">{{ userStore.userInfo.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">手机号</span>
            <span class="info-value">{{ maskPhone(userStore.userInfo.phone) }}</span>
          </div>
        </div>
      </div>

      <!-- 提示信息 -->
      <div class="notice">
        <el-icon color="#e6a23c"><WarningFilled /></el-icon>
        <span>确认预约后将生成待支付订单，请在 30 分钟内完成支付。</span>
      </div>

      <!-- 确认按钮 -->
      <el-button
        type="primary"
        size="large"
        class="confirm-btn"
        :loading="submitting"
        :disabled="!canSubmit"
        @click="handleConfirm"
      >
        {{ submitting ? '提交中...' : '确认预约' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, WarningFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getScheduleDetail, createAppointment } from '@/api/patient'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const scheduleId = route.params.scheduleId
const schedule = ref(null)
const loading = ref(false)
const submitting = ref(false)

// 是否可提交（状态正常且未在提交中）
const canSubmit = computed(() => {
  return schedule.value && schedule.value.status === 1 && !submitting.value
})

// 日期格式化：2026-07-17 → 2026年7月17日 周三
const formatFullDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${weeks[d.getDay()]}`
}

// 手机号脱敏：13900139001 → 139****9001
const maskPhone = (phone) => {
  if (!phone || phone.length < 7) return phone
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

// 获取排班详情
const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getScheduleDetail(scheduleId)
    schedule.value = res.data
  } catch {
    // 错误由拦截器处理
  } finally {
    loading.value = false
  }
}

// 提交预约
const handleConfirm = async () => {
  if (!canSubmit.value) return

  submitting.value = true
  try {
    const res = await createAppointment(scheduleId)
    const { appointmentId, appointmentNo } = res.data
    const s = schedule.value
    router.push({
      path: `/patient/pay/${appointmentId}`,
      query: {
        appointmentNo,
        doctorName: s.doctorName,
        departmentName: s.departmentName,
        titleName: s.titleName,
        scheduleDate: s.scheduleDate,
        periodText: s.periodText,
      },
    })
  } catch {
    // 错误由拦截器处理
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.confirm-page {
  max-width: 700px;
  margin: 0 auto;
}

/* ===== 返回栏 ===== */
.back-bar {
  margin-bottom: 8px;
}

/* ===== 页面标题 ===== */
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 24px;
}

/* ===== 内容区 ===== */
.confirm-body {
  min-height: 200px;
}

/* ===== 信息卡片 ===== */
.info-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #909399;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.doctor-name {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.remaining strong {
  color: #409eff;
  font-size: 16px;
}

/* ===== 提示信息 ===== */
.notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 18px;
  background: #fdf6ec;
  border-radius: 8px;
  font-size: 13px;
  color: #e6a23c;
  margin-bottom: 24px;
}

/* ===== 确认按钮 ===== */
.confirm-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  letter-spacing: 4px;
}

/* ===== 响应式适配 ===== */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-card {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }

  .doctor-name {
    font-size: 16px;
  }
}
</style>