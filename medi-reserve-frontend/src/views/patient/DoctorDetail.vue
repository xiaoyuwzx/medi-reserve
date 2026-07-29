<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { patientApi } from '@/api/patient'
import type { ScheduleCalendarVO, EvaluationListVO, DoctorListVO } from '@/api/patient/patientApi'
import DoctorCard from '@/components/patient/DoctorCard.vue'
import ScheduleCalendar from '@/components/patient/ScheduleCalendar.vue'

const route = useRoute()
const router = useRouter()

const doctorId = Number(route.params.doctorId)

// 从路由 state 中获取医生基本信息（跳转时传入，减少额外请求）
const doctorInfo = (history.state?.doctor as DoctorListVO) ?? ({} as DoctorListVO)

// 排班数据
const schedules = ref<ScheduleCalendarVO[]>([])
const schedulesLoading = ref(false)

// 评价数据
const evaluations = ref<EvaluationListVO[]>([])
const evalTotal = ref(0)
const evalsLoading = ref(false)
const evalPage = reactive({ page: 1, size: 10 })

async function loadSchedules() {
  schedulesLoading.value = true
  try {
    const res = await patientApi.patient.getScheduleCalendar(doctorId)
    schedules.value = (res as unknown as ScheduleCalendarVO[]) ?? []
  } catch {
    schedules.value = []
  } finally {
    schedulesLoading.value = false
  }
}

async function loadEvaluations() {
  evalsLoading.value = true
  try {
    const res = await patientApi.patient.getDoctorEvaluations(doctorId, {
      page: evalPage.page,
      size: evalPage.size
    })
    const data = res as unknown as { list?: EvaluationListVO[]; total?: number }
    evaluations.value = data.list ?? []
    evalTotal.value = data.total ?? 0
  } catch {
    evaluations.value = []
    evalTotal.value = 0
  } finally {
    evalsLoading.value = false
  }
}

function onScheduleSelect(scheduleId: number) {
  // 跳转到挂号确认页（后续阶段实现，当前先提示）
  router.push({
    name: 'AppointmentConfirm',
    params: { scheduleId }
  }).catch(() => {
    // 路由暂未定义，静默处理
  })
}

function onEvalPageChange(page: number) {
  evalPage.page = page
  loadEvaluations()
}

function goBack() {
  router.push({ name: 'DoctorList' })
}

onMounted(() => {
  loadSchedules()
  loadEvaluations()
})
</script>

<template>
  <div class="doctor-detail-page">
    <!-- 返回按钮 -->
    <div class="back-row">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回医生列表
      </el-button>
    </div>

    <!-- 医生基本信息 -->
    <div class="doctor-info-card" v-if="doctorInfo.doctorId">
      <div class="info-header">
        <el-avatar :size="64">
          {{ (doctorInfo.name || '').charAt(0) }}
        </el-avatar>
        <div class="info-main">
          <h2 class="info-name">{{ doctorInfo.name }}</h2>
          <div class="info-meta">
            <span class="meta-tag">{{ doctorInfo.title }}</span>
            <span class="meta-tag">{{ doctorInfo.department }}</span>
          </div>
        </div>
      </div>
      <div v-if="doctorInfo.specialty" class="info-specialty">
        <span class="label">擅长：</span>{{ doctorInfo.specialty }}
      </div>
    </div>

    <!-- 排班日历 -->
    <ScheduleCalendar
      :schedules="schedules"
      :loading="schedulesLoading"
      @select="onScheduleSelect"
      class="section"
    />

    <!-- 评价列表 -->
    <div class="evaluation-section section" v-loading="evalsLoading">
      <div class="section-title">患者评价（{{ evalTotal }}）</div>
      <template v-if="evaluations.length > 0">
        <div
          v-for="evaluation in evaluations"
          :key="evaluation.evaluationId"
          class="eval-item"
        >
          <div class="eval-header">
            <span class="eval-user">
              {{ evaluation.isAnonymous ? '匿名用户' : evaluation.doctorName?.charAt(0) + '***' }}
            </span>
            <span class="eval-score">
              {{ '★'.repeat(evaluation.score ?? 0) }}{{ '☆'.repeat(5 - (evaluation.score ?? 0)) }}
            </span>
            <span class="eval-date">{{ evaluation.createdAt?.split(' ')[0] ?? evaluation.scheduleDate }}</span>
          </div>
          <div class="eval-content">{{ evaluation.content || '（无文字评价）' }}</div>
        </div>
        <div v-if="evalTotal > evalPage.size" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="evalPage.page"
            :page-size="evalPage.size"
            :total="evalTotal"
            layout="prev, pager, next"
            background
            small
            @current-change="onEvalPageChange"
          />
        </div>
      </template>
      <el-empty v-else-if="!evalsLoading" description="暂无评价" />
    </div>
  </div>
</template>

<style scoped>
.doctor-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px;
}

.back-row {
  margin-bottom: 16px;
}

.doctor-info-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.info-name {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.info-meta {
  display: flex;
  gap: 8px;
  margin-top: 6px;
}

.meta-tag {
  font-size: 12px;
  color: #606266;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.info-specialty {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.info-specialty .label {
  color: #909399;
}

.section {
  margin-bottom: 16px;
}

/* 评价区域 */
.evaluation-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.eval-item {
  padding: 12px 0;
  border-bottom: 1px solid #f2f2f2;
}

.eval-item:last-child {
  border-bottom: none;
}

.eval-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.eval-user {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.eval-score {
  color: #e6a23c;
  font-size: 14px;
}

.eval-date {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}

.eval-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>