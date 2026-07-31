<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '@/api/admin'

// ========== 总览统计 ==========
const overviewLoading = ref(false)
const overview = ref<Record<string, unknown>>({})

async function loadOverview() {
  overviewLoading.value = true
  try {
    const res = await adminApi.admin.getOverview()
    overview.value = (res as unknown as Record<string, unknown>) ?? {}
  } catch {
    overview.value = {}
  } finally {
    overviewLoading.value = false
  }
}

const overviewCards = [
  { key: 'todayAppointments', label: '今日挂号', icon: '📋' },
  { key: 'todayPaid', label: '今日支付', icon: '💰' },
  { key: 'todayIncome', label: '今日收入', icon: '💵', format: (v: any) => `¥${(Number(v) || 0).toFixed(2)}` },
  { key: 'totalAppointments', label: '历史总挂号', icon: '📊' },
  { key: 'totalIncome', label: '历史总收入', icon: '🏦', format: (v: any) => `¥${(Number(v) || 0).toFixed(2)}` },
  { key: 'totalPatients', label: '总患者数', icon: '👥' },
  { key: 'totalDoctors', label: '总医生数', icon: '🩺' },
  { key: 'totalEvaluations', label: '总评价数', icon: '⭐' }
]

// ========== 趋势图 ==========
const trendLoading = ref(false)
const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null
const daysOptions = [
  { label: '7 天', value: 7 },
  { label: '14 天', value: 14 },
  { label: '30 天', value: 30 }
]
const selectedDays = ref(7)

watch(selectedDays, () => loadTrend())

async function loadTrend() {
  trendLoading.value = true
  try {
    const res = await adminApi.admin.getTrend({ days: selectedDays.value })
    const data = (res as unknown as Record<string, number | string>[]) ?? []
    renderTrendChart(data)
  } catch {
    renderTrendChart([])
  } finally {
    trendLoading.value = false
  }
}

function renderTrendChart(data: Record<string, number | string>[]) {
  if (!chartRef.value) return
  if (!chartInstance) chartInstance = echarts.init(chartRef.value)

  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['挂号量', '支付量', '收入'], bottom: 0 },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.date || d.day || ''),
      axisLabel: { rotate: 30 }
    },
    yAxis: [
      { type: 'value', name: '数量', minInterval: 1 },
      { type: 'value', name: '收入 (元)', min: 0 }
    ],
    series: [
      {
        name: '挂号量',
        type: 'line',
        data: data.map((d) => Number(d.appointments ?? d.count ?? 0)),
        smooth: true,
        lineStyle: { color: '#409eff' },
        itemStyle: { color: '#409eff' }
      },
      {
        name: '支付量',
        type: 'line',
        data: data.map((d) => Number(d.paid ?? 0)),
        smooth: true,
        lineStyle: { color: '#67c23a' },
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '收入',
        type: 'line',
        yAxisIndex: 1,
        data: data.map((d) => Number(d.income ?? 0)),
        smooth: true,
        lineStyle: { color: '#e6a23c' },
        itemStyle: { color: '#e6a23c' }
      }
    ],
    grid: { left: 55, right: 55, top: 20, bottom: 40 }
  })
}

// ========== 科室排行 ==========
const deptLoading = ref(false)
const deptList = ref<Record<string, unknown>[]>([])
async function loadDeptRanking() {
  deptLoading.value = true
  try {
    const res = await adminApi.admin.getDepartmentRanking({ limit: 10 })
    deptList.value = (res as unknown as Record<string, unknown>[]) ?? []
    nextTick(() => renderDeptPieChart(deptList.value))
  } catch {
    deptList.value = []
  } finally {
    deptLoading.value = false
  }
}

// ========== 医生排行 ==========
const doctorLoading = ref(false)
const doctorList = ref<Record<string, unknown>[]>([])
const sortBy = ref('appointmentCount')

watch(sortBy, () => loadDoctorRanking())

async function loadDoctorRanking() {
  doctorLoading.value = true
  try {
    const res = await adminApi.admin.getDoctorRanking({ limit: 10, sortBy: sortBy.value })
    doctorList.value = (res as unknown as Record<string, unknown>[]) ?? []
  } catch {
    doctorList.value = []
  } finally {
    doctorLoading.value = false
  }
}

// ========== 饼图 ==========
const pieLoading = ref(false)
const pieRef = ref<HTMLDivElement>()
let pieInstance: echarts.ECharts | null = null

async function loadPie() {
  pieLoading.value = true
  try {
    const res = await adminApi.admin.getStatusDistribution()
    const rawData = (res as unknown as { status: number; label: string; count: number }[]) ?? []
    const pieData = rawData.map(item => ({
      name: item.label,
      value: item.count
    }))
    renderPieChart(pieData)
  } catch {
    renderPieChart([])
  } finally {
    pieLoading.value = false
  }
}

function renderPieChart(data: { name: string; value: number }[]) {
  if (!pieRef.value) return
  if (!pieInstance) pieInstance = echarts.init(pieRef.value)
  pieInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: data,
      label: { formatter: '{b}: {c}' }
    }],
    color: ['#f56c6c', '#409eff', '#67c23a', '#909399']
  })
}

