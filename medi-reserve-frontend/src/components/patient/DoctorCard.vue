<script setup lang="ts">
import { computed } from 'vue'
import type { DoctorListVO, DoctorHotVO } from '@/api/patient/patientApi'

const props = defineProps<{
  doctor: DoctorListVO | DoctorHotVO
}>()

const emit = defineEmits<{
  navigate: [doctorId: number]
}>()

/** 统一提取显示字段（兼容 DoctorListVO 与 DoctorHotVO） */
const display = computed(() => {
  const d = props.doctor as Record<string, unknown>
  return {
    doctorId: (d.doctorId ?? d.id ?? 0) as number,
    name: (d.name ?? d.doctorName ?? '') as string,
    department: (d.department ?? d.departmentName ?? '') as string,
    title: (d.title ?? d.titleName ?? '') as string,
    specialty: (d.specialty ?? '') as string,
    avatar: (d.avatar ?? '') as string,
    hotScore: (d.hotScore ?? undefined) as number | undefined,
    evaluationCount: (d.evaluationCount ?? undefined) as number | undefined,
    hasAvailableSlot: (d.hasAvailableSlot ?? false) as boolean
  }
})

const handleClick = () => {
  emit('navigate', display.value.doctorId)
}
</script>

<template>
  <div class="doctor-card" @click="handleClick">
    <div class="card-avatar">
      <el-avatar :size="56" :src="display.avatar">
        {{ display.name.charAt(0) }}
      </el-avatar>
    </div>
    <div class="card-body">
      <div class="card-header">
        <span class="doctor-name">{{ display.name }}</span>
        <span class="doctor-title">{{ display.title }}</span>
        <el-tag v-if="display.hasAvailableSlot" size="small" type="success" effect="plain">
          可挂号
        </el-tag>
      </div>
      <div class="card-department">{{ display.department }}</div>
      <div v-if="display.specialty" class="card-specialty">
        {{ display.specialty }}
      </div>
      <div v-if="display.hotScore !== undefined" class="card-hot">
        <span class="hot-score">🔥 {{ display.hotScore.toFixed(1) }}</span>
        <span v-if="display.evaluationCount" class="hot-count">
          {{ display.evaluationCount }} 条评价
        </span>
      </div>
    </div>
    <div class="card-arrow">
      <el-icon><ArrowRight /></el-icon>
    </div>
  </div>
</template>

<style scoped>
.doctor-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: box-shadow 0.2s;
  gap: 12px;
}

.doctor-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
}

.card-avatar {
  flex-shrink: 0;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.doctor-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.doctor-title {
  font-size: 12px;
  color: #909399;
  background: #f0f2f5;
  padding: 1px 6px;
  border-radius: 4px;
}

.card-department {
  font-size: 13px;
  color: #606266;
  margin-bottom: 2px;
}

.card-specialty {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-hot {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.hot-score {
  font-size: 13px;
  font-weight: 600;
  color: #e6a23c;
}

.hot-count {
  font-size: 12px;
  color: #909399;
}

.card-arrow {
  flex-shrink: 0;
  color: #c0c4cc;
  font-size: 16px;
}
</style>