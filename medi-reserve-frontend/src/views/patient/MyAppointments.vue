<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { patientApi } from '@/api/patient'
import type { AppointmentListVO } from '@/api/patient/patientApi'

const router = useRouter()

const appointments = ref<AppointmentListVO[]>([])
const total = ref(0)
const loading = ref(false)
const payingId = ref<number | null>(null)

const queryParams = reactive<{
  status: number | undefined
  page: number
  size: number
}>({
  status: undefined,
  page: 1,
  size: 10
})

const statusTabs = [
  { label: '全部', value: undefined },
  { label: '待支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已就诊', value: 2 },
  { label: '已取消', value: 3 },
  { label: '已过期', value: 4 }
]

const statusMap: Record<number, { text: string; type: 'warning' | 'primary' | 'success' | 'info' | 'danger' }> = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'primary' },
  2: { text: '已就诊', type: 'success' },
  3: { text: '已取消', type: 'info' },
  4: { text: '已过期', type: 'danger' }
}

async function loadAppointments() {
  loading.value = true
  try {
    const res = await patientApi.patient.getMyAppointments({
      status: queryParams.status,
      page: queryParams.page,
      size: queryParams.size
    })
    const data = res as unknown as { list?: AppointmentListVO[]; total?: number }
    appointments.value = data.list ?? []
    total.value = data.total ?? 0
  } catch {
    appointments.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  queryParams.page = 1
  loadAppointments()
}

function onPageChange(page: number) {
  queryParams.page = page
  loadAppointments()
}

function handlePay(appointment: AppointmentListVO) {
  const detail = {
    doctorName: appointment.doctorName,
    departmentName: appointment.departmentName,
    titleName: appointment.titleName || '',
    scheduleDate: appointment.scheduleDate,
    periodText: appointment.periodText
  }
  router.push({
    name: 'PaymentPage',
    query: { appointmentId: String(appointment.id) },
    state: { detail }
  })
}

function handleEnterRoom(appointment: AppointmentListVO) {
  router.push({
    name: 'ConsultationRoom',
    params: { appointmentId: appointment.id }
  })
}

function handleEvaluate(appointment: AppointmentListVO) {
  router.push({
    name: 'EvaluationCreate',
    params: { appointmentId: appointment.id },
    state: { appointment }
  })
}

onMounted(() => {
  loadAppointments()
})
</script>

<template>
  <div class="appointments-page">
    <h2 class="page-title">我的预约</h2>

    <!-- 状态筛选 Tabs -->
    <el-tabs
      :model-value="String(queryParams.status ?? 'all')"
      @update:model-value="(val: string) => {
        queryParams.status = val === 'all' ? undefined : Number(val)
        onTabChange()
      }"
      class="status-tabs"
    >
      <el-tab-pane
        v-for="tab in statusTabs"
        :key="String(tab.value ?? 'all')"
        :label="tab.label"
        :name="String(tab.value ?? 'all')"
      />
    </el-tabs>

    <!-- 预约列表 -->
    <div class="appointment-list" v-loading="loading">
      <template v-if="appointments.length > 0">
        <div
          v-for="item in appointments"
          :key="item.id"
          class="appointment-item"
        >
          <div class="item-header">
            <span class="doctor-name">{{ item.doctorName }}</span>
            <span class="dept-name">{{ item.departmentName }}</span>
            <span class="title-name">{{ item.titleName }}</span>
          </div>
          <div class="item-body">
            <span class="schedule-info">
              📅 {{ item.scheduleDate }} {{ item.periodText }}
            </span>
            <span class="appointment-no">单号: {{ item.appointmentNo }}</span>
          </div>
          <div class="item-footer">
            <el-tag
              :type="statusMap[item.status ?? 0]?.type ?? 'info'"
              size="small"
            >
              {{ statusMap[item.status ?? 0]?.text ?? '未知' }}
            </el-tag>
            <div class="item-actions">
              <el-button
                v-if="item.status === 0"
                type="warning"
                size="small"
                :loading="payingId === item.id"
                @click="handlePay(item)"
              >
                去支付
              </el-button>
              <el-button
                v-if="item.status === 1"
                type="primary"
                size="small"
                @click="handleEnterRoom(item)"
              >
                进入问诊室
              </el-button>
              <el-button
                v-if="item.status === 2"
                type="success"
                size="small"
                @click="handleEvaluate(item)"
              >
                去评价
              </el-button>
            </div>
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

      <el-empty v-else-if="!loading" description="暂无预约记录" />
    </div>
  </div>
</template>

<style scoped>
.appointments-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;
}

.page-title {
  margin: 0 0 16px;
  font-size: 20px;
  color: #303133;
}

.status-tabs {
  margin-bottom: 16px;
}

.appointment-list {
  min-height: 200px;
}

.appointment-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.doctor-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.dept-name {
  font-size: 13px;
  color: #606266;
  background: #f0f2f5;
  padding: 1px 6px;
  border-radius: 4px;
}

.title-name {
  font-size: 12px;
  color: #909399;
}

.item-body {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 10px;
}

.schedule-info {
  font-size: 14px;
  color: #606266;
}

.appointment-no {
  font-size: 12px;
  color: #909399;
}

.item-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.item-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>