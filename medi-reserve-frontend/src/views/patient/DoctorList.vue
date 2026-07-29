<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { patientApi } from '@/api/patient'
import { useDictStore } from '@/stores/dict'
import type { DoctorListVO } from '@/api/patient/patientApi'
import DoctorCard from '@/components/patient/DoctorCard.vue'

const router = useRouter()
const dictStore = useDictStore()

const doctorList = ref<DoctorListVO[]>([])
const total = ref(0)
const loading = ref(false)

const queryParams = reactive({
  department: '',
  keyword: '',
  page: 1,
  size: 10
})

let searchTimer: ReturnType<typeof setTimeout> | null = null

async function loadDoctors() {
  loading.value = true
  try {
    const res = await patientApi.patient.getDoctorList({
      department: queryParams.department || undefined,
      keyword: queryParams.keyword || undefined,
      page: queryParams.page,
      size: queryParams.size
    })
    const data = res as unknown as { list?: DoctorListVO[]; total?: number }
    doctorList.value = data.list ?? []
    total.value = data.total ?? 0
  } catch {
    doctorList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 科室变更 */
function onDepartmentChange() {
  queryParams.page = 1
  loadDoctors()
}

/** 关键词搜索（防抖500ms） */
function onKeywordInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    queryParams.page = 1
    loadDoctors()
  }, 500)
}

/** 立即搜索（点击按钮） */
function onSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  queryParams.page = 1
  loadDoctors()
}

/** 分页变更 */
function onPageChange(page: number) {
  queryParams.page = page
  loadDoctors()
}

/** 点击医生卡片 */
function handleNavigate(doctorId: number) {
  const doctor = doctorList.value.find(d => d.doctorId === doctorId)
  router.push({
    name: 'DoctorDetail',
    params: { doctorId },
    state: { doctor }
  })
}

onMounted(() => {
  loadDoctors()
})
</script>

<template>
  <div class="doctor-list-page">
    <!-- 搜索区 -->
    <div class="search-bar">
      <el-select
        v-model="queryParams.department"
        placeholder="全部科室"
        clearable
        style="width: 160px"
        @change="onDepartmentChange"
      >
        <el-option
          v-for="dept in dictStore.departments"
          :key="dept.id"
          :label="dept.department"
          :value="dept.department"
        />
      </el-select>
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索医生姓名或擅长领域"
        clearable
        style="flex: 1; max-width: 360px"
        @input="onKeywordInput"
        @clear="onSearch"
        @keyup.enter="onSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="onSearch">搜索</el-button>
    </div>

    <!-- 医生列表 -->
    <div class="doctor-grid" v-loading="loading">
      <template v-if="doctorList.length > 0">
        <DoctorCard
          v-for="doctor in doctorList"
          :key="doctor.doctorId"
          :doctor="doctor"
          @navigate="handleNavigate"
        />
      </template>
      <el-empty v-else-if="!loading" description="暂无符合条件的医生" />
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
  </div>
</template>

<style scoped>
.doctor-list-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.doctor-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>