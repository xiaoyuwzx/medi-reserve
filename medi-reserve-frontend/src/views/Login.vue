<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 标题区域 -->
      <div class="login-header">
        <div class="logo">
          <el-icon :size="40" color="#409EFF"><Monitor /></el-icon>
        </div>
        <h2 class="title">MediReserve</h2>
        <p class="subtitle">智慧医疗预约平台</p>
      </div>

      <!-- 角色切换 -->
      <div class="role-tabs">
        <el-radio-group v-model="role" @change="handleRoleChange" size="large">
          <el-radio-button value="patient">患者登录</el-radio-button>
          <el-radio-button value="doctor">医生登录</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="手机号" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
            maxlength="11"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <!-- 错误提示：审核状态相关 -->
        <el-alert
          v-if="auditError"
          :title="auditError"
          type="warning"
          show-icon
          :closable="false"
          style="margin-bottom: 18px;"
        />

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            :disabled="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部链接 -->
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link :to="registerPath" class="register-link">
          {{ role === 'patient' ? '患者注册' : '医生注册' }}
        </router-link>
      </div>

      <!-- 切换入口 -->
      <div class="switch-entry">
        <span class="switch-text">
          {{ role === 'patient' ? '医生请' : '患者请' }}
        </span>
        <el-button link type="primary" @click="switchRole">
          {{ role === 'patient' ? '切换到医生登录' : '切换到患者登录' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Phone, Lock } from '@element-plus/icons-vue'
import { patientLogin } from '@/api/patient'
import { doctorLogin } from '@/api/doctor'
import { useUserStore } from '@/store/user'
import { isValidPhone } from '@/utils/validate'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const auditError = ref('')

// 角色：patient / doctor（可通过 query 参数预设）
const role = ref(route.query.role === 'doctor' ? 'doctor' : 'patient')

// 表单
const form = reactive({
  username: '',
  password: '',
})

// 注册路径
const registerPath = computed(() => {
  return role.value === 'patient' ? '/patient/register' : '/doctor/register'
})

// 切换角色 → 清空表单
const handleRoleChange = () => {
  form.username = ''
  form.password = ''
  auditError.value = ''
  formRef.value?.clearValidate()
}

const switchRole = () => {
  role.value = role.value === 'patient' ? 'doctor' : 'patient'
  handleRoleChange()
}

// 手机号校验
const validatePhone = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!isValidPhone(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

// 表单规则
const rules = {
  username: [{ required: true, validator: validatePhone, trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' },
  ],
}

// 登录
const handleLogin = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  auditError.value = ''
  loading.value = true

  try {
    let res
    let userRole

    if (role.value === 'patient') {
      res = await patientLogin({ username: form.username, password: form.password })
      userRole = 'PATIENT'
    } else {
      res = await doctorLogin({ username: form.username, password: form.password })
      userRole = 'DOCTOR'
    }

    const { token, id, name, phone } = res.data
    userStore.setUser(token, { id, name, phone, role: userRole })

    // 根据角色跳转
    const redirectPath = userRole === 'PATIENT' ? '/patient/home' : '/doctor/schedules'
    router.push(redirectPath)
  } catch (err) {
    // 响应拦截器已 ElMessage.error，但审核相关错误码拦截器只会提示一次
    // 此处捕获错误消息判断是否审核相关，在页面中持久显示
    const msg = err?.message || ''
    if (msg.includes('审核') || msg.includes('管理员')) {
      auditError.value = msg
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f7ff 50%, #e0efff 100%);
  padding: 20px;
}

.login-card {
  width: 420px;
  max-width: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  padding: 36px 36px 28px;
}

/* ===== 标题区域 ===== */
.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.logo {
  margin-bottom: 10px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 4px;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

/* ===== 角色切换 ===== */
.role-tabs {
  margin-bottom: 24px;
}

.role-tabs :deep(.el-radio-group) {
  width: 100%;
}

.role-tabs :deep(.el-radio-button) {
  width: 50%;
}

.role-tabs :deep(.el-radio-button__inner) {
  width: 100%;
  text-align: center;
  font-size: 15px;
  padding: 10px 0;
}

/* ===== 表单 ===== */
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}

/* ===== 底部链接 ===== */
.login-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
  padding-top: 4px;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  margin-left: 4px;
}

.register-link:hover {
  text-decoration: underline;
}

/* ===== 切换入口 ===== */
.switch-entry {
  text-align: center;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #909399;
}

.switch-text {
  margin-right: 2px;
}

/* ===== 响应式适配 ===== */
@media (max-width: 768px) {
  .login-card {
    width: 380px;
    padding: 30px 28px 24px;
    border-radius: 10px;
  }

  .title {
    font-size: 22px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 16px;
    align-items: flex-start;
    padding-top: 30px;
    background: #fff;
  }

  .login-card {
    width: 100%;
    padding: 24px 20px 20px;
    box-shadow: none;
    border-radius: 0;
  }

  .title {
    font-size: 20px;
  }

  .login-btn {
    height: 40px;
    font-size: 15px;
  }
}
</style>