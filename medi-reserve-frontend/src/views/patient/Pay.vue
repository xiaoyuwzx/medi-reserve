<template>
  <div class="pay-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回确认预约
      </el-button>
    </div>

    <!-- 页面标题 -->
    <h2 class="page-title">确认支付</h2>

    <div class="pay-body">
      <!-- 倒计时 -->
      <div class="countdown-card" :class="{ 'countdown-urgent': remainingSeconds <= 60 }">
        <div class="countdown-icon">
          <el-icon :size="22">
            <Clock v-if="remainingSeconds > 0" />
            <CircleClose v-else />
          </el-icon>
        </div>
        <div class="countdown-text">
          <template v-if="remainingSeconds > 0">
            请在 <strong>{{ formatCountdown }}</strong> 内完成支付
          </template>
          <template v-else>
            支付已超时，订单已被取消
          </template>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="info-card">
        <h3 class="card-title">订单信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">预约单号</span>
            <span class="info-value order-no">{{ orderInfo.appointmentNo || '待生成' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">医生</span>
            <span class="info-value doctor-name">{{ orderInfo.doctorName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">科室</span>
            <span class="info-value">
              <el-tag size="small" type="primary" effect="plain">
                {{ orderInfo.departmentName || '-' }}
              </el-tag>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">职称</span>
            <span class="info-value">{{ orderInfo.titleName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">日期</span>
            <span class="info-value">{{ formatFullDate(orderInfo.scheduleDate) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">时段</span>
            <span class="info-value">
              <el-tag
                :type="orderInfo.periodText === '上午' ? 'warning' : 'info'"
                size="small"
              >
                {{ orderInfo.periodText || '-' }}
              </el-tag>
            </span>
          </div>
        </div>

        <!-- 支付金额 -->
        <div class="amount-row">
          <span class="amount-label">支付金额</span>
          <span class="amount-value">¥0.01</span>
        </div>
      </div>

      <!-- 支付按钮 -->
      <el-button
        type="primary"
        size="large"
        class="pay-btn"
        :loading="paying"
        :disabled="!canPay"
        @click="handlePay"
      >
        <template v-if="paying">支付中...</template>
        <template v-else-if="remainingSeconds <= 0">已超时</template>
        <template v-else>确认支付 ¥0.01</template>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Clock, CircleClose } from '@element-plus/icons-vue'
import { payAppointment } from '@/api/patient'

const route = useRoute()
const router = useRouter()

const appointmentId = route.params.appointmentId

// 订单信息（从路由 query 传入）
const orderInfo = reactive({
  appointmentNo: route.query.appointmentNo || '',
  doctorName: route.query.doctorName || '',
  departmentName: route.query.departmentName || '',
  titleName: route.query.titleName || '',
  scheduleDate: route.query.scheduleDate || '',
  periodText: route.query.periodText || '',
})

const paying = ref(false)
const remainingSeconds = ref(30 * 60) // 30 分钟 = 1800 秒
let timer = null

// 是否可支付
const canPay = computed(() => {
  return remainingSeconds.value > 0 && !paying.value
})

// 格式化倒计时：MM:SS
const formatCountdown = computed(() => {
  const minutes = Math.floor(remainingSeconds.value / 60)
  const seconds = remainingSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

// 日期格式化
const formatFullDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${weeks[d.getDay()]}`
}

// 开始倒计时
const startCountdown = () => {
  timer = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    } else {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

// 支付
const handlePay = async () => {
  if (!canPay.value) return

  paying.value = true
  try {
    await payAppointment(appointmentId)
    ElMessage.success('支付成功')
    // 清理倒计时
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      router.push('/patient/orders')
    }, 800)
  } catch {
    // 错误已由拦截器处理
    // 如果是超时（4010），响应拦截器会提示，这里也停止倒计时
    // 其他错误保留在当前页，允许重试
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  startCountdown()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.pay-page {
  max-width: 650px;
  margin: 0 auto;
}

/* ===== 返回栏 ===== */
.back-bar {
  margin-bottom: 8px;
}

/* ===== 页面标题 ===== */
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 24px;
}

/* ===== 倒计时卡片 ===== */
.countdown-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #ecf5ff;
  border-radius: 10px;
  margin-bottom: 20px;
  border: 1px solid #d9ecff;
  transition: all 0.3s;
}

.countdown-card.countdown-urgent {
  background: #fef0f0;
  border-color: #fde2e2;
}

.countdown-icon {
  color: #409eff;
  flex-shrink: 0;
}

.countdown-urgent .countdown-icon {
  color: #f56c6c;
}

.countdown-text {
  font-size: 14px;
  color: #606266;
}

.countdown-text strong {
  color: #409eff;
}

.countdown-urgent .countdown-text strong {
  color: #f56c6c;
}

/* ===== 信息卡片 ===== */
.info-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #909399;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  word-break: break-all;
}

.order-no {
  font-size: 12px;
  font-family: 'Courier New', monospace;
  color: #909399;
}

.doctor-name {
  font-size: 17px;
  font-weight: 600;
  color: #409eff;
}

/* ===== 支付金额 ===== */
.amount-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px dashed #e0e0e0;
}

.amount-label {
  font-size: 14px;
  color: #606266;
}

.amount-value {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}

/* ===== 支付按钮 ===== */
.pay-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  letter-spacing: 4px;
}

/* ===== 响应式适配 ===== */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-card {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }

  .amount-value {
    font-size: 20px;
  }
}
</style>