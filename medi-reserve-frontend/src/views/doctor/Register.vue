<template>
  <div class="register-container">
    <div class="register-card">
      <!-- 标题区域 -->
      <div class="register-header">
        <div class="logo">
          <el-icon :size="36" color="#409EFF"><Monitor /></el-icon>
        </div>
        <h2 class="title">MediReserve</h2>
        <p class="subtitle">医生注册</p>
      </div>

      <!-- 注册表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleRegister"
      >
        <!-- ===== 基础信息 ===== -->
        <div class="section-divider">
          <span class="section-label">基础信息</span>
        </div>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input
                v-model="form.name"
                placeholder="请输入真实姓名"
                :prefix-icon="User"
                maxlength="20"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="请输入手机号"
                :prefix-icon="Phone"
                maxlength="11"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
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
          </el-col>
          <el-col :span="12">
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
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科室" prop="departmentId">
              <el-select
                v-model="form.departmentId"
                placeholder="请选择科室"
                clearable
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="dept in departments"
                  :key="dept.id"
                  :label="dept.department"
                  :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="titleId">
              <el-select
                v-model="form.titleId"
                placeholder="请选择职称"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="t in titles"
                  :key="t.id"
                  :label="t.name"
                  :value="t.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :value="0">未知</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input
                v-model="form.idCard"
                placeholder="选填，18位身份证号码"
                :prefix-icon="Postcard"
                maxlength="18"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- ===== 专业信息（可折叠） ===== -->
        <div class="section-divider section-collapsible" @click="toggleProfessional">
          <span class="section-label">专业信息</span>
          <span class="collapse-toggle">
            <el-icon>
              <ArrowDown v-if="!showProfessional" />
              <ArrowUp v-else />
            </el-icon>
            {{ showProfessional ? '收起' : '展开' }}
          </span>
        </div>

        <!-- 专业信息内容（v-show 控制显示/隐藏） -->
        <div v-show="showProfessional" class="professional-content">
          <el-form-item label="擅长领域" prop="specialty">
            <el-input
              v-model="form.specialty"
              placeholder="请输入擅长的疾病或治疗领域，如：心血管疾病、高血压"
              maxlength="200"
              clearable
            />
          </el-form-item>
          <el-form-item label="个人简介" prop="introduction">
            <el-input
              v-model="form.introduction"
              type="textarea"
              :rows="3"
              placeholder="请输入个人简介（选填，最多500字）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </div>

        <!-- 注册按钮 -->
        <el-form-item style="margin-top: 8px;">
          <el-button
            type="primary"
            class="register-btn"
            :loading="loading"
            :disabled="loading"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '提交注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部链接 -->
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login?role=doctor" class="login-link">
          去登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Phone, Lock, Postcard, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { doctorRegister } from '@/api/doctor'
import { getDepartments, getTitles } from '@/api/patient'
import { isValidPhone, isValidPassword } from '@/utils/validate'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const departments = ref([])
const titles = ref([])

// 控制专业信息展开/收起
const showProfessional = ref(false)

// 表单
const form = reactive({
  name: '',
  phone: '',
  password: '',
  confirmPassword: '',
  departmentId: null,
  titleId: null,
  gender: 0,
  idCard: '',
  specialty: '',
  introduction: '',
})

// ---- 切换专业信息展开/收起 ----
const toggleProfessional = () => {
  showProfessional.value = !showProfessional.value
}

// ---- 校验规则 ----
const validateConfirmPassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const validatePhone = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!isValidPhone(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const validatePassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (!isValidPassword(value)) {
    callback(new Error('密码需为6-20位字母和数字组合'))
  } else {
    if (form.confirmPassword) {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validateIdCard = (_rule, value, callback) => {
  if (!value) {
    callback()
  } else if (!/^[1-9]\d{16}[0-9Xx]$/.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 1, max: 20, message: '姓名长度 1-20 个字符', trigger: 'blur' },
  ],
  phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
  password: [{ required: true, validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  titleId: [{ required: true, message: '请选择职称', trigger: 'change' }],
  idCard: [{ validator: validateIdCard, trigger: 'blur' }],
}

// ---- 数据加载 ----
const fetchDicts = async () => {
  try {
    const [deptRes, titleRes] = await Promise.all([
      getDepartments(),
      getTitles(),
    ])
    departments.value = deptRes.data || []
    titles.value = titleRes.data || []
  } catch {
    // 错误由拦截器处理
  }
}

// ---- 注册 ----
const handleRegister = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await doctorRegister({
      name: form.name,
      phone: form.phone,
      password: form.password,
      departmentId: form.departmentId,
      titleId: form.titleId,
      gender: form.gender,
      idCard: form.idCard || undefined,
      specialty: form.specialty || undefined,
      introduction: form.introduction || undefined,
    })
    ElMessage.success('注册成功，请等待管理员审核')
    setTimeout(() => {
      router.push('/login?role=doctor')
    }, 2000)
  } catch {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDicts()
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f7ff 50%, #e0efff 100%);
  padding: 24px 20px;
}

.register-card {
  width: 650px;
  max-width: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  padding: 36px 36px 28px;
}

/* ===== 标题区域 ===== */
.register-header {
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
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* ===== 分隔线（可折叠） ===== */
.section-divider {
  display: flex;
  align-items: center;
  margin: 4px 0 16px;
}

.section-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #ebeef5;
  margin-left: 12px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  flex-shrink: 0;
}

/* ===== 折叠切换按钮 ===== */
.section-collapsible {
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
  padding: 4px 0;
  border-radius: 4px;
}

.section-collapsible:hover {
  background: #f5f7fa;
}

.section-collapsible .section-label {
  cursor: pointer;
}

.collapse-toggle {
  font-size: 13px;
  color: #909399;
  margin-left: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  transition: color 0.2s;
}

.section-collapsible:hover .collapse-toggle {
  color: #409eff;
}

/* ===== 专业信息内容（展开/收起动画） ===== */
.professional-content {
  overflow: hidden;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== 表单 ===== */
.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}

/* ===== 底部链接 ===== */
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

/* ===== 表单项间距 ===== */
:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  padding-bottom: 2px;
}

/* ===== 响应式适配 ===== */
@media (max-width: 768px) {
  .register-card {
    width: 100%;
    padding: 28px 24px 22px;
    border-radius: 10px;
  }

  .title {
    font-size: 22px;
  }

  :deep(.el-col-12) {
    flex: 0 0 100%;
    max-width: 100%;
  }
}

@media (max-width: 480px) {
  .register-container {
    padding: 16px;
    padding-top: 20px;
    background: #fff;
  }

  .register-card {
    padding: 20px 16px 18px;
    box-shadow: none;
    border-radius: 0;
  }

  .title {
    font-size: 20px;
  }

  .register-btn {
    height: 40px;
    font-size: 15px;
  }

  :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  .collapse-toggle {
    font-size: 12px;
  }
}
</style>