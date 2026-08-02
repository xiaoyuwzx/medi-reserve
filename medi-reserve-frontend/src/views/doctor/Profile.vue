<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { doctorApi } from '@/api/doctor'

const userStore = useUserStore()

const isEditing = ref(false)
const saving = ref(false)

const form = reactive({
  name: userStore.name,
  phone: userStore.phone,
  idCard: userStore.idCard,
  gender: userStore.gender,
  certificateUrl: '' as string,
  qualificationUrl: '' as string
})

const genderMap: Record<number, string> = {
  0: '未知',
  1: '男',
  2: '女'
}

// 证件审核状态 + 证书图片
const auditLoading = ref(false)
const auditData = ref<Record<string, unknown>>({})
const certUploading = ref(false)
const qualUploading = ref(false)

const auditStatusMap: Record<number, { text: string; type: 'info' | 'warning' | 'success' | 'danger' }> = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已驳回', type: 'danger' }
}

async function loadAuditStatus() {
  auditLoading.value = true
  try {
    const res = await doctorApi.doctor.getAuditStatus()
    auditData.value = (res as unknown as Record<string, unknown>) ?? {}
  } catch {
    // 静默
  } finally {
    auditLoading.value = false
  }
}

function startEdit() {
  form.name = userStore.name
  form.phone = userStore.phone
  form.idCard = userStore.idCard
  form.gender = userStore.gender
  form.certificateUrl = ''
  form.qualificationUrl = ''
  isEditing.value = true
}

function cancelEdit() {
  isEditing.value = false
}

async function uploadFile(file: File, type: 'certificate' | 'qualification'): Promise<string> {
  // 1. 获取 STS 凭证
  const stsRes = await doctorApi.doctor.getStsToken()
  const stsData = stsRes as unknown as {
    accessKeyId?: string
    accessKeySecret?: string
    securityToken?: string
    bucket?: string
    endpoint?: string
    dir?: string
  }

  // 2. 动态导入 OSS 客户端
  const OSS = (await import('ali-oss')).default
  const client = new OSS({
    endpoint: stsData.endpoint,
    accessKeyId: stsData.accessKeyId!,
    accessKeySecret: stsData.accessKeySecret!,
    stsToken: stsData.securityToken!,
    bucket: stsData.bucket!,
    secure: true
  })

  // 3. 上传
  const filename = `${Date.now()}_${file.name}`
  await client.put(stsData.dir + filename, file)

  // 4. 构造 URL
  return `https://${stsData.bucket}.${stsData.endpoint}/${stsData.dir}${filename}`
}

async function handleCertUpload(file: File) {
  certUploading.value = true
  try {
    const url = await uploadFile(file, 'certificate')
    form.certificateUrl = url
    ElMessage.success('执业证书上传成功')
  } catch (error) {
    ElMessage.error('执业证书上传失败')
  } finally {
    certUploading.value = false
  }
}

async function handleQualUpload(file: File) {
  qualUploading.value = true
  try {
    const url = await uploadFile(file, 'qualification')
    form.qualificationUrl = url
    ElMessage.success('资格证上传成功')
  } catch (error) {
    ElMessage.error('资格证上传失败')
  } finally {
    qualUploading.value = false
  }
}

async function saveProfile() {
  saving.value = true
  try {
    const payload: Record<string, unknown> = {
      name: form.name,
      phone: form.phone,
      idCard: form.idCard || undefined,
      gender: form.gender
    }
    if (form.certificateUrl) payload.certificateUrl = form.certificateUrl
    if (form.qualificationUrl) payload.qualificationUrl = form.qualificationUrl

    await doctorApi.doctor.updateProfile(payload as any)
    userStore.updateProfile(form.name, form.phone, form.idCard, form.gender)
    ElMessage.success('保存成功')
    isEditing.value = false
    loadAuditStatus() // 刷新审核状态
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadAuditStatus()
})
</script>

