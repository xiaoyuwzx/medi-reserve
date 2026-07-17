<template>
  <div class="doctors-page">
    <!-- 顶部筛选区域 -->
    <div class="filter-bar">
      <div class="filter-item">
        <el-select
          v-model="filters.department"
          placeholder="全部科室"
          clearable
          @change="handleSearch"
        >
          <el-option label="全部科室" value="" />
          <el-option
            v-for="dept in departments"
            :key="dept.department"
            :label="`${dept.department}（${dept.doctorCount}）`"
            :value="dept.department"
          />
        </el-select>
      </div>

      <div class="filter-item filter-search">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索医生姓名或擅长领域"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <!-- 医生卡片列表 -->
    <div v-loading="loading" class="doctor-list">
      <div v-if="doctorList.length === 0 && !loading" class="empty-state">
        <el-icon :size="48" color="#c0c4cc"><FolderOpened /></el-icon>
        <p>暂无医生，请调整筛选条件</p>
      </div>

      <div class="doctor-grid">
        <div
          v-for="doctor in doctorList"
          :key="doctor.doctorId"
          class="doctor-card"
          @click="goToSchedule(doctor)"
        >
          <div class="card-top">
            <div class="card-avatar">
              <el-avatar :size="52">
                <el-icon :size="26"><UserFilled /></el-icon>
              </el-avatar>
            </div>
            <div class="card-info">
              <div class="card-name">
                {{ doctor.name }}
                <el-tag
                  :type="doctor.hasAvailableSlot ? 'success' : 'info'"
                  size="small"
                  effect="plain"
                >
                  {{ doctor.hasAvailableSlot ? '有号' : '已满' }}
                </el-tag>
              </div>
              <div class="card-meta">
                <el-tag size="small" type="primary" effect="plain">
                  {{ doctor.department }}
                </el-tag>
                <span class="card-title">{{ doctor.title }}</span>
              </div>
            </div>
          </div>
          <div class="card-specialty" v-if="doctor.specialty">
            <span class="specialty-label">擅长：</span>
            <span class="specialty-text">{{ doctor.specialty }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[5, 10, 15, 20]"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Refresh, FolderOpened, UserFilled } from '@element-plus/icons-vue'
import { getDepartments, getDoctors } from '@/api/patient'

const route = useRoute()
const router = useRouter()

const departments = ref([])
const doctorList = ref([])
const loading = ref(false)
const total = ref(0)

// 筛选条件
const filters = reactive({
  department: '',
  keyword: '',
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
})

// 构建请求参数（空值不传）
const buildParams = () => {
  const params = {
    page: pagination.page,
    size: pagination.size,
  }
  if (filters.department) {
    params.department = filters.department
  }
  if (filters.keyword) {
    params.keyword = filters.keyword
  }
  return params
}

// 获取科室列表
const fetchDepartments = async () => {
  try {
    const res = await getDepartments()
    departments.value = res.data || []
  } catch {
    // 错误由拦截器处理
  }
}

// 获取医生列表
const fetchDoctors = async () => {
  loading.value = true
  try {
    const res = await getDoctors(buildParams())
    // PageInfo 格式：{ total, list, pageNum, pageSize, pages }
    const data = res.data
    doctorList.value = data.list || []
    total.value = data.total || 0
  } catch {
    doctorList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchDoctors()
}

// 重置
const handleReset = () => {
  filters.department = ''
  filters.keyword = ''
  pagination.page = 1
  fetchDoctors()
}

// 分页切换
const handlePageChange = (page) => {
  pagination.page = page
  fetchDoctors()
}

// 每页条数切换
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchDoctors()
}

// 跳转排班日历（携带医生信息）
const goToSchedule = (doctor) => {
  router.push({
    path: `/patient/schedule/${doctor.doctorId}`,
    query: {
      name: doctor.name,
      department: doctor.department,
      title: doctor.title,
      specialty: doctor.specialty || '',
    },
  })
}

onMounted(async () => {
  // 并行加载科室列表
  await fetchDepartments()

  // 如果从首页携带科室参数，自动填入
  if (route.query.department) {
    filters.department = route.query.department
  }

  // 加载医生列表
  fetchDoctors()
})
</script>

<style scoped>
.doctors-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* ===== 筛选栏 ===== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-item {
  flex: 0 0 auto;
}

.filter-search {
  flex: 1;
  min-width: 200px;
}

.filter-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* ===== 医生卡片网格 ===== */
.doctor-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.doctor-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid transparent;
}

.doctor-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.12);
  border-color: #409eff;
}

.card-top {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 12px;
}

.card-avatar {
  flex-shrink: 0;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title {
  font-size: 12px;
  color: #909399;
}

/* ===== 擅长领域 ===== */
.card-specialty {
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
  font-size: 13px;
  line-height: 1.5;
}

.specialty-label {
  color: #909399;
}

.specialty-text {
  color: #606266;
}

/* ===== 分页 ===== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* ===== 空状态 ===== */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #c0c4cc;
  font-size: 14px;
}

.empty-state p {
  margin: 12px 0 0;
}

/* ===== 加载容器最小高度 ===== */
.doctor-list {
  min-height: 200px;
}

/* ===== 响应式适配 ===== */
@media (max-width: 1024px) {
  .doctor-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-search {
    min-width: 100%;
  }

  .filter-actions {
    justify-content: flex-end;
  }

  .doctor-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .doctor-card {
    padding: 16px;
  }

  .card-name {
    font-size: 15px;
  }
}
</style>