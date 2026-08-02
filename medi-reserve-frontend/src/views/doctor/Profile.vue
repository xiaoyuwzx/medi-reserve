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
  const stsRes = await doctorApi.doctor.getStsToken()
  const stsData = stsRes as unknown as {
    accessKeyId?: string
    accessKeySecret?: string
    securityToken?: string
    bucket?: string
    endpoint?: string
    dir?: string
  }

  const OSS = (await import('ali-oss')).default
  const client = new OSS({
    endpoint: stsData.endpoint,
    accessKeyId: stsData.accessKeyId!,
    accessKeySecret: stsData.accessKeySecret!,
    stsToken: stsData.securityToken!,
    bucket: stsData.bucket!,
    secure: true
  })

  const filename = `${Date.now()}_${file.name}`
  await client.put(stsData.dir + filename, file)

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
    loadAuditStatus()
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

        <!-- 证书图片（始终显示） -->
        <div class="cert-section">
          <div class="section-header">执业证书</div>
          <div class="cert-images">
            <div class="cert-item" v-if="auditData.certificateUrl?.toString().trim()">
              <el-image
                :src="(auditData.certificateUrl as string)?.trim() || ''"
                fit="contain"
                style="max-width:200px;max-height:150px"
                preview-teleported
                :preview-src-list="[(auditData.certificateUrl as string)?.trim() || '']"
              >
                <template #error>
                  <div class="image-error">加载失败</div>
                </template>
              </el-image>
              <span class="cert-status success">✅ 当前生效</span>
            </div>
            <div class="cert-item" v-else>
              <div class="image-placeholder">暂无图片</div>
              <span class="cert-status info">未上传</span>
            </div>
            <div class="cert-item" v-if="auditData.pendingCertificateUrl?.toString().trim()">
              <el-image
                :src="(auditData.pendingCertificateUrl as string)?.trim() || ''"
                fit="contain"
                style="max-width:200px;max-height:150px"
                preview-teleported
                :preview-src-list="[(auditData.pendingCertificateUrl as string)?.trim() || '']"
              >
                <template #error>
                  <div class="image-error">加载失败</div>
                </template>
              </el-image>
              <span class="cert-status pending">⏳ 待审核</span>
            </div>
          </div>

          <div class="section-header" style="margin-top:16px">资格证</div>
          <div class="cert-images">
            <div class="cert-item" v-if="auditData.qualificationUrl?.toString().trim()">
              <el-image
                :src="(auditData.qualificationUrl as string)?.trim() || ''"
                fit="contain"
                style="max-width:200px;max-height:150px"
                preview-teleported
                :preview-src-list="[(auditData.qualificationUrl as string)?.trim() || '']"
              >
                <template #error>
                  <div class="image-error">加载失败</div>
                </template>
              </el-image>
              <span class="cert-status success">✅ 当前生效</span>
            </div>
            <div class="cert-item" v-else>
              <div class="image-placeholder">暂无图片</div>
              <span class="cert-status info">未上传</span>
            </div>
            <div class="cert-item" v-if="auditData.pendingQualificationUrl?.toString().trim()">
              <el-image
                :src="(auditData.pendingQualificationUrl as string)?.trim() || ''"
                fit="contain"
                style="max-width:200px;max-height:150px"
                preview-teleported
                :preview-src-list="[(auditData.pendingQualificationUrl as string)?.trim() || '']"
              >
                <template #error>
                  <div class="image-error">加载失败</div>
                </template>
              </el-image>
              <span class="cert-status pending">⏳ 待审核</span>
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

        <!-- 证件上传（带新旧预览） -->
        <div class="upload-section">
          <div class="section-header">证件资料</div>

          <!-- 执业证书 -->
          <div class="upload-group">
            <div class="upload-group-title">执业证书</div>
            <div class="upload-group-content">
              <div class="preview-item" v-if="auditData.certificateUrl?.toString().trim()">
                <div class="preview-label">当前证件</div>
                <el-image
                  :src="(auditData.certificateUrl as string)?.trim() || ''"
                  fit="contain"
                  style="width:120px;height:90px;border-radius:4px"
                  preview-teleported
                  :preview-src-list="[(auditData.certificateUrl as string)?.trim() || '']"
                />
              </div>
              <div class="preview-item" v-if="form.certificateUrl">
                <div class="preview-label preview-new">新上传</div>
                <el-image
                  :src="form.certificateUrl"
                  fit="contain"
                  style="width:120px;height:90px;border-radius:4px;border:2px solid #409eff"
                  preview-teleported
                  :preview-src-list="[form.certificateUrl]"
                />
              </div>
              <div class="upload-btn-area">
                <span v-if="!auditData.certificateUrl?.toString().trim() && !form.certificateUrl" class="upload-hint-text">暂无执业证书</span>
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
          </div>

          <!-- 资格证 -->
          <div class="upload-group">
            <div class="upload-group-title">资格证</div>
            <div class="upload-group-content">
              <div class="preview-item" v-if="auditData.qualificationUrl?.toString().trim()">
                <div class="preview-label">当前证件</div>
                <el-image
                  :src="(auditData.qualificationUrl as string)?.trim() || ''"
                  fit="contain"
                  style="width:120px;height:90px;border-radius:4px"
                  preview-teleported
                  :preview-src-list="[(auditData.qualificationUrl as string)?.trim() || '']"
                />
              </div>
              <div class="preview-item" v-if="form.qualificationUrl">
                <div class="preview-label preview-new">新上传</div>
                <el-image
                  :src="form.qualificationUrl"
                  fit="contain"
                  style="width:120px;height:90px;border-radius:4px;border:2px solid #409eff"
                  preview-teleported
                  :preview-src-list="[form.qualificationUrl]"
                />
              </div>
              <div class="upload-btn-area">
                <span v-if="!auditData.qualificationUrl?.toString().trim() && !form.qualificationUrl" class="upload-hint-text">暂无资格证</span>
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

.cert-status {
  display: block;
  margin-top: 6px;
  font-size: 12px;
}

.cert-status.success { color: #67c23a; }
.cert-status.pending { color: #e6a23c; }
.cert-status.info { color: #909399; }

.image-placeholder {
  width: 200px;
  height: 150px;
  background: #f5f5f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 13px;
}

/* 上传区 */
.upload-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.upload-group {
  margin-bottom: 16px;
}

.upload-group-title {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.upload-group-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.preview-item {
  text-align: center;
}

.preview-label {
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.preview-label.preview-new {
  color: #409eff;
  font-weight: 600;
}

.upload-btn-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-height: 90px;
  justify-content: center;
}

.upload-hint-text {
  font-size: 12px;
  color: #c0c4cc;
}

.image-error {
  width: 200px;
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #c0c4cc;
  font-size: 13px;
  border-radius: 4px;
}

.action-row {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>