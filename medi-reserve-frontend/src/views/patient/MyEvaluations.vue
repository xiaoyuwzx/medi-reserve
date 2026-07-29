<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { patientApi } from '@/api/patient'
import type { MyEvaluationVO } from '@/api/patient/patientApi'

const evaluations = ref<MyEvaluationVO[]>([])
const total = ref(0)
const loading = ref(false)
const deletingId = ref<number | null>(null)

const queryParams = reactive({
  page: 1,
  size: 10
})

async function loadEvaluations() {
  loading.value = true
  try {
    const res = await patientApi.patient.getMyEvaluations({
      page: queryParams.page,
      size: queryParams.size
    })
    const data = res as unknown as { list?: MyEvaluationVO[]; total?: number }
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

async function handleDelete(evaluationId: number) {
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？', '确认删除', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return // 用户取消
  }

  deletingId.value = evaluationId
  try {
    const msg = await patientApi.patient.deleteEvaluation(evaluationId)
    ElMessage.success((msg as unknown as string) || '删除成功')
    loadEvaluations()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败')
  } finally {
    deletingId.value = null
  }
}

onMounted(() => {
  loadEvaluations()
})
</script>

<template>
  <div class="my-evaluations-page">
    <h2 class="page-title">我的评价</h2>

    <div class="evaluation-list" v-loading="loading">
      <template v-if="evaluations.length > 0">
        <div
          v-for="item in evaluations"
          :key="item.evaluationId"
          class="evaluation-item"
        >
          <div class="eval-header">
            <div class="eval-meta">
              <span class="doctor-name">{{ item.doctorName }}</span>
              <span class="dept-name">{{ item.departmentName }}</span>
              <span class="eval-anonymous">{{ item.isAnonymous ? '· 匿名' : '' }}</span>
            </div>
            <div class="eval-status">
              <el-tag
                :type="item.status === 1 ? 'success' : 'info'"
                size="small"
              >
                {{ item.status === 1 ? '已发布' : '已删除' }}
              </el-tag>
            </div>
          </div>

          <div class="eval-score">
            <el-rate :model-value="item.score" disabled show-score size="small" />
          </div>

          <div class="eval-content">
            {{ item.content || '（无文字评价）' }}
          </div>

          <div class="eval-footer">
            <span class="eval-time">{{ item.createdAt }}</span>
            <el-button
              v-if="item.status === 1"
              type="danger"
              size="small"
              text
              :loading="deletingId === item.evaluationId"
              @click="handleDelete(item.evaluationId!)"
            >
              删除
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

      <el-empty v-else-if="!loading" description="暂无评价记录" />
    </div>
  </div>
</template>

<style scoped>
.my-evaluations-page {
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

.evaluation-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.eval-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.eval-meta {
  display: flex;
  align-items: center;
  gap: 8px;
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

.eval-anonymous {
  font-size: 12px;
  color: #909399;
}

.eval-score {
  margin-bottom: 8px;
}

.eval-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.eval-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.eval-time {
  font-size: 12px;
  color: #c0c4cc;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>