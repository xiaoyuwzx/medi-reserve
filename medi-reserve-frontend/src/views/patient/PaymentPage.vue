<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { patientApi } from '@/api/patient'
import type { ScheduleDetailVO } from '@/api/patient/patientApi'

const route = useRoute()
const router = useRouter()

const appointmentId = Number(route.query.appointmentId)
const loading = ref(false)

// 尝试从 history.state 获取 detail，失败则 fallback 查询
const stateDetail = (history.state?.detail as ScheduleDetailVO | undefined) ?? null
const detail = ref<ScheduleDetailVO | null>(stateDetail)

const paying = ref(false)

const hasDetail = computed(() => !!detail.value?.doctorName)

async function fetchAppointmentDetail() {
  if (detail.value?.doctorName) return
  if (!appointmentId) return

  loading.value = true
  try {
    const res = await patientApi.patient.getMyAppointments({
      status: undefined,
      page: 1,
      size: 100
    })
    const data = res as unknown as { list?: Record<string, unknown>[] }
    const found = data.list?.find(
      (item: Record<string, unknown>) => item.id === appointmentId
    )
    if (found) {
      detail.value = {
        doctorName: found.doctorName as string,
        departmentName: found.departmentName as string,
        titleName: (found.titleName as string) || '',
        scheduleDate: found.scheduleDate as string,
        periodText: found.periodText as string,
        price: (found.price as number) || 10
      }
    }
  } catch {
    // 静默处理
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  if (paying.value) return
  paying.value = true
  try {
    const payMsg = await patientApi.patient.payAppointment(appointmentId)
    ElMessage.success((payMsg as unknown as string) || '支付成功')
    router.replace({
      name: 'PaymentResult',
      query: { status: 'success', appointmentId: String(appointmentId) }
    })
  } catch {
    router.replace({
      name: 'PaymentResult',
      query: { status: 'fail', appointmentId: String(appointmentId) }
    })
  } finally {
    paying.value = false
  }
}

function handleCancel() {
  router.push({ name: 'MyAppointments' })
}

function goBack() {
  router.back()
}

onMounted(() => {
  if (!detail.value?.doctorName && appointmentId) {
    fetchAppointmentDetail()
  }
})
</script>

<template>
  <div class="payment-page">
    <!-- 返回按钮 -->
    <div class="back-row">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <!-- 就诊信息卡片 -->
    <div class="detail-card" v-loading="loading" v-if="hasDetail">
      <h2 class="card-title">确认支付</h2>

      <div class="info-grid">
        <div class="info-item">
          <span class="label">就诊医生</span>
          <span class="value">{{ detail.doctorName }}</span>
        </div>
        <div class="info-item">
          <span class="label">科室</span>
          <span class="value">{{ detail.departmentName }}</span>
        </div>
        <div class="info-item">
          <span class="label">职称</span>
          <span class="value">{{ detail.titleName }}</span>
        </div>
        <div class="info-item">
          <span class="label">就诊日期</span>
          <span class="value">{{ detail.scheduleDate }}</span>
        </div>
        <div class="info-item">
          <span class="label">时段</span>
          <span class="value">{{ detail.periodText }}</span>
        </div>
        <div class="info-item">
          <span class="label">预约编号</span>
          <span class="value">{{ appointmentId }}</span>
        </div>
        <div class="info-item">
          <span class="label">挂号费</span>
          <span class="value">{{ detail.price ? `¥${detail.price.toFixed(2)}` : '¥10.00' }}</span>
        </div>
      </div>

      <div class="tip info">
        💰 挂号费：系统将模拟微信支付完成付款
      </div>

      <!-- 操作按钮 -->
      <div class="action-row">
        <el-button
          type="primary"
          size="large"
          :loading="paying"
          @click="handlePay"
        >
          {{ paying ? '支付中...' : '确认支付' }}
        </el-button>
        <el-button size="large" @click="handleCancel">取消</el-button>
      </div>

      <div class="cancel-hint">
        取消后可稍后在「我的预约」中继续支付
      </div>
    </div>

    <el-empty v-else description="支付信息不存在" />
  </div>
</template>

<style scoped>
.payment-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px 16px;
}

.back-row {
  margin-bottom: 16px;
}

.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.card-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
  text-align: center;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.label {
  font-size: 13px;
  color: #909399;
}

.value {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.tip {
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 20px;
  font-size: 14px;
}

.tip.info {
  background: #ecf5ff;
  color: #409eff;
}

.action-row {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.cancel-hint {
  text-align: center;
  margin-top: 16px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>