<template>
  <div class="profile-container">
    <div class="profile-card">
      <h2 class="profile-title">个人信息</h2>

      <!-- 展示模式 -->
      <div v-if="!isEditing" class="display-mode">
        <div class="info-item">
          <label>姓名</label>
          <span>{{ form.name || '-' }}</span>
        </div>
        <div class="info-item">
          <label>手机号</label>
          <span>{{ form.phone || '-' }}</span>
        </div>
        <div class="info-item">
          <label>性别</label>
          <span>{{ genderMap[form.gender] || '未知' }}</span>
        </div>
        <div class="info-item">
          <label>身份证号</label>
          <span>{{ form.idCard || '-' }}</span>
        </div>

        <el-button type="primary" style="width: 100%; margin-top: 20px;" @click="enterEdit">
          修改信息
        </el-button>
      </div>

      <!-- 编辑模式 -->
      <div v-else class="edit-mode">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入姓名" maxlength="20" clearable />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" clearable />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio :value="0">未知</el-radio>
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" placeholder="选填" maxlength="18" clearable />
          </el-form-item>
          <el-form-item>
            <div style="display: flex; gap: 12px;">
              <el-button type="primary" :loading="loading" @click="handleSave" style="flex: 1;">
                {{ loading ? '保存中...' : '保存' }}
              </el-button>
              <el-button @click="cancelEdit" style="flex: 1;">取消</el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { updateProfile } from '@/api/patient'
import { isValidPhone } from '@/utils/validate'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const isEditing = ref(false)

// 备份原始数据（取消时还原）
const backupData = ref({})

const form = reactive({
  name: '',
  phone: '',
  gender: 0,
  idCard: '',
})

const genderMap = { 0: '未知', 1: '男', 2: '女' }

// ========== 校验规则 ==========
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (!isValidPhone(value)) callback(new Error('手机号格式不正确'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  idCard: [
    {
      validator: (_rule, value, callback) => {
        if (!value) return callback()
        if (/^[1-9]\d{16}[0-9Xx]$/.test(value)) callback()
        else callback(new Error('身份证号格式不正确'))
      },
      trigger: 'blur',
    },
  ],
}

// ========== 初始化 ==========
const loadData = () => {
  const info = userStore.userInfo || {}
  form.name = info.name || ''
  form.phone = info.phone || ''
  form.gender = info.gender ?? 0
  form.idCard = info.idCard || ''
}

onMounted(loadData)

// ========== 进入编辑 ==========
const enterEdit = () => {
  backupData.value = { ...form }
  isEditing.value = true
}

// ========== 取消 ==========
const cancelEdit = () => {
  Object.assign(form, backupData.value)
  isEditing.value = false
}

// ========== 保存 ==========
const handleSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await updateProfile({
      name: form.name,
      phone: form.phone,
      gender: form.gender,
      idCard: form.idCard || undefined,
    })

    // ✅ 兼容两种响应结构
    const data = res?.data || res || {}

    // 更新 Token
    if (data.token) {
      localStorage.setItem('token', data.token)
    }

    // ✅ 修复：从 form 取所有字段，从响应取 name/phone
    const updated = {
      ...userStore.userInfo,
      name: data.name || form.name,
      phone: data.phone || form.phone,
      gender: form.gender,
      idCard: form.idCard,
    }
    userStore.setUserInfo(updated)

    // ✅ 关键修复：从 store 重新加载数据到 form（确保展示模式显示最新值）
    loadData()

    ElMessage.success('个人信息修改成功')
    isEditing.value = false
  } catch (error) {
    console.error('保存个人信息失败:', error)
    // 错误已由拦截器处理，这里只做日志
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-container {
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  background: #f5f7fa;
  padding: 40px 20px;
}

.profile-card {
  width: 520px;
  max-width: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 32px 36px 24px;
}

.profile-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 24px;
  text-align: center;
}

/* 展示模式 */
.display-mode .info-item {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #ebeef5;
}

.display-mode .info-item:last-child {
  border-bottom: none;
}

.display-mode .info-item label {
  width: 80px;
  flex-shrink: 0;
  font-size: 14px;
  color: #909399;
}

.display-mode .info-item span {
  font-size: 15px;
  color: #303133;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}
</style>