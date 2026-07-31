<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import AuditDialog from '@/components/admin/AuditDialog.vue'

const route = useRoute()
const router = useRouter()

const doctorId = Number(route.params.doctorId)

const detail = ref<Record<string, unknown>>({})
const loading = ref(false)
const approving = ref(false)

// 驳回对话框
const dialogVisible = ref(false)
const dialogLoading = ref(false)

async function loadDetail() {
  loading.value = true
  try {
    const res = await adminApi.admin.getAuditDetail(doctorId)
    detail.value = (res as unknown as Record<string, unknown>) ?? {}
  } catch {
    detail.value = {}
    ElMessage.error('加载审核详情失败')
  } finally {
    loading.value = false
  }
}

async function handleApprove() {
  approving.value = true
  try {
    const msg = await adminApi.admin.approve(doctorId)
    ElMessage.success((msg as unknown as string) || '审核通过')
    router.push({ name: 'AdminDoctorAudit' })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
  } finally {
    approving.value = false
  }
}

function openRejectDialog() {
  dialogVisible.value = true
}

async function handleReject(reason: string) {
  dialogLoading.value = true
  try {
    const msg = await adminApi.admin.reject(doctorId, { rejectReason: reason })
    ElMessage.success((msg as unknown as string) || '已驳回')
    dialogVisible.value = false
    router.push({ name: 'AdminDoctorAudit' })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
  } finally {
    dialogLoading.value = false
  }
}

function goBack() {
  router.push({ name: 'AdminDoctorAudit' })
}

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="audit-detail-page">
    <div class="back-row">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <h2 class="page-title">审核详情</h2>

    <div class="detail-card" v-loading="loading">
      <template v-if="detail.doctorName">
        <!-- 基本信息 -->
        <div class="section">
          <div class="section-title">基本信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">{{ detail.doctorName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detail.doctorPhone }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ detail.idCard || '-' }}</el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ { 0: '未知', 1: '男', 2: '女' }[detail.gender as number] || '未知' }}
            </el-descriptions-item>
            <el-descriptions-item label="注册时间" :span="2">{{ detail.createdAt }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 专业信息 -->
        <div class="section">
          <div class="section-title">专业信息</div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="科室">{{ detail.departmentName }}</el-descriptions-item>
            <el-descriptions-item label="职称">{{ detail.titleName }}</el-descriptions-item>
            <el-descriptions-item label="擅长领域">{{ detail.specialty || '-' }}</el-descriptions-item>
            <el-descriptions-item label="个人简介">{{ detail.introduction || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 证件图片 -->
        <div class="section" v-if="detail.certificateUrl || detail.qualificationUrl">
          <div class="section-title">证件资料</div>
          <el-row :gutter="16">
            <el-col :span="12" v-if="detail.certificateUrl">
              <div class="cert-card">
                <div class="cert-label">执业证书</div>
                <el-image
                  :src="(detail.certificateUrl as string)"
                  fit="contain"
                  style="width: 100%; max-height: 300px"
                  preview-teleported
                  :preview-src-list="[(detail.certificateUrl as string)]"
                />
              </div>
            </el-col>
            <el-col :span="12" v-if="detail.qualificationUrl">
              <div class="cert-card">
                <div class="cert-label">资格证</div>
                <el-image
                  :src="(detail.qualificationUrl as string)"
                  fit="contain"
                  style="width: 100%; max-height: 300px"
                  preview-teleported
                  :preview-src-list="[(detail.qualificationUrl as string)]"
                />
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 操作按钮 -->
        <div class="action-row">
          <el-button type="success" :loading="approving" @click="handleApprove">
            审核通过
          </el-button>
          <el-button type="danger" @click="openRejectDialog">
            审核驳回
          </el-button>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="审核信息不存在" />
    </div>

    <!-- 驳回对话框 -->
    <AuditDialog
      :visible="dialogVisible"
      :loading="dialogLoading"
      @confirm="handleReject"
      @cancel="dialogVisible = false"
    />
  </div>
</template>

<style scoped>
.audit-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px;
}

.back-row {
  margin-bottom: 8px;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.cert-card {
  text-align: center;
}

.cert-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.action-row {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 8px;
}
</style>