<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { doctorApi } from '@/api/doctor'
import type { DailyTrendVO } from '@/api/doctor/doctorApi'

// ========== 总览统计 ==========
const overviewLoading = ref(false)
const overview = ref<Record<string, unknown>>({})

async function loadOverview() {
  overviewLoading.value = true
  try {
    const res = await doctorApi.doctor.getOverview()
    overview.value = (res as unknown as Record<string, unknown>) ?? {}
  } catch {
    overview.value = {}
  } finally {
    overviewLoading.value = false
  }
}

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

async function loadTrend(days: number) {
  trendLoading.value = true
  try {
    const res = await doctorApi.doctor.getTrend({ days })
    const data = (res as unknown as DailyTrendVO[]) ?? []
    renderChart(data)
  } catch {
    renderChart([])
  } finally {
    trendLoading.value = false
  }
}

function renderChart(data: DailyTrendVO[]) {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.date),
      axisLabel: { rotate: 30 }
    },
    yAxis: {
      type: 'value',
      name: '接诊量',
      minInterval: 1
    },
    series: [
      {
        type: 'line',
        data: data.map((d) => d.count ?? 0),
        smooth: true,
        lineStyle: { color: '#409eff' },
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.35)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      }
    ],
    grid: { left: 50, right: 20, top: 30, bottom: 50 }
  })
}

function onResize() {
  chartInstance?.resize()
}

watch(selectedDays, (val) => {
  loadTrend(val)
})

// ========== 评价列表 ==========
const evalsLoading = ref(false)
const evaluations = ref<Record<string, unknown>[]>([])
const evalTotal = ref(0)
const evalPage = reactive({ page: 1, size: 10 })

async function loadEvaluations() {
  evalsLoading.value = true
  try {
    const res = await doctorApi.doctor.getEvaluations({
      page: evalPage.page,
      size: evalPage.size
    })
    const data = res as unknown as { list?: Record<string, unknown>[]; total?: number }
    evaluations.value = data.list ?? []
    evalTotal.value = data.total ?? 0
  } catch {
    evaluations.value = []
    evalTotal.value = 0
  } finally {
    evalsLoading.value = false
  }
}

function onEvalPageChange(page: number) {
  evalPage.page = page
  loadEvaluations()
}

onMounted(async () => {
  await loadOverview()
  nextTick(() => {
    loadTrend(selectedDays.value)
  })
  loadEvaluations()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<template>
  <div class="statistics-page">
    <h2 class="page-title">数据统计</h2>

    <!-- 总览统计卡片 -->
    <div class="overview-section" v-loading="overviewLoading">
      <el-row :gutter="16">
        <el-col :span="24" :sm="12" :md="8" :lg="Math.floor(24 / 5)" v-for="card in [
          { label: '总接诊数', key: 'totalPatients', icon: '👥' },
          { label: '今日接诊', key: 'todayPatients', icon: '📅' },
          { label: '平均评分', key: 'avgScore', icon: '⭐', format: (v: any) => Number(v || 0).toFixed(2) },
          { label: '好评率', key: 'positiveRate', icon: '👍', format: (v: any) => `${Number(v || 0).toFixed(1)}%` },
          { label: '待处理问诊', key: 'pendingConsultations', icon: '⏳' }
        ]" :key="card.key">
          <div class="overview-card">
            <div class="card-icon">{{ card.icon }}</div>
            <div class="card-value">{{ card.format ? card.format(overview[card.key]) : (overview[card.key] ?? 0) }}</div>
            <div class="card-label">{{ card.label }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 每日接诊趋势 -->
    <div class="trend-section" v-loading="trendLoading">
      <div class="section-header">
        <span>每日接诊趋势</span>
        <div class="days-switch">
          <el-radio-group v-model="selectedDays" size="small">
            <el-radio-button v-for="opt in daysOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
      <div ref="chartRef" class="chart-container"></div>
    </div>

    <!-- 评价列表 -->
    <div class="evaluation-section" v-loading="evalsLoading">
      <div class="section-header">患者评价</div>
      <template v-if="evaluations.length > 0">
        <div
          v-for="(item, index) in evaluations"
          :key="index"
          class="eval-item"
        >
          <div class="eval-header">
            <span class="eval-user">{{ item.isAnonymous ? '匿名用户' : ((item.patientName as string) || '患者') }}</span>
            <span class="eval-score">
              <el-rate :model-value="(item.score as number) || 0" disabled size="small" />
            </span>
            <span class="eval-date">{{ item.createdAt as string }}</span>
          </div>
          <div class="eval-content">{{ (item.content as string) || '（无文字评价）' }}</div>
        </div>
        <div v-if="evalTotal > evalPage.size" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="evalPage.page"
            :page-size="evalPage.size"
            :total="evalTotal"
            layout="prev, pager, next"
            background
            small
            @current-change="onEvalPageChange"
          />
        </div>
      </template>
      <el-empty v-else-if="!evalsLoading" description="暂无评价" />
    </div>
  </div>
</template>

<style scoped>
.statistics-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 16px;
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
  padding: 20px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}

.card-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.card-label {
  font-size: 13px;
  color: #909399;
}

/* 趋势图 */
.trend-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 300px;
}

/* 评价列表 */
.evaluation-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.eval-item {
  padding: 12px 0;
  border-bottom: 1px solid #f2f2f2;
}

.eval-item:last-child {
  border-bottom: none;
}

.eval-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.eval-user {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.eval-date {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}

.eval-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>