<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { doctorApi } from '@/api/doctor'

const evaluations = ref<Record<string, unknown>[]>([])
const total = ref(0)
const loading = ref(false)

const queryParams = reactive({
  page: 1,
  size: 10
})

async function loadEvaluations() {
  loading.value = true
  try {
    const res = await doctorApi.doctor.getEvaluations({
      page: queryParams.page,
      size: queryParams.size
    })
    const data = res as unknown as { list?: Record<string, unknown>[]; total?: number }
    evaluations.value = data.list ?? []
    total.value = data.total ?? 0
  } catch {
    evaluations.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onPageChange(page: number) {
  queryParams.page = page
  loadEvaluations()
}

onMounted(() => {
  loadEvaluations()
})
</script>

<template>
  <div class="evaluations-page">
    <h2 class="page-title">患者评价</h2>

    <div class="evaluation-list" v-loading="loading">
      <template v-if="evaluations.length > 0">
        <div
          v-for="(item, index) in evaluations"
          :key="index"
          class="eval-item"
        >
          <div class="eval-header">
            <span class="eval-user">{{ item.isAnonymous ? '匿名用户' : ((item.patientName as string) || '患者') }}</span>
            <span class="eval-score">
              <el-rate :model-value="(item.score as number) || 0" disabled size="small" />
            </span>
            <span class="eval-date">{{ item.createdAt as string }}</span>
          </div>
          <div class="eval-content">{{ (item.content as string) || '（无文字评价）' }}</div>
        </div>

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

      <el-empty v-else-if="!loading" description="暂无评价" />
    </div>
  </div>
</template>

<style scoped>
.evaluations-page {
  max-width: 750px;
  margin: 0 auto;
  padding: 24px 16px;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

.evaluation-list {
  min-height: 200px;
}

.eval-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
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

.eval-date {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}

.eval-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-top: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>