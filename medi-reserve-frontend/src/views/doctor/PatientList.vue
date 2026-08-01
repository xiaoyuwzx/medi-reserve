<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi } from '@/api/doctor'
import type { AppointmentListVO } from '@/api/patient/patientApi'

const patients = ref<AppointmentListVO[]>([])
const total = ref(0)
const router = useRouter()
const loading = ref(false)

const today = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const queryParams = reactive({
  date: today(),
  page: 1,
  size: 10
})

async function loadPatients() {
  loading.value = true
  try {
    const res = await doctorApi.doctor.getDoctorAppointments({
      date: queryParams.date || undefined,
      status: 1,
      page: queryParams.page,
      size: queryParams.size
    })
    const data = res as unknown as { list?: AppointmentListVO[]; total?: number }
    patients.value = data.list ?? []
    total.value = data.total ?? 0
  } catch {
    patients.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onDateChange() {
  queryParams.page = 1
  loadPatients()
}

function onPageChange(page: number) {
  queryParams.page = page
  loadPatients()
}

function handleEnterConsultation(appointmentId?: number) {
  if (!appointmentId) {
    ElMessage.warning('预约信息不完整')
    return
  }
  router.push({
    name: 'ConsultationRoom',
    params: { appointmentId }
  })
}

onMounted(() => {
  loadPatients()
})
</script>

<template>
  <div class="patient-list-page">
    <div class="page-header">
      <h2>问诊患者</h2>
    </div>

    <!-- 日期筛选 -->
    <div class="filter-bar">
      <el-date-picker
        v-model="queryParams.date"
        type="date"
        placeholder="选择日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        style="width: 180px"
        @change="onDateChange"
      />
      <span class="filter-hint">默认显示今天的已支付患者</span>
    </div>

    <!-- 患者列表 -->
    <div class="patient-list" v-loading="loading">
      <template v-if="patients.length > 0">
        <div
          v-for="item in patients"
          :key="item.id"
          class="patient-card"
        >
          <div class="card-header">
            <span class="patient-name">{{ item.patientName || '未知患者' }}</span>
            <span class="patient-phone" v-if="item.patientPhone">{{ item.patientPhone }}</span>
            <el-tag type="primary" size="small">待问诊</el-tag>
          </div>
          <div class="card-body">
            <span class="appointment-no">预约单号：{{ item.appointmentNo }}</span>
            <span class="schedule-info">
              📅 {{ item.scheduleDate }} {{ item.periodText }}
            </span>
          </div>
          <div class="card-footer">
            <el-button type="primary" size="small" @click="handleEnterConsultation(item.id)">
              进入问诊室
            </el-button>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="total > queryParams.size" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="queryParams.page"
            :page-size="queryParams.size"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="onPageChange"
          />
        </div>
      </template>

      <el-empty v-else-if="!loading" description="今日暂无问诊患者" />
    </div>
  </div>
</template>

<style scoped>
.patient-list-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-hint {
  font-size: 13px;
  color: #909399;
}

.patient-list {
  min-height: 200px;
}

.patient-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.patient-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.patient-phone {
  font-size: 13px;
  color: #909399;
}

.card-body {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>