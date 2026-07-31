<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const logId = Number(route.params.id)
const detail = ref<Record<string, unknown>>({})
const loading = ref(false)

async function loadDetail() {
  loading.value = true
  try {
    const res = await adminApi.admin.detail(logId)
    detail.value = (res as unknown as Record<string, unknown>) ?? {}
  } catch {
    detail.value = {}
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="log-detail-page">
    <div class="back-row">
      <el-button text @click="router.push({ name: 'AdminLogList' })">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <h2 class="page-title">日志详情</h2>

    <div class="detail-card" v-loading="loading">
      <template v-if="detail.adminName">
        <div class="section">
          <div class="section-title">基本信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="管理员">{{ detail.adminName }}</el-descriptions-item>
            <el-descriptions-item label="模块">{{ detail.module }}</el-descriptions-item>
            <el-descriptions-item label="操作描述" :span="2">{{ detail.operation }}</el-descriptions-item>
            <el-descriptions-item label="请求方法">{{ detail.requestMethod }}</el-descriptions-item>
            <el-descriptions-item label="请求路径">{{ detail.path }}</el-descriptions-item>
            <el-descriptions-item label="客户端 IP">{{ detail.ip || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">请求参数</div>
          <div class="json-box">
            <pre>{{ detail.params ? JSON.stringify(detail.params, null, 2) : '(无参数)' }}</pre>
          </div>
        </div>

        <div class="section">
          <div class="section-title">结果信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="操作结果">
              <el-tag :type="detail.result === 1 ? 'success' : 'danger'" size="small">
                {{ detail.result === 1 ? '成功' : '失败' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="HTTP 状态码">{{ detail.httpStatus || '-' }}</el-descriptions-item>
            <el-descriptions-item label="错误信息" :span="2">
              {{ detail.errorMessage || '无' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">性能信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="操作耗时">
              {{ detail.durationMs ? `${detail.durationMs}ms` : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="操作时间">{{ detail.createdAt || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="日志信息不存在" />
    </div>
  </div>
</template>

<style scoped>
.log-detail-page {
  max-width: 900px;
  padding: 24px 0;
}

.back-row {
  margin-bottom: 8px;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.json-box {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 12px 16px;
  overflow-x: auto;
}

.json-box pre {
  margin: 0;
  font-size: 13px;
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>