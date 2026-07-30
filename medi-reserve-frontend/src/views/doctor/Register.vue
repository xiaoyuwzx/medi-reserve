<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { doctorApi } from '@/api/doctor'
import { useDictStore } from '@/stores/dict'

const router = useRouter()
const dictStore = useDictStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  name: '',
  phone: '',
  password: '',
  confirmPassword: '',
  departmentId: undefined as number | undefined,
  titleId: undefined as number | undefined,
  idCard: '',
  gender: undefined as number | undefined,
  specialty: '',
  introduction: ''
})

const phonePattern = /^1[3-9]\d{9}$/
const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,20}$/
const idCardPattern = /^[1-9]\d{16}[0-9Xx]$/

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phonePattern, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: passwordPattern, message: '密码需为6-20位字母和数字组合', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  titleId: [{ required: true, message: '请选择职称', trigger: 'change' }],
  idCard: [
    {
      validator: (_rule, value, callback) => {
        if (value && !idCardPattern.test(value)) {
          callback(new Error('请输入正确的18位身份证号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function handleRegister() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await doctorApi.doctor.register({
      name: form.name,
      phone: form.phone,
      password: form.password,
      departmentId: form.departmentId!,
      titleId: form.titleId!,
      idCard: form.idCard || undefined,
      gender: form.gender,
      specialty: form.specialty || undefined,
      introduction: form.introduction || undefined
    })
    ElMessage.success('注册成功，请等待管理员审核')
    router.push('/doctor/login')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>医生注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="6-20位字母和数字" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item label="科室" prop="departmentId">
          <el-select v-model="form.departmentId" placeholder="请选择科室" style="width: 100%">
            <el-option
              v-for="dept in dictStore.departments"
              :key="dept.id"
              :label="dept.department"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职称" prop="titleId">
          <el-select v-model="form.titleId" placeholder="请选择职称" style="width: 100%">
            <el-option
              v-for="title in dictStore.titles"
              :key="title.id"
              :label="title.name"
              :value="title.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="选填" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="选填" clearable style="width: 100%">
            <el-option label="未知" :value="0" />
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="擅长领域">
          <el-input v-model="form.specialty" placeholder="例如：冠心病、高血压（选填）" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="form.introduction" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister">注册</el-button>
        </el-form-item>
      </el-form>
      <p class="auth-link">
        已有账号？
        <router-link to="/doctor/login">去登录</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f7fa;
}

.auth-card {
  width: 500px;
  padding: 32px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.auth-card h2 {
  margin: 0 0 24px;
  text-align: center;
}

.auth-link {
  margin: 16px 0 0;
  text-align: center;
  color: #606266;
}
</style>