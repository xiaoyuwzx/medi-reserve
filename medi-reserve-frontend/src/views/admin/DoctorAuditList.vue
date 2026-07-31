<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'

const router = useRouter()

const list = ref<Record<string, unknown>[]>([])
const total = ref(0)
const loading = ref(false)

const queryParams = reactive({
  page: 1,
  size: 10
})

async function loadList() {
  loading.value = true
  try {
    const res = await adminApi.admin.listPending({
      page: queryParams.page,
      size: queryParams.size
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
  queryParams.page = page
  loadList()
}

function viewDetail(id: unknown) {
  router.push({ name: 'AdminDoctorAuditDetail', params: { doctorId: id } })
}

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="audit-list-page">
    <h2 class="page-title">医生审核</h2>

    <div class="table-container" v-loading="loading">
      <el-table :data="list" stripe empty-text="暂无待审核医生">
        <el-table-column label="姓名" min-width="100">
          <template #default="{ row }">
            {{ row.name || row.doctorName || row.realName || row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column label="科室" min-width="120">
          <template #default="{ row }">
            {{ row.departmentName || row.deptName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="职称" min-width="100">
          <template #default="{ row }">
            {{ row.titleName || row.title || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="160">
          <template #default="{ row }">
            {{ row.createdAt || row.createTime || row.registerTime || row.createdTime || row.submitTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="viewDetail(row.doctorId || row.id)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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
    </div>
  </div>
</template>

<style scoped>
.audit-list-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 16px;
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