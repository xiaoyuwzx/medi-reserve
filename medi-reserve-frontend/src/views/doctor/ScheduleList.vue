<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { doctorApi } from '@/api/doctor'
import type { Schedule } from '@/api/doctor/doctorApi'
import ScheduleCard from '@/components/doctor/ScheduleCard.vue'

const router = useRouter()

const schedules = ref<Schedule[]>([])
const loading = ref(false)

const queryParams = reactive({
  startDate: '',
  endDate: '',
  status: undefined as number | undefined
})

async function loadSchedules() {
  loading.value = true
  try {
    const res = await doctorApi.doctor.listSchedules({
      startDate: queryParams.startDate || undefined,
      endDate: queryParams.endDate || undefined,
      status: queryParams.status
    })
    const data = res as unknown as Schedule[]
    schedules.value = data ?? []
  } catch {
    schedules.value = []
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  loadSchedules()
}

function onSearch() {
  loadSchedules()
}

async function handleToggle(schedule: Schedule) {
  const newStatus = schedule.status === 1 ? 2 : 1
  const actionText = newStatus === 2 ? '停诊' : '恢复'
  try {
    await ElMessageBox.confirm(`确定要${actionText}该排班吗？`, '操作确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    const msg = await doctorApi.doctor.updateScheduleStatus(schedule.scheduleId ?? 0, {
      status: newStatus
    })
    ElMessage.success((msg as unknown as string) || `${actionText}成功`)
    loadSchedules()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : `${actionText}失败`)
  }
}

async function handleDelete(schedule: Schedule) {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？此操作不可恢复。', '确认删除', {
      type: 'error',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    const msg = await doctorApi.doctor.deleteSchedule(schedule.scheduleId ?? 0)
    ElMessage.success((msg as unknown as string) || '删除成功')
    loadSchedules()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => {
  loadSchedules()
})
</script>

<template>
  <div class="schedule-list-page">
    <div class="page-header">
      <h2>排班管理</h2>
      <el-button type="primary" @click="router.push({ name: 'DoctorScheduleCreate' })">
        新增排班
      </el-button>
    </div>

    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-date-picker
        v-model="queryParams.startDate"
        type="date"
        placeholder="开始日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        style="width: 160px"
        @change="onFilterChange"
      />
      <span class="sep">-</span>
      <el-date-picker
        v-model="queryParams.endDate"
        type="date"
        placeholder="结束日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        style="width: 160px"
        @change="onFilterChange"
      />
      <el-select
        v-model="queryParams.status"
        placeholder="全部状态"
        clearable
        style="width: 120px"
        @change="onFilterChange"
      >
        <el-option label="正常" :value="1" />
        <el-option label="停诊" :value="2" />
        <el-option label="已满" :value="3" />
      </el-select>
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <!-- 排班列表 -->
    <div class="schedule-list" v-loading="loading">
      <ScheduleCard
        v-for="item in schedules"
        :key="item.scheduleId"
        :schedule="item"
        @toggle="handleToggle"
        @delete="handleDelete"
      />
      <el-empty v-if="schedules.length === 0 && !loading" description="暂无排班记录" />
    </div>
  </div>
</template>

<style scoped>
.schedule-list-page {
  max-width: 900px;
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
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.sep {
  color: #909399;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 200px;
}
</style>