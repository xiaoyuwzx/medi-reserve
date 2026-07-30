<script setup lang="ts">
import type { Schedule } from '@/api/doctor/doctorApi'

defineProps<{
  schedule: Schedule
  loading?: boolean
}>()

const emit = defineEmits<{
  toggle: [schedule: Schedule]
  delete: [schedule: Schedule]
}>()

const statusMap: Record<number, { text: string; type: 'success' | 'danger' | 'warning' }> = {
  1: { text: '正常', type: 'success' },
  2: { text: '停诊', type: 'danger' },
  3: { text: '已满', type: 'warning' }
}
</script>

<template>
  <div class="schedule-card" v-loading="loading">
    <div class="card-left">
      <span class="card-date">{{ schedule.scheduleDate }}</span>
      <span class="card-period">{{ schedule.periodText }}</span>
    </div>
    <div class="card-center">
      <span class="card-count">
        号源：<strong>{{ schedule.remainingCount ?? 0 }}</strong> / {{ schedule.maxCount ?? 0 }}
      </span>
    </div>
    <div class="card-right">
      <el-tag
        :type="statusMap[schedule.status ?? 1]?.type ?? 'info'"
        size="small"
      >
        {{ schedule.statusText || statusMap[schedule.status ?? 1]?.text || '未知' }}
      </el-tag>
      <el-button
        v-if="schedule.status === 1"
        type="warning"
        size="small"
        plain
        @click="emit('toggle', schedule)"
      >
        停诊
      </el-button>
      <el-button
        v-if="schedule.status === 2"
        type="success"
        size="small"
        plain
        @click="emit('toggle', schedule)"
      >
        恢复
      </el-button>
      <el-button
        type="danger"
        size="small"
        plain
        @click="emit('delete', schedule)"
      >
        删除
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.schedule-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  gap: 16px;
}

.card-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 110px;
}

.card-date {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.card-period {
  font-size: 12px;
  color: #909399;
}

.card-center {
  flex: 1;
}

.card-count {
  font-size: 13px;
  color: #606266;
}

.card-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
</style>