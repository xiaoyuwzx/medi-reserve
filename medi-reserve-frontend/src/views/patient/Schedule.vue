<template>
  <div class="schedule-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回医生列表
      </el-button>
    </div>

    <!-- 医生信息卡片 -->
    <div class="doctor-info-card">
      <div class="doctor-avatar">
        <el-avatar :size="64">
          <el-icon :size="32"><UserFilled /></el-icon>
        </el-avatar>
      </div>
      <div class="doctor-detail">
        <h2 class="doctor-name">{{ doctorInfo.name }}</h2>
        <div class="doctor-meta">
          <el-tag size="small" type="primary" effect="plain">
            {{ doctorInfo.department }}
          </el-tag>
          <span class="doctor-title">{{ doctorInfo.title }}</span>
        </div>
        <p class="doctor-specialty" v-if="doctorInfo.specialty">
          擅长：{{ doctorInfo.specialty }}
        </p>
      </div>
    </div>

    <!-- 排班日历 -->
    <div class="schedule-section">
      <h3 class="section-title">排班日历（未来7天）</h3>

      <div v-loading="loading" class="schedule-body">
        <div v-if="groupedList.length === 0 && !loading" class="empty-state">
          <el-icon :size="48" color="#c0c4cc"><Calendar /></el-icon>
          <p>该医生暂无排班</p>
        </div>

        <div class="schedule-grid">
          <div
            v-for="(day, index) in groupedList"
            :key="index"
            class="day-card"
          >
            <div class="day-header">
              <span class="day-date">{{ formatDate(day.date) }}</span>
              <span class="day-week">{{ formatWeek(day.date) }}</span>
            </div>
            <div class="day-slots">
              <div
                v-for="schedule in day.schedules"
                :key="schedule.scheduleId"
                class="slot-row"
                :class="slotStatusClass(schedule.status)"
              >
                <div class="slot-info">
                  <span class="slot-period">{{ schedule.periodText }}</span>
                  <span class="slot-remaining">
                    <span
                      v-if="schedule.status === 1"
                      class="remaining-number"
                    >
                      {{ schedule.remainingCount }}
                    </span>
                    号
                  </span>
                </div>
                <div class="slot-action">
                  <el-tag
                    v-if="schedule.status !== 1"
                    :type="slotTagType(schedule.status)"
                    size="small"
                  >
                    {{ schedule.statusText }}
                  </el-tag>
                  <el-button
                    v-else
                    type="primary"
                    size="small"
                    @click="goToConfirm(schedule.scheduleId)"
                  >
                    预约
                  </el-button>
                </div>
              </div>
              <div v-if="day.schedules.length === 0" class="slot-row slot-empty">
                <span class="no-schedule">当日无排班</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, UserFilled, Calendar } from '@element-plus/icons-vue'
import { getDoctorSchedules } from '@/api/patient'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const scheduleList = ref([])

// 路由参数
const doctorId = route.params.doctorId

// 医生信息（从路由 query 获取）
const doctorInfo = reactive({
  name: route.query.name || '未知医生',
  department: route.query.department || '未知科室',
  title: route.query.title || '',
  specialty: route.query.specialty || '',
})

// 日期格式化：2026-07-17 → 7月17日
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

// 星期格式化
const formatWeek = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weeks[d.getDay()]
}

// 按日期分组
const groupedList = computed(() => {
  const map = new Map()
  scheduleList.value.forEach((s) => {
    const date = s.scheduleDate
    if (!map.has(date)) {
      map.set(date, [])
    }
    map.get(date).push(s)
  })

  // 转换为数组并排序（上午在前）
  const result = []
  map.forEach((schedules, date) => {
    schedules.sort((a, b) => a.period - b.period)
    result.push({ date, schedules })
  })

  // 按日期排序
  result.sort((a, b) => a.date.localeCompare(b.date))
  return result
})

// 槽位状态样式
const slotStatusClass = (status) => {
  if (status === 2) return 'slot-stopped'
  if (status === 3) return 'slot-full'
  return ''
}

// 标签类型
const slotTagType = (status) => {
  if (status === 2) return 'danger'
  return 'info'
}

// 跳转确认页
const goToConfirm = (scheduleId) => {
  router.push(`/patient/confirm/${scheduleId}`)
}

// 获取排班数据
const fetchSchedules = async () => {
  loading.value = true
  try {
    const res = await getDoctorSchedules(doctorId)
    scheduleList.value = res.data || []
  } catch {
    scheduleList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSchedules()
})
</script>

<style scoped>
.schedule-page {
  max-width: 1000px;
  margin: 0 auto;
}

/* ===== 返回栏 ===== */
.back-bar {
  margin-bottom: 12px;
}

/* ===== 医生信息卡片 ===== */
.doctor-info-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
}

.doctor-avatar {
  flex-shrink: 0;
}

.doctor-detail {
  flex: 1;
}

.doctor-name {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.doctor-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.doctor-title {
  font-size: 13px;
  color: #909399;
}

.doctor-specialty {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

/* ===== 排班区域 ===== */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px;
}

.schedule-body {
  min-height: 150px;
}

/* ===== 排班网格 ===== */
.schedule-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.day-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.day-header {
  text-align: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 10px;
}

.day-date {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.day-week {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.day-slots {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.slot-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 6px;
  background: #f5f7fa;
}

.slot-row.slot-stopped {
  background: #fef0f0;
}

.slot-row.slot-full {
  background: #f5f5f5;
}

.slot-row.slot-empty {
  justify-content: center;
  background: transparent;
}

.slot-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.slot-period {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.slot-remaining {
  font-size: 13px;
  color: #909399;
}

.remaining-number {
  color: #409eff;
  font-weight: 600;
}

.slot-action {
  flex-shrink: 0;
}

.no-schedule {
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
  padding: 60px 0;
  color: #c0c4cc;
  font-size: 14px;
}

.empty-state p {
  margin: 12px 0 0;
}

/* ===== 响应式适配 ===== */
@media (max-width: 1024px) {
  .schedule-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .doctor-info-card {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }

  .doctor-meta {
    justify-content: center;
  }

  .schedule-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .doctor-name {
    font-size: 18px;
  }
}
</style>