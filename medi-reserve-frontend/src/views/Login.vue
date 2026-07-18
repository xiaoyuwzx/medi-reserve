<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 标题区域 -->
      <div class="login-header">
        <div class="logo">
          <el-icon :size="40" color="#409EFF"><Monitor /></el-icon>
        </div>
        <h2 class="title">MediReserve</h2>
        <p class="subtitle">
          {{ role === 'admin' ? '管理员登录' : '智慧医疗预约平台' }}
        </p>
      </div>

      <!-- 角色切换 Tab（管理员模式不显示） -->
      <div v-if="showTabs" class="role-tabs">
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
        <el-form-item :label="accountLabel" prop="username">
          <el-input
            v-model="form.username"
            :placeholder="accountPlaceholder"
            :prefix-icon="User"
            maxlength="30"
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

        <el-alert
          v-if="auditError && role !== 'admin'"
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

      <!-- 底部链接（管理员不显示） -->
      <div v-if="role !== 'admin'" class="login-footer">
        <span>还没有账号？</span>
        <router-link :to="registerPath" class="register-link">
          {{ role === 'patient' ? '患者注册' : '医生注册' }}
        </router-link>
      </div>

      <!-- 切换入口（管理员显示切换入口） -->
      <div v-if="role === 'admin'" class="switch-entry">
        <span class="switch-text">管理员</span>
        <el-button link type="primary" @click="switchTo('patient')">
          切换到患者登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter, useRoute, onBeforeRouteUpdate } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { patientLogin } from '@/api/patient'
import { doctorLogin } from '@/api/doctor'
import { adminLogin } from '@/api/admin'
import { useUserStore } from '@/store/user'
import { isValidPhone } from '@/utils/validate'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const auditError = ref('')

// 角色：从 URL 参数读取，默认为 patient
const role = ref(
  route.query.role === 'doctor' ? 'doctor'
    : route.query.role === 'admin' ? 'admin'
      : 'patient',
)

// 管理员模式不显示 Tab
const showTabs = computed(() => role.value !== 'admin')

// 监听路由变化，当 role 参数变化时更新
onBeforeRouteUpdate((to) => {
  const newRole = to.query.role === 'doctor' ? 'doctor'
    : to.query.role === 'admin' ? 'admin'
    : 'patient'
  if (newRole !== role.value) {
    role.value = newRole
    form.username = ''
    form.password = ''
    auditError.value = ''
    formRef.value?.clearValidate()
  }
})

// 表单
const form = reactive({
  username: '',
  password: '',
})

// 动态标签和占位符
const accountLabel = computed(() => role.value === 'admin' ? '用户名' : '手机号')
const accountPlaceholder = computed(() => role.value === 'admin' ? '请输入用户名' : '请输入手机号')

// 注册路径
const registerPath = computed(() => role.value === 'patient' ? '/patient/register' : '/doctor/register')

// 切换角色
const handleRoleChange = () => {
  form.username = ''
  form.password = ''
  auditError.value = ''
  formRef.value?.clearValidate()
}

const switchTo = (targetRole) => {
  role.value = targetRole
  handleRoleChange()
}

// 账号校验
const validateAccount = (_rule, value, callback) => {
  if (!value) {
    callback(new Error(role.value === 'admin' ? '请输入用户名' : '请输入手机号'))
  } else if (role.value !== 'admin' && !isValidPhone(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, validator: validateAccount, trigger: 'blur' }],
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
    } else if (role.value === 'doctor') {
      res = await doctorLogin({ username: form.username, password: form.password })
      userRole = 'DOCTOR'
    } else {
      res = await adminLogin({ username: form.username, password: form.password })
      userRole = 'SUPER_ADMIN'
    }

    const { token, id, name, phone } = res.data
    userStore.setUser(token, { id, name, phone: phone || '', role: userRole })

    const redirectMap = {
      PATIENT: '/patient/home',
      DOCTOR: '/doctor/schedules',
      SUPER_ADMIN: '/admin/audit',
    }
    router.push(redirectMap[userRole] || '/patient/home')
  } catch (err) {
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

/* ===== 切换入口（管理员） ===== */
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