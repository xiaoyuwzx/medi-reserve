<script setup lang="ts">
import { computed } from 'vue'
import type { ScheduleCalendarVO } from '@/api/patient/patientApi'

const props = defineProps<{
  schedules: ScheduleCalendarVO[]
  loading: boolean
}>()

const emit = defineEmits<{
  select: [scheduleId: number]
}>()

/** 按日期分组后的结构 */
interface DayGroup {
  date: string
  dayLabel: string
  dayOfWeek: string
  periods: {
    period: number        // 1=上午, 2=下午
    periodText: string
    schedule: ScheduleCalendarVO | undefined
  }[]
}

const weekLabels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

/** 获取未来7天的日期范围作为日历骨架 */
const calendarDays = computed<DayGroup[]>(() => {
  const result: DayGroup[] = []
  const today = new Date()

  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    const dateStr = formatDate(date)
    const dayOfWeek = weekLabels[date.getDay()]

    result.push({
      date: dateStr,
      dayLabel: i === 0 ? '今天' : `${date.getMonth() + 1}/${date.getDate()}`,
      dayOfWeek,
      periods: [
        { period: 1, periodText: '上午', schedule: undefined },
        { period: 2, periodText: '下午', schedule: undefined }
      ]
    })
  }

  // 将真实排班数据填充到对应日期和时段
  for (const s of props.schedules) {
    const day = result.find(d => d.date === s.scheduleDate)
    if (day) {
      const period = day.periods.find(p => p.period === s.period)
      if (period) {
        period.schedule = s
      }
    }
  }

  return result
})

function formatDate(date: Date): string {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function getStatusClass(s: ScheduleCalendarVO | undefined): string {
  if (!s) return 'status-none'
  if (s.status === 1) return 'status-available'
  if (s.status === 3) return 'status-full'
  return 'status-closed'
}

function getStatusText(s: ScheduleCalendarVO | undefined): string {
  if (!s) return '-'
  if (s.status === 1) return `余${s.remainingCount}`
  if (s.status === 3) return '已满'
  return s.statusText || '停诊'
}

function handleSelect(scheduleId: number | undefined) {
  if (scheduleId) {
    emit('select', scheduleId)
  }
}
</script>

<template>
  <div class="calendar" v-loading="loading">
    <div class="calendar-header">排班日历（未来 7 天）</div>
    <div class="calendar-grid">
      <!-- 日期行 -->
      <div
        v-for="day in calendarDays"
        :key="day.date"
        class="day-column"
      >
        <div class="day-header">
          <span class="day-label">{{ day.dayLabel }}</span>
          <span class="day-week">{{ day.dayOfWeek }}</span>
        </div>
        <div
          v-for="p in day.periods"
          :key="`${day.date}-${p.period}`"
          class="period-cell"
          :class="getStatusClass(p.schedule)"
          @click="handleSelect(p.schedule?.scheduleId)"
        >
          <span class="period-name">{{ p.periodText }}</span>
          <span class="period-status">{{ getStatusText(p.schedule) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.calendar {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.calendar-header {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.calendar-grid {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.day-column {
  flex: 1;
  min-width: 90px;
  text-align: center;
}

.day-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.day-label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.day-week {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

.period-cell {
  padding: 8px 4px;
  margin-bottom: 6px;
  border-radius: 6px;
  cursor: default;
  transition: background 0.2s;
  border: 1px solid #ebeef5;
}

.period-cell.status-available {
  background: #f0f9eb;
  border-color: #b7eb8f;
  cursor: pointer;
}

.period-cell.status-available:hover {
  background: #e1f3d8;
}

.period-cell.status-full {
  background: #fef0f0;
  border-color: #fab6b6;
}

.period-cell.status-closed {
  background: #f5f5f5;
  border-color: #e0e0e0;
}

.period-cell.status-none {
  background: #fafafa;
  border-color: #ebeef5;
}

.period-name {
  display: block;
  font-size: 12px;
  color: #606266;
  margin-bottom: 2px;
}

.period-status {
  display: block;
  font-size: 12px;
  font-weight: 600;
}

.status-available .period-status {
  color: #67c23a;
}

.status-full .period-status {
  color: #f56c6c;
}

.status-closed .period-status {
  color: #b0b0b0;
}

.status-none .period-status {
  color: #c0c4cc;
}
</style>