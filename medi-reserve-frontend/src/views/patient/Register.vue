<template>
  <div class="register-container">
    <div class="register-card">
      <!-- 标题区域 -->
      <div class="register-header">
        <div class="logo">
          <el-icon :size="36" color="#409EFF"><Monitor /></el-icon>
        </div>
        <h2 class="title">医疗预约挂号平台</h2>
        <p class="subtitle">患者注册</p>
      </div>

      <!-- 注册表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleRegister"
      >
        <!-- 姓名 -->
        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入真实姓名"
            :prefix-icon="User"
            maxlength="20"
            clearable
          />
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
            maxlength="11"
            clearable
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="6-20位字母和数字组合"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <!-- 身份证号（选填） -->
        <el-form-item label="身份证号" prop="idCard">
          <el-input
            v-model="form.idCard"
            placeholder="选填，18位身份证号码"
            :prefix-icon="Postcard"
            maxlength="18"
            clearable
          />
        </el-form-item>

        <!-- 性别（选填） -->
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            class="register-btn"
            :loading="loading"
            :disabled="loading"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部链接 -->
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/patient/login" class="login-link">
          去登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Phone, Lock, Postcard } from '@element-plus/icons-vue'
import { patientRegister } from '@/api/patient'
import { isValidPhone, isValidPassword } from '@/utils/validate'

const router = useRouter()

const formRef = ref(null)
const loading = ref(false)

// 表单数据
const form = reactive({
  name: '',
  phone: '',
  password: '',
  confirmPassword: '',
  idCard: '',
  gender: 0,  // 默认：未知
})

// 自定义确认密码校验
const validateConfirmPassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

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

// 自定义密码校验
const validatePassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (!isValidPassword(value)) {
    callback(new Error('密码需为6-20位字母和数字组合'))
  } else {
    // 密码变化后，触发确认密码重新校验
    if (form.confirmPassword) {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

// 自定义身份证校验（选填，填了才校验格式）
const validateIdCard = (_rule, value, callback) => {
  if (!value) {
    callback()
  } else if (!/^[1-9]\d{16}[0-9Xx]$/.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 1, max: 20, message: '姓名长度 1-20 个字符', trigger: 'blur' },
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' },
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' },
  ],
  idCard: [
    { validator: validateIdCard, trigger: 'blur' },
  ],
}

// 注册处理
const handleRegister = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    // 构建请求体，idCard 为空则不传
    const payload = {
      name: form.name,
      phone: form.phone,
      password: form.password,
      gender: form.gender,
    }
    if (form.idCard) {
      payload.idCard = form.idCard
    }

    await patientRegister(payload)

    ElMessage.success('注册成功，请登录')
    // 3 秒后跳转到登录页
    setTimeout(() => {
      router.push('/patient/login')
    }, 3000)
  } catch {
    // 错误提示已由响应拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f7ff 50%, #e0efff 100%);
  padding: 20px;
}

.register-card {
  width: 420px;
  max-width: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  padding: 36px 36px 28px;
}

.register-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo {
  margin-bottom: 10px;
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  margin-top: 4px;
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
  padding-top: 4px;
}

.login-link {
  color: #409eff;
  text-decoration: none;
  margin-left: 4px;
}

.login-link:hover {
  text-decoration: underline;
}

/* ===== Element Plus 表单项间距微调 ===== */
:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  padding-bottom: 2px;
}

/* ===== 响应式适配 ===== */

/* 平板 */
@media (max-width: 768px) {
  .register-card {
    width: 380px;
    padding: 30px 28px 24px;
    border-radius: 10px;
  }

  .title {
    font-size: 20px;
  }
}

/* 手机 */
@media (max-width: 480px) {
  .register-container {
    padding: 16px;
    align-items: flex-start;
    padding-top: 30px;
    background: #fff;
  }

  .register-card {
    width: 100%;
    padding: 24px 20px 20px;
    box-shadow: none;
    border-radius: 0;
  }

  .title {
    font-size: 18px;
  }

  .register-btn {
    height: 40px;
    font-size: 15px;
  }

  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}
</style>