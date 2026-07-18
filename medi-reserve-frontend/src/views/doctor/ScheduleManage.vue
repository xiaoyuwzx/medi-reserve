<template>
  <div class="schedule-page">
    <!-- 页面标题 -->
    <h2 class="page-title">排班管理</h2>

    <!-- 查询筛选区域 -->
    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-item">
          <span class="filter-label">日期范围</span>
          <el-date-picker
            v-model="filterDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px;"
          />
        </div>
        <div class="filter-item">
          <span class="filter-label">状态</span>
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 130px;">
            <el-option label="全部" :value="null" />
            <el-option label="正常" :value="1" />
            <el-option label="已停诊" :value="2" />
            <el-option label="已满" :value="3" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新增排班区域 -->
    <div class="add-card">
      <div class="add-header">
        <span class="add-title">新增排班</span>
      </div>
      <div class="add-row">
        <div class="add-item">
          <el-date-picker
            v-model="addForm.scheduleDate"
            type="date"
            placeholder="选择排班日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            style="width: 170px;"
          />
        </div>
        <div class="add-item">
          <el-select v-model="addForm.period" placeholder="选择时段" style="width: 120px;">
            <el-option label="上午" :value="1" />
            <el-option label="下午" :value="2" />
          </el-select>
        </div>
        <div class="add-item">
          <el-input-number
            v-model="addForm.maxCount"
            :min="1"
            :max="100"
            placeholder="最大号源"
            style="width: 140px;"
          />
        </div>
        <div class="add-item">
          <el-button type="warning" plain @click="handleRecommend" :loading="recommendLoading">
            <el-icon><MagicStick /></el-icon>智能推荐
          </el-button>
        </div>
        <div class="add-item">
          <el-button type="primary" @click="handleAdd" :loading="addLoading">
            <el-icon><Plus /></el-icon>提交
          </el-button>
        </div>
      </div>
    </div>

    <!-- 排班列表 -->
    <div v-loading="loading" class="table-card">
      <div v-if="scheduleList.length === 0 && !loading" class="empty-state">
        <el-icon :size="48" color="#c0c4cc"><FolderOpened /></el-icon>
        <p>暂无排班记录</p>
      </div>

      <el-table v-else :data="scheduleList" stripe style="width: 100%;">
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column label="时段" width="80">
          <template #default="{ row }">
            <el-tag :type="row.period === 1 ? 'warning' : 'info'" size="small">
              {{ row.period === 1 ? '上午' : '下午' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxCount" label="最大号源" width="100" align="center" />
        <el-table-column prop="remainingCount" label="剩余号源" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" effect="dark">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="180">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="danger"
              size="small"
              plain
              @click="handleStop(row)"
            >
              停诊
            </el-button>
            <el-button
              v-if="row.status === 2"
              type="success"
              size="small"
              plain
              @click="handleResume(row)"
            >
              恢复
            </el-button>
            <el-button
              type="danger"
              size="small"
              plain
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, MagicStick, Plus, FolderOpened } from '@element-plus/icons-vue'
import { getSchedules, createSchedule, updateScheduleStatus, deleteSchedule, getRecommendedMaxCount } from '@/api/doctor'

// ----- 筛选 -----
const filterDateRange = ref([])
const filterStatus = ref(null)

// ----- 新增表单 -----
const addForm = reactive({
  scheduleDate: '',
  period: 1,
  maxCount: 20,
})
const addLoading = ref(false)
const recommendLoading = ref(false)

// ----- 列表 -----
const scheduleList = ref([])
const loading = ref(false)

// 禁用今天之前的日期
const disabledDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}

// 状态映射
const statusMap = { 1: '正常', 2: '已停诊', 3: '已满' }
const statusTagMap = { 1: 'success', 2: 'danger', 3: 'info' }
const statusText = (s) => statusMap[s] ?? '未知'
const statusTagType = (s) => statusTagMap[s] ?? 'info'

// 时间格式化
const formatDateTime = (str) => {
  if (!str) return '-'
  return str.replace('T', ' ').substring(0, 16)
}

// ----- 查询 -----
const fetchList = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterDateRange.value?.length === 2) {
      params.startDate = filterDateRange.value[0]
      params.endDate = filterDateRange.value[1]
    }
    if (filterStatus.value !== null) {
      params.status = filterStatus.value
    }
    const res = await getSchedules(params)
    scheduleList.value = res.data || []
  } catch {
    scheduleList.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => fetchList()
const handleReset = () => {
  filterDateRange.value = []
  filterStatus.value = null
  fetchList()
}

// ----- 新增 -----
const handleRecommend = async () => {
  if (!addForm.scheduleDate) {
    ElMessage.warning('请先选择排班日期')
    return
  }
  recommendLoading.value = true
  try {
    const res = await getRecommendedMaxCount(addForm.scheduleDate, addForm.maxCount)
    const { recommendedMax } = res.data
    addForm.maxCount = recommendedMax
    ElMessage.success(`智能推荐：${recommendedMax} 号`)
  } catch {
    // 错误已由拦截器处理
  } finally {
    recommendLoading.value = false
  }
}

const handleAdd = async () => {
  if (!addForm.scheduleDate) {
    ElMessage.warning('请选择排班日期')
    return
  }
  addLoading.value = true
  try {
    await createSchedule({
      scheduleDate: addForm.scheduleDate,
      period: addForm.period,
      maxCount: addForm.maxCount,
    })
    ElMessage.success('排班创建成功')
    fetchList()
  } catch {
    // 错误已由拦截器处理
  } finally {
    addLoading.value = false
  }
}

// ----- 停诊 / 恢复 -----
const handleStop = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认停诊 ${row.scheduleDate} ${row.period === 1 ? '上午' : '下午'} 的排班？`,
      '停诊确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await updateScheduleStatus(row.id, 2)
    ElMessage.success('停诊操作成功')
    fetchList()
  } catch {
    // 错误已由拦截器处理
  }
}

const handleResume = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认恢复 ${row.scheduleDate} ${row.period === 1 ? '上午' : '下午'} 的排班？`,
      '恢复确认',
      { type: 'info' },
    )
  } catch {
    return
  }
  try {
    await updateScheduleStatus(row.id, 1)
    ElMessage.success('恢复接诊操作成功')
    fetchList()
  } catch {
    // 错误已由拦截器处理
  }
}

// ----- 删除 -----
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除 ${row.scheduleDate} ${row.period === 1 ? '上午' : '下午'} 的排班？删除后不可恢复。`,
      '删除确认',
      { type: 'error', confirmButtonText: '确认删除' },
    )
  } catch {
    return
  }
  try {
    await deleteSchedule(row.id)
    ElMessage.success('排班删除成功')
    fetchList()
  } catch {
    // 错误已由拦截器处理
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.schedule-page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px;
}

/* ===== 筛选卡片 ===== */
.filter-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  color: #606266;
  flex-shrink: 0;
}

.filter-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

/* ===== 新增卡片 ===== */
.add-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.add-header {
  margin-bottom: 12px;
}

.add-title {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
}

.add-row {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  min-height: 200px;
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  color: #c0c4cc;
  font-size: 14px;
}

.empty-state p {
  margin: 12px 0 0;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-actions {
    margin-left: 0;
    justify-content: flex-end;
  }

  .add-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>