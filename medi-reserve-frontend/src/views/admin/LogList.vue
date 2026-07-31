<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const router = useRouter()

const list = ref<Record<string, unknown>[]>([])
const total = ref(0)
const loading = ref(false)

const filterForm = reactive({
  adminId: '',
  module: '',
  result: undefined as string | undefined,
  startDate: '',
  endDate: ''
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

async function loadList() {
  loading.value = true
  try {
    const res = await adminApi.admin.list({
      adminId: filterForm.adminId || undefined,
      module: filterForm.module || undefined,
      result: filterForm.result ? Number(filterForm.result) : undefined,
      startDate: filterForm.startDate || undefined,
      endDate: filterForm.endDate || undefined,
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

function onSearch() {
  queryParams.pageNum = 1
  loadList()
}

function onReset() {
  filterForm.adminId = ''
  filterForm.module = ''
  filterForm.result = undefined
  filterForm.startDate = ''
  filterForm.endDate = ''
  queryParams.pageNum = 1
  loadList()
}

function onPageChange(page: number) {
  queryParams.pageNum = page
  loadList()
}

function viewDetail(id: unknown) {
  router.push({ name: 'AdminLogDetail', params: { id: String(id) } })
}

async function handleDelete(row: Record<string, unknown>) {
  try {
    await ElMessageBox.confirm('确定要删除该日志吗？', '确认删除', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    await adminApi.admin.delete(row.id as number)
    ElMessage.success('删除成功')
    loadList()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="log-page">
    <h2 class="page-title">操作日志</h2>

    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-form :inline="true" :model="filterForm" size="small">
        <el-form-item label="管理员">
          <el-input v-model="filterForm.adminId" placeholder="管理员 ID" style="width: 120px" clearable />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="filterForm.module" placeholder="模块名称" style="width: 140px" clearable />
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="filterForm.result" placeholder="全部" clearable style="width: 100px">
            <el-option label="成功" value="1" />
            <el-option label="失败" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="filterForm.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="filterForm.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <div class="table-container" v-loading="loading">
      <el-table :data="list" stripe size="small" empty-text="暂无日志">
        <el-table-column prop="adminName" label="管理员" min-width="100" />
        <el-table-column prop="module" label="模块" min-width="100" />
        <el-table-column prop="operation" label="操作描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="path" label="请求路径" min-width="160" show-overflow-tooltip />
        <el-table-column label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result === 1 ? 'success' : 'danger'" size="small">
              {{ row.result === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="90">
          <template #default="{ row }">{{ row.durationMs ? `${row.durationMs}ms` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" min-width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" type="primary" @click="viewDetail(row.id)">详情</el-button>
            <el-button text size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
.log-page {
  max-width: 1300px;
  padding: 24px 0;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

.filter-bar {
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
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