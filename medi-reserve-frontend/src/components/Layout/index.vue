<template>
  <el-container style="height: 100vh;">
    <!-- 侧边栏 -->
    <el-aside width="200px" style="background-color: #304156;">
      <div class="logo">
        <span>MediReserve</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
          @click="item.path ? navigate(item.path) : openChangePassword()"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header style="background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: space-between; padding: 0 20px;">
        <div>
          <span style="font-size: 18px; font-weight: bold;">{{ pageTitle }}</span>
        </div>
        <div>
          <span style="margin-right: 15px; color: #666;">{{ userStore.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main style="background: #f0f2f5; padding: 20px;">
        <router-view />
      </el-main>
    </el-container>

    <!-- 修改密码弹窗 -->
    <ChangePasswordDialog v-model="showPasswordDialog" :role="userStore.role" />
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import ChangePasswordDialog from '@/components/ChangePasswordDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const showPasswordDialog = ref(false)

// 根据角色动态生成菜单
const menuItems = computed(() => {
  const role = userStore.role

  if (role === 'PATIENT') {
    return [
      { path: '/patient/home', title: '首页', icon: 'HomeFilled' },
      { path: '/patient/doctors', title: '找医生', icon: 'User' },
      { path: '/patient/orders', title: '我的预约', icon: 'List' },
      { path: '/patient/hot', title: '热门医生', icon: 'TrendCharts' },
      { path: '/patient/profile', title: '个人信息', icon: 'User' },
      { path: '', title: '修改密码', icon: 'Lock' },
    ]
  }

  if (role === 'DOCTOR') {
    return [
      { path: '/doctor/schedules', title: '排班管理', icon: 'Calendar' },
      { path: '/doctor/chat', title: '在线问诊', icon: 'ChatDotRound' },
      { path: '', title: '修改密码', icon: 'Lock' },
    ]
  }

  if (role === 'SUPER_ADMIN') {
    return [
      { path: '/admin/audit', title: '医生审核', icon: 'Check' },
      { path: '/admin/admins', title: '管理员管理', icon: 'User' },
      { path: '', title: '修改密码', icon: 'Lock' },
    ]
  }

  return []
})

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || 'MediReserve')

// 菜单导航（使用 hash 直接跳转，绕过 el-menu 的 router 模式兼容问题）
const navigate = (path) => {
  window.location.hash = `#${path}`
}

// 打开修改密码弹窗
const openChangePassword = () => {
  showPasswordDialog.value = true
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
}
.el-menu {
  border-right: none;
}
</style>