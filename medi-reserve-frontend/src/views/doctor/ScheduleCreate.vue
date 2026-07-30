<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi } from '@/api/doctor'

const router = useRouter()

const form = reactive({
  scheduleDate: '',
  period: 1 as 1 | 2,
  maxCount: 20
})

const submitting = ref(false)

// 智能推荐
const recommendLoading = ref(false)
const recommendation = ref<{ recommendedMax: number; difference: number; percentChange: number } | null>(null)

async function fetchRecommendation() {
  if (!form.scheduleDate || !form.period) {
    recommendation.value = null
    return
  }

  recommendLoading.value = true
  try {
    const res = await doctorApi.doctor.getRecommendedMaxCount({
      scheduleDate: form.scheduleDate,
      period: form.period
    })
    const data = res as unknown as { recommendedMax: number; difference: number; percentChange: number }
    recommendation.value = data
  } catch {
    recommendation.value = null
  } finally {
    recommendLoading.value = false
  }
}

// 监听日期和时段的变化，自动获取推荐
watch(
  [() => form.scheduleDate, () => form.period],
  ([newDate, newPeriod]) => {
    if (newDate && newPeriod) {
      fetchRecommendation()
    }
  },
  { immediate: true, deep: true }
)

function applyRecommendation() {
  if (recommendation.value?.recommendedMax) {
    form.maxCount = recommendation.value.recommendedMax
  }
}

async function handleSubmit() {
  if (!form.scheduleDate) {
    ElMessage.warning('请选择排班日期')
    return
  }

  submitting.value = true
  try {
    await doctorApi.doctor.createSchedule({
      scheduleDate: form.scheduleDate,
      period: form.period,
      maxCount: form.maxCount
    })
    ElMessage.success('新增排班成功')
    router.push({ name: 'DoctorSchedules' })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '新增排班失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="create-page">
    <h2 class="page-title">新增排班</h2>

    <div class="form-card">
      <!-- 日期 -->
      <div class="form-item">
        <span class="label">排班日期</span>
        <el-date-picker
          v-model="form.scheduleDate"
          type="date"
          placeholder="请选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :disabled-date="(date: Date) => date < new Date()"
          style="width: 100%"
        />
      </div>

      <!-- 时段 -->
      <div class="form-item">
        <span class="label">时段</span>
        <el-radio-group v-model="form.period">
          <el-radio-button :value="1">上午</el-radio-button>
          <el-radio-button :value="2">下午</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 号源数 -->
      <div class="form-item">
        <span class="label">号源数</span>
        <el-input-number
          v-model="form.maxCount"
          :min="1"
          :max="100"
          style="width: 100%"
        />
      </div>

      <!-- 智能推荐 -->
      <div class="recommend-section" v-loading="recommendLoading">
        <div class="recommend-header">📊 智能推荐</div>
        <div v-if="recommendation && recommendation.recommendedMax" class="recommend-tip">
          <el-tag type="info" size="large">
            推荐号源：{{ recommendation.recommendedMax }} 个
            <span v-if="recommendation.percentChange">
              （{{ recommendation.percentChange > 0 ? '+' : '' }}{{ recommendation.percentChange }}%）
            </span>
          </el-tag>
          <el-button size="small" type="primary" text @click="applyRecommendation">
            填入推荐值
          </el-button>
        </div>
        <span v-else-if="!recommendLoading">选择日期和时段后自动获取</span>
      </div>

      <!-- 提交 -->
      <div class="action-row">
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          提交
        </el-button>
        <el-button @click="router.back()">取消</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.create-page {
  max-width: 500px;
  margin: 0 auto;
  padding: 24px 16px;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

.form-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.form-item {
  margin-bottom: 20px;
}

.form-item .label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.recommend-section {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 24px;
}

.recommend-header {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.recommend-tip {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-row {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>