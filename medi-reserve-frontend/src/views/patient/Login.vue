<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { patientApi } from '@/api/patient'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入手机号和密码')
    return
  }

  loading.value = true
  try {
    const res = (await patientApi.patient.login({
      username: username.value,
      password: password.value
    })) as unknown as Record<string, string | number>

    console.log('🔍 患者登录响应:', res)

    userStore.setToken(res.token as string)
    userStore.setUserInfo(
      res.id as number,
      res.phone as string,
      'PATIENT',
      (res.name as string) || '',
      (res.phone as string) || '',
      (res.idCard as string) || '',
      (res.gender as number) || 0
    )
    ElMessage.success('登录成功')
    router.push('/patient')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>患者登录</h2>
      <el-form label-width="80px" @submit.prevent="handleLogin">
        <el-form-item label="手机号">
          <el-input v-model="username" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
      <p class="auth-link">
        还没有账号？
        <router-link to="/patient/register">去注册</router-link>
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
  width: 400px;
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
