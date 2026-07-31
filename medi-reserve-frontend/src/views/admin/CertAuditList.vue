<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'

const router = useRouter()

const list = ref<Record<string, unknown>[]>([])
const total = ref(0)
const loading = ref(false)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

async function loadList() {
  loading.value = true
  try {
    const res = await adminApi.admin.listCertPending({
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    const data = res as unknown as { list?: Record<string, unknown>[]; total?: number }
    list.value = data.list ?? []
    total.value = data.total ?? 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onPageChange(page: number) {
  queryParams.pageNum = page
  loadList()
}

function viewDetail(doctorId: unknown) {
  router.push({ name: 'AdminCertAuditDetail', params: { doctorId: String(doctorId) } })
}

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="cert-audit-page">
    <h2 class="page-title">证件审核</h2>

    <div class="table-container" v-loading="loading">
      <el-table :data="list" stripe empty-text="暂无待审核证件">
        <el-table-column prop="doctorName" label="医生姓名" min-width="100" />
        <el-table-column prop="departmentName" label="科室" min-width="120" />
        <el-table-column prop="titleName" label="职称" min-width="100" />
        <el-table-column prop="submittedAt" label="提交时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="viewDetail(row.doctorId)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > queryParams.pageSize" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          :page-size="queryParams.pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.cert-audit-page {
  max-width: 1000px;
  padding: 24px 0;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>