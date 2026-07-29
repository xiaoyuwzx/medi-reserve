<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const status = computed(() => (route.query.status as string) || 'fail')
const appointmentId = computed(() => (route.query.appointmentId as string) || '')

const isSuccess = computed(() => status.value === 'success')

function goHome() {
  router.push({ name: 'PatientHome' })
}

function goMyAppointments() {
  router.push({ name: 'MyAppointments' })
}

function retryPay() {
  if (appointmentId.value) {
    router.replace({
      name: 'AppointmentConfirm',
      params: { scheduleId: '0' }
    })
  }
  router.back()
}
</script>

<template>
  <div class="result-page">
    <!-- 成功状态 -->
    <template v-if="isSuccess">
      <div class="result-icon success">✓</div>
      <h1 class="result-title">支付成功！</h1>
      <p class="result-desc">您的预约已生成，请按时就诊</p>
      <p v-if="appointmentId" class="result-detail">
        预约编号：<strong>{{ appointmentId }}</strong>
      </p>
      <div class="result-actions">
        <el-button type="primary" @click="goMyAppointments">查看我的预约</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
    </template>

    <!-- 失败状态 -->
    <template v-else>
      <div class="result-icon fail">✕</div>
      <h1 class="result-title">支付失败</h1>
      <p class="result-desc">请重试或选择其他医生</p>
      <p v-if="appointmentId" class="result-detail">
        预约编号：<strong>{{ appointmentId }}</strong>
      </p>
      <div class="result-actions">
        <el-button type="primary" @click="retryPay">重新支付</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.result-page {
  max-width: 480px;
  margin: 80px auto 0;
  padding: 48px 24px;
  text-align: center;
}

.result-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 700;
}

.result-icon.success {
  background: #f0f9eb;
  color: #67c23a;
}

.result-icon.fail {
  background: #fef0f0;
  color: #f56c6c;
}

.result-title {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.result-desc {
  margin: 0 0 8px;
  font-size: 15px;
  color: #606266;
}

.result-detail {
  margin: 0 0 32px;
  font-size: 13px;
  color: #909399;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>