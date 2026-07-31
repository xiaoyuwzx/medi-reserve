<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])
const total = ref(0)
const loading = ref(false)

const queryParams = reactive({
  keyword: '',
  status: undefined as number | undefined,
  page: 1,
  size: 10
})

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const patientDetail = ref<Record<string, unknown>>({})

async function loadList() {
  loading.value = true
  try {
    const res = await adminApi.instance.request({
      url: '/admin/patients',
      method: 'GET',
      params: {
        keyword: queryParams.keyword || undefined,
        status: queryParams.status,
        page: queryParams.page,
        size: queryParams.size
      }
    })
    const data = res.data as { list?: Record<string, unknown>[]; total?: number }
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
  queryParams.page = 1
  loadList()
}

function onPageChange(page: number) {
  queryParams.page = page
  loadList()
}

async function handleStatusChange(row: Record<string, unknown>) {
  const newStatus = (row.status as number) === 1 ? 0 : 1
  try {
    const res = await adminApi.instance.request({
      url: `/admin/patients/${row.id}/status`,
      method: 'PATCH',
      data: { status: newStatus }
    })
    ElMessage.success('状态更新成功')
    loadList()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
    loadList()
  }
}

async function viewDetail(patientId: unknown) {
  detailDialogVisible.value = true
  detailLoading.value = true
  try {
    const res = await adminApi.instance.request({
      url: `/admin/patients/${patientId}`,
      method: 'GET'
    })
    patientDetail.value = (res.data as Record<string, unknown>) ?? {}
  } catch {
    patientDetail.value = {}
    ElMessage.error('加载患者详情失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="manage-page">
    <h2 class="page-title">患者管理</h2>

    <!-- 搜索栏 -->
    <div class="filter-bar">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索姓名/手机号"
        clearable
        style="width: 200px"
        @keyup.enter="onSearch"
      />
      <el-select
        v-model="queryParams.status"
        placeholder="全部状态"
        clearable
        style="width: 120px"
      >
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-container" v-loading="loading">
      <el-table :data="list" stripe empty-text="暂无患者数据">
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="idCard" label="身份证号" min-width="180" />
        <el-table-column label="性别" width="70">
          <template #default="{ row }">
            {{ { 0: '未知', 1: '男', 2: '女' }[row.gender as number] || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="() => handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="viewDetail(row.id)">详情</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="患者详情" width="500px">
      <div v-loading="detailLoading">
        <template v-if="patientDetail.name">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="姓名">{{ patientDetail.name }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ patientDetail.phone }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ patientDetail.idCard || '-' }}</el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ { 0: '未知', 1: '男', 2: '女' }[patientDetail.gender as number] || '未知' }}
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ patientDetail.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="patientDetail.status === 1 ? 'success' : 'danger'" size="small">
                {{ patientDetail.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </template>
        <el-empty v-else-if="!detailLoading" description="暂无数据" />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.manage-page {
  max-width: 1200px;
  padding: 24px 0;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
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