<template>
  <div class="profile-page">
    <h2 class="page-title">个人信息</h2>

    <div class="profile-card">
      <!-- 证件审核状态 -->
      <div class="audit-section" v-loading="auditLoading">
        <div class="section-header">证件审核状态</div>
        <el-tag
          v-if="auditData.certAuditStatus !== undefined"
          :type="auditStatusMap[auditData.certAuditStatus as number]?.type ?? 'info'"
          size="large"
        >
          {{ auditStatusMap[auditData.certAuditStatus as number]?.text ?? '未知' }}
        </el-tag>
        <span v-else class="no-data">暂无数据</span>
      </div>

      <!-- 查看模式 -->
      <template v-if="!isEditing">
        <div class="info-grid">
          <div class="info-item">
            <span class="label">姓名</span>
            <span class="value">{{ userStore.name || userStore.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机号</span>
            <span class="value">{{ userStore.phone || userStore.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">身份证号</span>
            <span class="value">{{ userStore.idCard || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">性别</span>
            <span class="value">{{ genderMap[userStore.gender] || '未知' }}</span>
          </div>
        </div>

        <!-- 证书图片 -->
        <div class="cert-section" v-if="auditData.certificateUrl || auditData.qualificationUrl || auditData.pendingCertificateUrl || auditData.pendingQualificationUrl">
          <div class="section-header">执业证书</div>
          <div class="cert-images">
            <div class="cert-item" v-if="auditData.certificateUrl">
              <div class="cert-label">当前生效</div>
              <el-image
                :src="auditData.certificateUrl as string"
                fit="contain"
                style="max-width:200px;max-height:150px"
                preview-teleported
                :preview-src-list="[auditData.certificateUrl as string]"
              />
            </div>
            <div class="cert-item" v-if="auditData.pendingCertificateUrl">
              <div class="cert-label pending">待审核</div>
              <el-image
                :src="auditData.pendingCertificateUrl as string"
                fit="contain"
                style="max-width:200px;max-height:150px"
                preview-teleported
                :preview-src-list="[auditData.pendingCertificateUrl as string]"
              />
            </div>
          </div>
          <div class="section-header" style="margin-top:16px">资格证</div>
          <div class="cert-images">
            <div class="cert-item" v-if="auditData.qualificationUrl">
              <div class="cert-label">当前生效</div>
              <el-image
                :src="auditData.qualificationUrl as string"
                fit="contain"
                style="max-width:200px;max-height:150px"
                preview-teleported
                :preview-src-list="[auditData.qualificationUrl as string]"
              />
            </div>
            <div class="cert-item" v-if="auditData.pendingQualificationUrl">
              <div class="cert-label pending">待审核</div>
              <el-image
                :src="auditData.pendingQualificationUrl as string"
                fit="contain"
                style="max-width:200px;max-height:150px"
                preview-teleported
                :preview-src-list="[auditData.pendingQualificationUrl as string]"
              />
            </div>
          </div>
        </div>

        <div class="action-row">
          <el-button type="primary" @click="startEdit">编辑</el-button>
        </div>
      </template>

      <!-- 编辑模式 -->
      <template v-else>
        <div class="form-grid">
          <div class="form-item">
            <span class="label">姓名</span>
            <el-input v-model="form.name" placeholder="请输入姓名" />
          </div>
          <div class="form-item">
            <span class="label">手机号</span>
            <el-input v-model="form.phone" placeholder="请输入11位手机号" />
          </div>
          <div class="form-item">
            <span class="label">身份证号</span>
            <el-input v-model="form.idCard" placeholder="选填" />
          </div>
          <div class="form-item">
            <span class="label">性别</span>
            <el-select v-model="form.gender" style="width: 100%">
              <el-option label="未知" :value="0" />
              <el-option label="男" :value="1" />
              <el-option label="女" :value="2" />
            </el-select>
          </div>
        </div>

        <!-- 证件上传 -->
        <div class="upload-section">
          <div class="section-header">证件资料（选填）</div>

          <!-- 执业证书上传 -->
          <div class="upload-item">
            <span class="upload-label">执业证书：</span>
            <div class="upload-content">
              <span v-if="form.certificateUrl" class="upload-success">✅ 已上传</span>
              <el-upload
                :auto-upload="false"
                :show-file-list="false"
                accept="image/*"
                :on-change="(uploadFile: any) => handleCertUpload(uploadFile.raw)"
                :disabled="certUploading"
              >
                <el-button size="small" :loading="certUploading">
                  {{ form.certificateUrl ? '重新上传' : '点击上传' }}
                </el-button>
              </el-upload>
            </div>
          </div>

          <!-- 资格证上传 -->
          <div class="upload-item">
            <span class="upload-label">资格证：</span>
            <div class="upload-content">
              <span v-if="form.qualificationUrl" class="upload-success">✅ 已上传</span>
              <el-upload
                :auto-upload="false"
                :show-file-list="false"
                accept="image/*"
                :on-change="(uploadFile: any) => handleQualUpload(uploadFile.raw)"
                :disabled="qualUploading"
              >
                <el-button size="small" :loading="qualUploading">
                  {{ form.qualificationUrl ? '重新上传' : '点击上传' }}
                </el-button>
              </el-upload>
            </div>
          </div>
        </div>

        <div class="action-row">
          <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 560px;
  margin: 0 auto;
  padding: 24px 16px;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

.profile-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.audit-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.section-header {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.no-data {
  font-size: 13px;
  color: #909399;
}

.info-grid,
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.info-item,
.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label {
  font-size: 13px;
  color: #909399;
}

.value {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

/* 证书图片 */
.cert-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.cert-images {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.cert-item {
  text-align: center;
}

.cert-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.cert-label.pending {
  color: #e6a23c;
  font-weight: 600;
}

/* 上传区 */
.upload-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.upload-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.upload-label {
  font-size: 13px;
  color: #606266;
  width: 72px;
  flex-shrink: 0;
}

.upload-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-success {
  font-size: 13px;
  color: #67c23a;
}

.action-row {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>