// ========== 科室饼图 ==========
const pieDeptRef = ref<HTMLDivElement>()
let pieDeptInstance: echarts.ECharts | null = null

function renderDeptPieChart(data: Record<string, unknown>[]) {
  if (!pieDeptRef.value) return

  // 过滤掉挂号量为0的科室，并确保数值为数字类型
  const filteredData = data.filter(item => {
    const count = Number(item.appointmentCount) || 0
    return count > 0
  })

  if (!pieDeptInstance) pieDeptInstance = echarts.init(pieDeptRef.value)

  if (filteredData.length === 0) {
    pieDeptInstance.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: { color: '#909399', fontSize: 14 }
      },
      series: [{ type: 'pie', data: [], label: { show: false } }]
    })
    return
  }

  const pieData = filteredData.map((item) => ({
    name: (item.departmentName as string) || '未知科室',
    value: Number(item.appointmentCount) || 0
  }))

  pieDeptInstance.setOption({
    title: undefined,
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      data: pieData,
      label: { formatter: '{b}\n{d}%' }
    }],
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#b37feb', '#5cdbd3', '#85a5ff', '#ffc069', '#95de64']
  })
}

// ========== 全局 ==========
function onResize() {
  chartInstance?.resize()
  pieInstance?.resize()
  pieDeptInstance?.resize()
}

onMounted(async () => {
  await loadOverview()
  nextTick(() => {
    loadTrend()
    loadPie()
  })
  loadDeptRanking()
  loadDoctorRanking()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  chartInstance?.dispose(); chartInstance = null
  pieDeptInstance?.dispose(); pieDeptInstance = null
  pieInstance?.dispose(); pieInstance = null
})
</script>

<template>
  <div class="dashboard">
    <h2 class="page-title">数据统计看板</h2>

    <!-- 总览卡片 -->
    <div class="overview-section" v-loading="overviewLoading">
      <el-row :gutter="16">
        <el-col
          :span="24" :sm="12" :md="8" :lg="6"
          v-for="card in overviewCards" :key="card.key"
        >
          <div class="overview-card">
            <div class="card-icon">{{ card.icon }}</div>
            <div class="card-value">
              {{ card.format ? card.format(overview[card.key]) : (overview[card.key] ?? 0) }}
            </div>
            <div class="card-label">{{ card.label }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 趋势图 -->
    <div class="chart-section" v-loading="trendLoading">
      <div class="section-header">
        <span>趋势分析</span>
        <el-radio-group v-model="selectedDays" size="small">
          <el-radio-button v-for="opt in daysOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </el-radio-button>
        </el-radio-group>
      </div>
      <div ref="chartRef" class="chart-box"></div>
    </div>

    <el-row :gutter="16">
      <!-- 科室排行 -->
      <el-col :span="12">
        <div class="table-section" v-loading="deptLoading">
          <div class="section-header">科室排行</div>
          <el-table :data="deptList" stripe size="small" empty-text="暂无数据">
            <el-table-column prop="departmentName" label="科室" />
            <el-table-column prop="appointmentCount" label="挂号量" width="120" />
            <el-table-column label="占比" width="100">
              <template #default="{ row }">
                {{ row.ratio ? `${row.ratio}%` : '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 医生排行 -->
      <el-col :span="12">
        <div class="table-section" v-loading="doctorLoading">
          <div class="section-header">
            <span>医生排行</span>
            <el-select v-model="sortBy" size="small" style="width: 100px" @change="loadDoctorRanking">
              <el-option label="挂号量" value="appointmentCount" />
              <el-option label="评分" value="avgScore" />
            </el-select>
          </div>
          <el-table :data="doctorList" stripe size="small" empty-text="暂无数据">
            <el-table-column prop="doctorName" label="医生" />
            <el-table-column prop="departmentName" label="科室" />
            <el-table-column label="挂号量" width="80">
              <template #default="{ row }">
                {{ row.appointmentCount ?? 0 }}
              </template>
            </el-table-column>
            <el-table-column label="评分" width="70">
              <template #default="{ row }">
                {{ row.avgScore ? Number(row.avgScore).toFixed(1) : '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <!-- 双饼图行 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <div class="chart-section" v-loading="deptLoading">
          <div class="section-header">科室挂号量占比</div>
          <div ref="pieDeptRef" class="chart-box-half"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-section" v-loading="pieLoading">
          <div class="section-header">预约状态分布</div>
          <div ref="pieRef" class="chart-box-half"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 1200px;
  padding: 24px 0;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

/* 总览卡片 */
.overview-section {
  margin-bottom: 24px;
}

.overview-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}

.card-icon { font-size: 22px; margin-bottom: 4px; }
.card-value { font-size: 20px; font-weight: 700; color: #303133; margin-bottom: 2px; }
.card-label { font-size: 12px; color: #909399; }

/* 趋势图 / 饼图 */
.chart-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.chart-box { width: 100%; height: 320px; }
.chart-box-half { width: 100%; height: 250px; }

/* 表格区 */
.table-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  min-height: 300px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
</style>