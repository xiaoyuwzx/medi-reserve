<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 标题区域 -->
      <div class="login-header">
        <div class="logo">
          <el-icon :size="36" color="#409EFF"><Monitor /></el-icon>
        </div>
        <h2 class="title">医疗预约挂号平台</h2>
        <p class="subtitle">患者登录</p>
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
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <!-- 登录按钮 -->
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
        <router-link to="/patient/register" class="register-link">
          立即注册
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Phone, Lock, Monitor } from '@element-plus/icons-vue'
import { patientLogin } from '@/api/patient'
import { useUserStore } from '@/store/user'
import { isValidPhone } from '@/utils/validate'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)

// 表单数据
const form = reactive({
  username: '',
  password: '',
})

// 自定义手机号校验
const validatePhone = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!isValidPhone(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

// 表单校验规则
const rules = {
  username: [
    { required: true, validator: validatePhone, trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' },
  ],
}

// 登录处理
const handleLogin = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await patientLogin({
      username: form.username,
      password: form.password,
    })

    const { token, id, name, phone } = res.data

    // 保存用户信息到 store 和 localStorage
    userStore.setUser(token, {
      id,
      name,
      phone,
      role: 'PATIENT',
    })

    // 跳转到患者首页
    router.push('/patient/home')
  } catch {
    // 错误提示已由响应拦截器处理
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
  padding: 40px 36px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  margin-bottom: 12px;
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
  padding-top: 8px;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  margin-left: 4px;
}

.register-link:hover {
  text-decoration: underline;
}

/* ===== 响应式适配 ===== */

/* 平板 */
@media (max-width: 768px) {
  .login-card {
    width: 380px;
    padding: 32px 28px;
    border-radius: 10px;
  }

  .title {
    font-size: 20px;
  }
}

/* 手机 */
@media (max-width: 480px) {
  .login-container {
    padding: 16px;
    align-items: flex-start;
    padding-top: 40px;
    background: #fff;
  }

  .login-card {
    width: 100%;
    padding: 28px 20px;
    box-shadow: none;
    border-radius: 0;
  }

  .title {
    font-size: 18px;
  }

  .login-btn {
    height: 40px;
    font-size: 15px;
  }
}
</style>