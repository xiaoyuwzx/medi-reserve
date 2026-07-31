<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = (await adminApi.admin.login({
      username: username.value,
      password: password.value
    })) as unknown as Record<string, string | number>

    const roleName = (res.role as number) === 1 ? 'SUPER_ADMIN' : 'ADMIN'

    userStore.setToken(res.token as string)
    userStore.setUserInfo(
      res.id as number,
      (res.username as string) || '',
      roleName,
      (res.name as string) || '',
      (res.phone as string) || '',
      '',
      0
    )
    ElMessage.success('登录成功')
    router.push('/admin')
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
      <h2>管理员登录</h2>
      <el-form label-width="80px" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="username" placeholder="请输入管理员用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
      <p class="auth-hint">🔒 管理员入口</p>
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

.auth-hint {
  margin: 16px 0 0;
  text-align: center;
  color: #c0c4cc;
  font-size: 13px;
}
</style>