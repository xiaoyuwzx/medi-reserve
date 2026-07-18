<template>
  <div class="orders-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">我的预约</h2>
    </div>

    <!-- 状态标签页 -->
    <div class="tabs-bar">
      <el-radio-group v-model="activeStatus" @change="handleStatusChange">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待支付</el-radio-button>
        <el-radio-button :value="1">已支付</el-radio-button>
        <el-radio-button :value="2">已完成</el-radio-button>
        <el-radio-button :value="3">已取消</el-radio-button>
        <el-radio-button :value="4">已过期</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 预约列表 -->
    <div v-loading="loading" class="orders-body">
      <div v-if="orderList.length === 0 && !loading" class="empty-state">
        <el-icon :size="48" color="#c0c4cc"><FolderOpened /></el-icon>
        <p>暂无预约记录</p>
      </div>

      <div class="order-list">
        <div
          v-for="order in orderList"
          :key="order.id"
          class="order-card"
        >
          <div class="card-left">
            <div class="order-line">
              <span class="label">预约单号</span>
              <span class="value order-no">{{ order.appointmentNo }}</span>
            </div>
            <div class="order-line">
              <span class="label">医生</span>
              <span class="value doctor-name">{{ order.doctorName }}</span>
            </div>
            <div class="order-line">
              <span class="label">科室 / 职称</span>
              <span class="value">
                <el-tag size="small" type="primary" effect="plain">{{ order.departmentName }}</el-tag>
                <span class="title-text">{{ order.titleName }}</span>
              </span>
            </div>
            <div class="order-line">
              <span class="label">就诊时间</span>
              <span class="value">{{ formatFullDate(order.scheduleDate) }} {{ order.periodText }}</span>
            </div>
            <div class="order-line">
              <span class="label">下单时间</span>
              <span class="value time-text">{{ formatDateTime(order.createdAt) }}</span>
            </div>
          </div>
          <div class="card-right">
            <el-tag
              :type="statusTagType(order.status)"
              size="default"
              effect="dark"
            >
              {{ statusText(order.status) }}
            </el-tag>

            <div class="card-actions">
              <!-- 待支付 → 去支付 -->
              <el-button
                v-if="order.status === 0"
                type="primary"
                size="small"
                @click="goToPay(order.id)"
              >
                去支付
              </el-button>

              <!-- 已支付 + 就诊日期是今天 → 进入问诊室 -->
              <el-button
                v-if="order.status === 1 && isToday(order.scheduleDate)"
                type="primary"
                size="small"
                @click="goToChat(order.id)"
              >
                进入问诊室
              </el-button>

              <!-- 已支付 + 就诊日期已过 → 去评价 -->
              <el-button
                v-if="order.status === 1 && isPastDate(order.scheduleDate) && !isToday(order.scheduleDate)"
                type="success"
                size="small"
                @click="goToEvaluate(order)"
              >
                去评价
              </el-button>

              <!-- 已支付 + 就诊日期未来 → 等待就诊 -->
              <span
                v-if="order.status === 1 && !isPastDate(order.scheduleDate) && !isToday(order.scheduleDate)"
                class="waiting-text"
              >
                等待就诊
              </span>
            </div>
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
import { useRouter } from 'vue-router'
import { FolderOpened } from '@element-plus/icons-vue'
import { getMyAppointments } from '@/api/patient'

const router = useRouter()

const orderList = ref([])
const loading = ref(false)
const total = ref(0)
const activeStatus = ref(null) // null = 全部

const pagination = reactive({
  page: 1,
  size: 10,
})

// 状态映射
const statusMap = {
  0: '待支付',
  1: '已支付',
  2: '已完成',
  3: '已取消',
  4: '已过期',
}

const statusTagMap = {
  0: 'warning',
  1: 'success',
  2: '',
  3: 'info',
  4: 'danger',
}

const statusText = (status) => statusMap[status] ?? '未知'
const statusTagType = (status) => statusTagMap[status] ?? 'info'

// 判断就诊日期是否已过
const isPastDate = (dateStr) => {
  if (!dateStr) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const scheduleDate = new Date(dateStr)
  scheduleDate.setHours(0, 0, 0, 0)
  return scheduleDate < today
}

// 判断就诊日期是否是今天
const isToday = (dateStr) => {
  if (!dateStr) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const targetDate = new Date(dateStr)
  targetDate.setHours(0, 0, 0, 0)
  return targetDate.getTime() === today.getTime()
}

// 日期格式化
const formatFullDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${d.getMonth() + 1}月${d.getDate()}日 ${weeks[d.getDay()]}`
}

// 时间格式化
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 16)
}

// 构建请求参数
const buildParams = () => {
  const params = {
    page: pagination.page,
    size: pagination.size,
  }
  if (activeStatus.value !== null) {
    params.status = activeStatus.value
  }
  return params
}

// 获取预约列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getMyAppointments(buildParams())
    const data = res.data
    orderList.value = data.list || []
    total.value = data.total || 0
  } catch {
    orderList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 状态切换
const handleStatusChange = () => {
  pagination.page = 1
  fetchOrders()
}

// 分页切换
const handlePageChange = (page) => {
  pagination.page = page
  fetchOrders()
}

// 每页条数切换
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchOrders()
}

// 去支付
const goToPay = (appointmentId) => {
  router.push(`/patient/pay/${appointmentId}`)
}

// 进入问诊室
const goToChat = (appointmentId) => {
  router.push(`/patient/chat/${appointmentId}`)
}

// 去评价（携带预约信息）
const goToEvaluate = (order) => {
  router.push({
    path: `/patient/evaluate/${order.id}`,
    query: {
      doctorName: order.doctorName,
      departmentName: order.departmentName,
      titleName: order.titleName,
      scheduleDate: order.scheduleDate,
      periodText: order.periodText,
    },
  })
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-page {
  max-width: 900px;
  margin: 0 auto;
}

/* ===== 页面标题 ===== */
.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

/* ===== 标签页 ===== */
.tabs-bar {
  margin-bottom: 20px;
}

/* ===== 内容区 ===== */
.orders-body {
  min-height: 200px;
}

/* ===== 空状态 ===== */
.empty-state {
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

/* ===== 订单卡片 ===== */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.order-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
}

.order-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.card-left {
  flex: 1;
  min-width: 0;
}

.order-line {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 6px;
}

.order-line:last-child {
  margin-bottom: 0;
}

.label {
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
  width: 60px;
}

.value {
  font-size: 14px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-no {
  font-size: 12px;
  font-family: 'Courier New', monospace;
  color: #909399;
}

.doctor-name {
  font-weight: 600;
  color: #303133;
}

.title-text {
  font-size: 12px;
  color: #909399;
}

.time-text {
  font-size: 12px;
  color: #c0c4cc;
}

/* ===== 右侧状态 + 操作 ===== */
.card-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  flex-shrink: 0;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.waiting-text {
  font-size: 12px;
  color: #c0c4cc;
}

/* ===== 分页 ===== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* ===== 响应式适配 ===== */
@media (max-width: 768px) {
  .order-card {
    flex-direction: column;
    align-items: stretch;
    padding: 18px;
  }

  .card-right {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .label {
    width: 55px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }

  .order-card {
    padding: 16px;
  }

  .label {
    width: 50px;
    font-size: 12px;
  }

  .value {
    font-size: 13px;
  }
}
</style>