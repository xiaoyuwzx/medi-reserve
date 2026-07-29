<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { patientApi } from '@/api/patient'
import type { DoctorHotVO } from '@/api/patient/patientApi'
import DoctorCard from './DoctorCard.vue'

const emit = defineEmits<{
  navigate: [doctorId: number]
}>()

const hotDoctors = ref<DoctorHotVO[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await patientApi.patient.getHotDoctors()
    hotDoctors.value = (res as unknown as DoctorHotVO[]) ?? []
  } catch {
    hotDoctors.value = []
  } finally {
    loading.value = false
  }
})

const handleNavigate = (doctorId: number) => {
  emit('navigate', doctorId)
}
</script>

<template>
  <div class="hot-doctors" v-loading="loading">
    <div class="section-title">🔥 热门医生排行榜</div>
    <div v-if="hotDoctors.length === 0 && !loading" class="empty-tip">
      暂无热门医生数据
    </div>
    <div class="doctor-list">
      <div
        v-for="(doctor, index) in hotDoctors"
        :key="doctor.doctorId"
        class="rank-item"
      >
        <span class="rank-badge" :class="'rank-' + (index + 1)">
          {{ index + 1 <= 3 ? ['🥇', '🥈', '🥉'][index] : index + 1 }}
        </span>
        <DoctorCard :doctor="doctor" @navigate="handleNavigate" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.hot-doctors {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 17px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e6a23c;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 24px 0;
  font-size: 14px;
}

.doctor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-badge {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  border-radius: 50%;
  background: #f5f7fa;
}

.rank-badge.rank-1 {
  background: #fff7e6;
  font-size: 24px;
}

.rank-badge.rank-2 {
  background: #f5f5f5;
  font-size: 22px;
}

.rank-badge.rank-3 {
  background: #fff0f0;
  font-size: 20px;
}

/* DoctorCard 在 rank-item 内撑满 */
.rank-item :deep(.doctor-card) {
  flex: 1;
}
</style>