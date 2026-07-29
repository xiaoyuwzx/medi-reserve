<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { patientApi } from '@/api/patient'
import type { ScheduleDetailVO } from '@/api/patient/patientApi'

const route = useRoute()
const router = useRouter()

const scheduleId = Number(route.params.scheduleId)

const detail = ref<ScheduleDetailVO | null>(null)
const loading = ref(false)
const confirming = ref(false)

const isUnavailable = computed(() => {
  if (!detail.value) return true
  return detail.value.status === 2 || detail.value.status === 3 || (detail.value.remainingCount ?? 0) <= 0
})

async function loadDetail() {
  loading.value = true
  try {
    const res = await patientApi.patient.getScheduleDetail(scheduleId)
    detail.value = res as unknown as ScheduleDetailVO
  } catch {
    detail.value = null
    ElMessage.error('加载排班详情失败')
  } finally {
    loading.value = false
  }
}

async function handleConfirm() {
  if (confirming.value || isUnavailable.value) return
  confirming.value = true

  try {
    const createRes = (await patientApi.patient.createAppointment({
      scheduleId
    })) as unknown as Record<string, unknown>

    const appointmentId = createRes.appointmentId as number
    ElMessage.success('挂号成功，请确认支付')
    router.push({
      name: 'PaymentPage',
      query: { appointmentId: String(appointmentId) },
      state: { detail: detail.value }
    })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '创建预约失败')
    confirming.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="confirm-page">
    <!-- 返回按钮 -->
    <div class="back-row">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <!-- 排班详情卡片 -->
    <div class="detail-card" v-loading="loading">
      <template v-if="detail">
        <h2 class="card-title">确认挂号信息</h2>

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
            <span class="label">剩余号源</span>
            <span class="value" :class="{ 'text-danger': (detail.remainingCount ?? 0) <= 3 }">
              {{ detail.remainingCount }} 个
            </span>
          </div>
        </div>

        <!-- 状态提示 -->
        <div v-if="detail.status === 3 || (detail.remainingCount ?? 0) <= 0" class="tip danger">
          ⚠️ 该时段号源已满，无法挂号
        </div>
        <div v-else-if="detail.status === 2" class="tip danger">
          ⚠️ 该医生当前停诊，无法挂号
        </div>
        <div v-else class="tip success">
          ✅ 该时段可挂号，确认后进入支付页面
        </div>

        <!-- 操作按钮 -->
        <div class="action-row">
          <el-button
            type="primary"
            size="large"
            :disabled="isUnavailable"
            :loading="confirming"
            @click="handleConfirm"
          >
            {{ confirming ? '处理中...' : '确认挂号' }}
          </el-button>
          <el-button size="large" @click="goBack">取消</el-button>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="排班信息不存在" />
    </div>
  </div>
</template>

<style scoped>
.confirm-page {
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

.text-danger {
  color: #f56c6c;
}

.tip {
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 20px;
  font-size: 14px;
}

.tip.success {
  background: #f0f9eb;
  color: #67c23a;
}

.tip.danger {
  background: #fef0f0;
  color: #f56c6c;
}

.action-row {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>