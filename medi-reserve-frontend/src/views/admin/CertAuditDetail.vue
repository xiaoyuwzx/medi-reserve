<script setup lang="ts">
import { ref, onMounted } from 'vue'
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

const dialogVisible = ref(false)
const dialogLoading = ref(false)

async function loadDetail() {
  loading.value = true
  try {
    const res = await adminApi.admin.getCertPendingDetail(doctorId)
    detail.value = (res as unknown as Record<string, unknown>) ?? {}
  } catch {
    detail.value = {}
    ElMessage.error('加载证件详情失败')
  } finally {
    loading.value = false
  }
}

async function handleApprove() {
  approving.value = true
  try {
    const msg = await adminApi.admin.auditCertificate(doctorId, { result: 1 })
    ElMessage.success((msg as unknown as string) || '审核通过')
    router.push({ name: 'AdminCertAudit' })
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
    const msg = await adminApi.admin.auditCertificate(doctorId, { result: 2, remark: reason })
    ElMessage.success((msg as unknown as string) || '已驳回')
    dialogVisible.value = false
    router.push({ name: 'AdminCertAudit' })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
  } finally {
    dialogLoading.value = false
  }
}

function goBack() {
  router.push({ name: 'AdminCertAudit' })
}

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="cert-detail-page">
    <div class="back-row">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <h2 class="page-title">证件审核详情</h2>

    <div class="detail-card" v-loading="loading">
      <template v-if="detail.doctorName">
        <div class="section">
          <div class="section-title">医生基本信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">{{ detail.doctorName }}</el-descriptions-item>
            <el-descriptions-item label="科室">{{ detail.departmentName }}</el-descriptions-item>
            <el-descriptions-item label="职称">{{ detail.titleName }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ detail.submittedAt }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">证件对比</div>
          <el-row :gutter="24">
            <el-col :span="12">
              <div class="cert-panel">
                <div class="cert-panel-title">当前生效证件</div>
                <div class="cert-item">
                  <div class="cert-label">执业证书</div>
                  <el-image
                    v-if="detail.currentCertificateUrl"
                    :src="(detail.currentCertificateUrl as string)"
                    fit="contain"
                    style="width: 100%; max-height: 250px"
                    preview-teleported
                    :preview-src-list="[(detail.currentCertificateUrl as string)]"
                  />
                  <span v-else class="no-cert">无</span>
                </div>
                <div class="cert-item">
                  <div class="cert-label">资格证</div>
                  <el-image
                    v-if="detail.currentQualificationUrl"
                    :src="(detail.currentQualificationUrl as string)"
                    fit="contain"
                    style="width: 100%; max-height: 250px"
                    preview-teleported
                    :preview-src-list="[(detail.currentQualificationUrl as string)]"
                  />
                  <span v-else class="no-cert">无</span>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="cert-panel pending">
                <div class="cert-panel-title">待审核证件</div>
                <div class="cert-item">
                  <div class="cert-label">执业证书</div>
                  <el-image
                    v-if="detail.pendingCertificateUrl"
                    :src="(detail.pendingCertificateUrl as string)"
                    fit="contain"
                    style="width: 100%; max-height: 250px"
                    preview-teleported
                    :preview-src-list="[(detail.pendingCertificateUrl as string)]"
                  />
                  <span v-else class="no-cert">无</span>
                </div>
                <div class="cert-item">
                  <div class="cert-label">资格证</div>
                  <el-image
                    v-if="detail.pendingQualificationUrl"
                    :src="(detail.pendingQualificationUrl as string)"
                    fit="contain"
                    style="width: 100%; max-height: 250px"
                    preview-teleported
                    :preview-src-list="[(detail.pendingQualificationUrl as string)]"
                  />
                  <span v-else class="no-cert">无</span>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="action-row">
          <el-button type="success" :loading="approving" @click="handleApprove">
            审核通过
          </el-button>
          <el-button type="danger" @click="openRejectDialog">
            审核驳回
          </el-button>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="证件信息不存在" />
    </div>

    <AuditDialog
      :visible="dialogVisible"
      :loading="dialogLoading"
      @confirm="handleReject"
      @cancel="dialogVisible = false"
    />
  </div>
</template>

<style scoped>
.cert-detail-page {
  max-width: 1000px;
  padding: 24px 0;
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

.cert-panel {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.cert-panel.pending {
  background: #fef0f0;
}

.cert-panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
}

.cert-item {
  margin-bottom: 12px;
}

.cert-item:last-child {
  margin-bottom: 0;
}

.cert-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}

.no-cert {
  font-size: 14px;
  color: #c0c4cc;
}

.action-row {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 8px;
}
</style>