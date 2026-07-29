<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { patientApi } from '@/api/patient'
import type { AppointmentListVO } from '@/api/patient/patientApi'

const route = useRoute()
const router = useRouter()

const appointmentId = Number(route.params.appointmentId)
const appointment = (history.state?.appointment as AppointmentListVO) ?? ({} as AppointmentListVO)

const score = ref(0)
const content = ref('')
const isAnonymous = ref(false)
const submitting = ref(false)

function goBack() {
  router.back()
}

async function handleSubmit() {
  if (score.value < 1 || score.value > 5) {
    ElMessage.warning('请选择评分（1-5 星）')
    return
  }
  if (content.value.length > 500) {
    ElMessage.warning('评价内容不能超过 500 字')
    return
  }

  submitting.value = true
  try {
    await patientApi.patient.createEvaluation({
      appointmentId,
      score: score.value,
      content: content.value || undefined,
      isAnonymous: isAnonymous.value
    })
    ElMessage.success('评价成功')
    router.replace({ name: 'MyEvaluations' })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '评价失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // appointment 通过 state 传递
})
</script>

<template>
  <div class="evaluation-create-page">
    <!-- 返回按钮 -->
    <div class="back-row">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <!-- 预约信息卡片 -->
    <div class="info-card" v-if="appointment.doctorName">
      <div class="info-line">
        <span class="label">评价医生</span>
        <span class="value">{{ appointment.doctorName }}</span>
      </div>
      <div class="info-line">
        <span class="label">科室</span>
        <span class="value">{{ appointment.departmentName }}</span>
      </div>
      <div class="info-line">
        <span class="label">就诊日期</span>
        <span class="value">{{ appointment.scheduleDate }} {{ appointment.periodText }}</span>
      </div>
    </div>

    <!-- 评价表单 -->
    <div class="form-card">
      <h3 class="form-title">发表评价</h3>

      <!-- 评分 -->
      <div class="form-item">
        <span class="form-label">评分</span>
        <el-rate v-model="score" :max="5" show-score />
      </div>

      <!-- 评价内容 -->
      <div class="form-item">
        <span class="form-label">评价内容</span>
        <el-input
          v-model="content"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          placeholder="请输入您的评价（选填，最多 500 字）"
        />
      </div>

      <!-- 匿名开关 -->
      <div class="form-item">
        <span class="form-label">匿名评价</span>
        <el-switch v-model="isAnonymous" />
      </div>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '提交评价' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.evaluation-create-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px 16px;
}

.back-row {
  margin-bottom: 16px;
}

.info-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}

.info-line {
  display: flex;
  gap: 12px;
  padding: 4px 0;
}

.info-line .label {
  width: 72px;
  flex-shrink: 0;
  font-size: 13px;
  color: #909399;
}

.info-line .value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.form-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.form-title {
  margin: 0 0 20px;
  font-size: 18px;
  color: #303133;
}

.form-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
}

.form-label {
  width: 72px;
  flex-shrink: 0;
  font-size: 14px;
  color: #606266;
  padding-top: 4px;
}

.form-item :deep(.el-textarea) {
  flex: 1;
}

.form-item :deep(.el-rate) {
  padding-top: 2px;
}

.form-item :deep(.el-switch) {
  padding-top: 4px;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}
</style>