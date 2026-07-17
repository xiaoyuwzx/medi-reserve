<template>
  <div class="evaluate-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button text @click="$router.push('/patient/orders')">
        <el-icon><ArrowLeft /></el-icon>
        返回我的预约
      </el-button>
    </div>

    <!-- 页面标题 -->
    <h2 class="page-title">就诊评价</h2>

    <!-- 就诊信息 -->
    <div class="info-card">
      <h3 class="card-title">就诊信息</h3>
      <div class="info-grid">
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
          <span class="info-label">就诊时间</span>
          <span class="info-value">{{ formatFullDate(orderInfo.scheduleDate) }} {{ orderInfo.periodText || '' }}</span>
        </div>
      </div>
    </div>

    <!-- 评价表单 -->
    <div class="form-card">
      <h3 class="card-title">评价内容</h3>

      <div class="form-item">
        <div class="form-label">评分</div>
        <div class="score-row">
          <el-rate
            v-model="form.score"
            :texts="rateTexts"
            show-text
            :disabled="submitting"
          />
        </div>
      </div>

      <div class="form-item">
        <div class="form-label">评价内容</div>
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="请输入您的就诊体验...（选填，最多500字）"
          maxlength="500"
          show-word-limit
          :disabled="submitting"
        />
      </div>

      <div class="form-item">
        <div class="form-label">匿名评价</div>
        <div class="switch-row">
          <el-switch v-model="form.isAnonymous" :disabled="submitting" />
          <span class="switch-text">
            {{ form.isAnonymous ? '您的姓名将不会显示在评价中' : '您的姓名将对医生可见' }}
          </span>
        </div>
      </div>
    </div>

    <!-- 提交按钮 -->
    <el-button
      type="primary"
      size="large"
      class="submit-btn"
      :loading="submitting"
      :disabled="!form.score"
      @click="handleSubmit"
    >
      {{ submitting ? '提交中...' : '提交评价' }}
    </el-button>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createEvaluation } from '@/api/patient'

const route = useRoute()
const router = useRouter()

const appointmentId = route.params.appointmentId

// 就诊信息（从路由 query 传入）
const orderInfo = reactive({
  doctorName: route.query.doctorName || '',
  departmentName: route.query.departmentName || '',
  titleName: route.query.titleName || '',
  scheduleDate: route.query.scheduleDate || '',
  periodText: route.query.periodText || '',
})

const submitting = ref(false)

// 评价表单
const form = reactive({
  score: 0,
  content: '',
  isAnonymous: false,
})

// 评分文本描述
const rateTexts = ['很差', '较差', '一般', '满意', '很满意']

// 日期格式化
const formatFullDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${d.getMonth() + 1}月${d.getDate()}日 ${weeks[d.getDay()]}`
}

// 提交评价
const handleSubmit = async () => {
  if (!form.score) return

  submitting.value = true
  try {
    await createEvaluation({
      appointmentId: Number(appointmentId),
      score: form.score,
      content: form.content || undefined,
      isAnonymous: form.isAnonymous,
    })

    ElMessage.success('评价成功')
    // 延迟跳转
    setTimeout(() => {
      router.push('/patient/orders')
    }, 800)
  } catch {
    // 错误已由拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.evaluate-page {
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

/* ===== 信息卡片 ===== */
.info-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
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
}

.doctor-name {
  font-size: 17px;
  font-weight: 600;
  color: #409eff;
}

/* ===== 评价表单卡片 ===== */
.form-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 10px;
}

.score-row {
  padding: 4px 0;
}

.switch-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.switch-text {
  font-size: 13px;
  color: #909399;
}

/* ===== 提交按钮 ===== */
.submit-btn {
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

  .info-card,
  .form-card {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }

  .doctor-name {
    font-size: 16px;
  }
}
</style>