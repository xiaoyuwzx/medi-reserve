<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { patientApi } from '@/api/patient'

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
    await patientApi.patient.updateProfile({
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
</script>

<template>
  <div class="profile-page">
    <h2 class="page-title">个人信息</h2>

    <div class="profile-card">
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