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
  gender: userStore.gender
})

const genderMap: Record<number, string> = {
  0: '未知',
  1: '男',
  2: '女'
}

// 证件审核状态
const auditLoading = ref(false)
const auditStatus = ref<{
  certAuditStatus?: number
  certAuditStatusText?: string
}>({})

const auditStatusMap: Record<number, { text: string; type: 'info' | 'warning' | 'success' | 'danger' }> = {
  0: { text: '未提交', type: 'info' },
  1: { text: '审核中', type: 'warning' },
  2: { text: '已通过', type: 'success' },
  3: { text: '已驳回', type: 'danger' }
}

async function loadAuditStatus() {
  auditLoading.value = true
  try {
    const res = await doctorApi.doctor.getAuditStatus()
    auditStatus.value = (res as unknown as { certAuditStatus?: number; certAuditStatusText?: string }) ?? {}
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
  isEditing.value = true
}

function cancelEdit() {
  isEditing.value = false
}

async function saveProfile() {
  saving.value = true
  try {
    await doctorApi.doctor.updateProfile({
      name: form.name,
      phone: form.phone,
      idCard: form.idCard || undefined,
      gender: form.gender
    })
    userStore.updateProfile(form.name, form.phone, form.idCard, form.gender)
    ElMessage.success('保存成功')
    isEditing.value = false
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
          v-if="auditStatus.certAuditStatus !== undefined"
          :type="auditStatusMap[auditStatus.certAuditStatus]?.type ?? 'info'"
          size="large"
        >
          {{ auditStatusMap[auditStatus.certAuditStatus]?.text ?? '未知' }}
        </el-tag>
        <span v-else class="no-data">暂无数据</span>
      </div>

      <!-- 查看模式 -->
      <template v-if="!isEditing">
        <div class="info-grid">
          <div class="info-item">
            <span class="label">姓名</span>
            <span class="value">{{ userStore.name || '-' }}</span>
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

.action-row {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>