<template>
  <div class="hot-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回首页
      </el-button>
    </div>

    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon color="#e6a23c"><TrendCharts /></el-icon>
        热门医生排行
      </h2>
      <p class="page-desc">基于近30天评价数据，反映医生近期热度</p>
    </div>

    <!-- 排行列表 -->
    <div v-loading="loading" class="hot-body">
      <div v-if="doctorList.length === 0 && !loading" class="empty-state">
        <el-icon :size="48" color="#c0c4cc"><FolderOpened /></el-icon>
        <p>暂无热门医生</p>
      </div>

      <div class="rank-list">
        <div
          v-for="(doctor, index) in doctorList"
          :key="doctor.doctorId"
          class="rank-card"
          :class="rankClass(index)"
          @click="goToSchedule(doctor)"
        >
          <!-- 排名 -->
          <div class="rank-badge" :class="`rank-${index + 1}`">
            <span v-if="index === 0">🥇</span>
            <span v-else-if="index === 1">🥈</span>
            <span v-else-if="index === 2">🥉</span>
            <span v-else class="rank-number">{{ index + 1 }}</span>
          </div>

          <!-- 头像 -->
          <div class="rank-avatar">
            <el-avatar :size="48" :src="doctor.avatar">
              <el-icon :size="24"><UserFilled /></el-icon>
            </el-avatar>
          </div>

          <!-- 医生信息 -->
          <div class="rank-info">
            <div class="rank-name">{{ doctor.doctorName }}</div>
            <div class="rank-meta">
              <el-tag size="small" type="primary" effect="plain">
                {{ doctor.departmentName }}
              </el-tag>
              <span class="rank-title">{{ doctor.titleName }}</span>
            </div>
          </div>

          <!-- 热度评分 -->
          <div class="rank-score">
            <div class="score-stars">
              <el-icon color="#e6a23c"><StarFilled /></el-icon>
              <span class="score-value">{{ doctor.hotScore?.toFixed(1) || '-' }}</span>
            </div>
            <div class="score-count">{{ doctor.evaluationCount || 0 }} 条评价</div>
          </div>

          <!-- 箭头 -->
          <div class="rank-arrow">
            <el-icon color="#c0c4cc"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeft,
  ArrowRight,
  TrendCharts,
  FolderOpened,
  UserFilled,
  StarFilled,
} from '@element-plus/icons-vue'
import { getHotDoctors } from '@/api/patient'

const router = useRouter()

const doctorList = ref([])
const loading = ref(false)

// 排名样式
const rankClass = (index) => {
  if (index === 0) return 'rank-top1'
  if (index === 1) return 'rank-top2'
  if (index === 2) return 'rank-top3'
  return ''
}

// 跳转排班日历
const goToSchedule = (doctor) => {
  router.push({
    path: `/patient/schedule/${doctor.doctorId}`,
    query: {
      name: doctor.doctorName,
      department: doctor.departmentName,
      title: doctor.titleName,
      specialty: '',
    },
  })
}

// 获取数据
const fetchHotDoctors = async () => {
  loading.value = true
  try {
    const res = await getHotDoctors()
    doctorList.value = res.data || []
  } catch {
    doctorList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHotDoctors()
})
</script>

<style scoped>
.hot-page {
  max-width: 750px;
  margin: 0 auto;
}

/* ===== 返回栏 ===== */
.back-bar {
  margin-bottom: 8px;
}

/* ===== 页面头部 ===== */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

/* ===== 内容区 ===== */
.hot-body {
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

/* ===== 排行列表 ===== */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid transparent;
}

.rank-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  border-color: #409eff;
}

/* 前三名特殊背景 */
.rank-card.rank-top1 {
  border-color: #f0c78e;
  background: linear-gradient(135deg, #fffbf0 0%, #fff 100%);
}

.rank-card.rank-top2 {
  border-color: #d0d8e0;
  background: linear-gradient(135deg, #f8fafc 0%, #fff 100%);
}

.rank-card.rank-top3 {
  border-color: #e0c8a8;
  background: linear-gradient(135deg, #fdf8f2 0%, #fff 100%);
}

/* ===== 排名徽章 ===== */
.rank-badge {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.rank-badge.rank-1,
.rank-badge.rank-2,
.rank-badge.rank-3 {
  font-size: 28px;
  background: transparent;
}

.rank-number {
  font-size: 16px;
  font-weight: 700;
  color: #909399;
}

/* ===== 头像 ===== */
.rank-avatar {
  flex-shrink: 0;
}

/* ===== 医生信息 ===== */
.rank-info {
  flex: 1;
  min-width: 0;
}

.rank-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.rank-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rank-title {
  font-size: 12px;
  color: #909399;
}

/* ===== 热度评分 ===== */
.rank-score {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}

.score-stars {
  display: flex;
  align-items: center;
  gap: 4px;
}

.score-value {
  font-size: 15px;
  font-weight: 700;
  color: #e6a23c;
}

.score-count {
  font-size: 12px;
  color: #c0c4cc;
}

/* ===== 箭头 ===== */
.rank-arrow {
  flex-shrink: 0;
  display: none;
}

/* ===== 响应式适配 ===== */
@media (max-width: 768px) {
  .rank-card {
    padding: 14px 16px;
    gap: 12px;
  }

  .rank-badge {
    width: 32px;
    height: 32px;
  }

  .rank-badge.rank-1,
  .rank-badge.rank-2,
  .rank-badge.rank-3 {
    font-size: 24px;
  }

  .rank-score {
    display: none;
  }

  .rank-arrow {
    display: block;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }

  .rank-card {
    padding: 12px 14px;
  }

  .rank-name {
    font-size: 14px;
  }
}
</style>