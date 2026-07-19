<template>
  <el-dialog
    v-model="visible"
    title="修改密码"
    width="420px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          placeholder="请输入旧密码"
          show-password
          clearable
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          placeholder="6-20位字母和数字组合"
          show-password
          clearable
        />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
          clearable
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ loading ? '提交中...' : '确认修改' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { updatePassword as patientUpdate } from '@/api/patient'
import { updatePassword as doctorUpdate } from '@/api/doctor'
import { updatePassword as adminUpdate } from '@/api/admin'
import { isValidPassword } from '@/utils/validate'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  role: { type: String, required: true }, // 'PATIENT' | 'DOCTOR' | 'SUPER_ADMIN'
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const visible = ref(props.modelValue)

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) resetForm()
  },
)

watch(visible, (val) => emit('update:modelValue', val))

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 校验确认密码一致性
const validateConfirm = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const validateNewPassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (!isValidPassword(value)) {
    callback(new Error('密码需为6-20位字母和数字组合'))
  } else if (value === form.oldPassword) {
    callback(new Error('新密码不能与旧密码相同'))
  } else {
    if (form.confirmPassword) {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, validator: validateNewPassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }],
}

const resetForm = () => {
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
  formRef.value?.clearValidate()
}

const handleClosed = () => {
  resetForm()
}

// 根据角色选择 API
const getApi = () => {
  if (props.role === 'DOCTOR') return doctorUpdate
  if (props.role === 'SUPER_ADMIN') return adminUpdate
  return patientUpdate
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await getApi()({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    // 清除 token 并跳转登录
    userStore.logout()
    setTimeout(() => {
      router.push('/login')
    }, 500)
  } catch {
    // 错误由拦截器处理
  } finally {
    loading.value = false
  }
}
</script>