<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { House, User, Document, Setting, Lock, DocumentCopy, Key, Collection, UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

function handleLogout() {
  userStore.clearToken()
  router.push('/admin/login')
}

const menuItems = [
  { path: '/admin', title: '首页', icon: House },
  { path: '/admin/audit/doctors', title: '医生审核', icon: User },
  { path: '/admin/audit/cert', title: '证件审核', icon: Document },
  { path: '/admin/admins', title: '管理员管理', icon: Setting },
  { path: '/admin/doctors', title: '医生管理', icon: User },
  { path: '/admin/patients', title: '患者管理', icon: UserFilled },
  { path: '/admin/password', title: '修改密码', icon: Lock },
  { path: '/admin/logs', title: '操作日志', icon: DocumentCopy },
  { path: '/admin/permissions/tree', title: '权限树', icon: Collection, adminOnly: true },
  { path: '/admin/permissions/roles', title: '角色权限', icon: Key, adminOnly: true }
]
</script>

<template>
  <el-container class="admin-layout">
    <!-- 左侧菜单 -->
    <el-aside width="220px" class="admin-aside">
      <div class="logo">
        <span class="logo-text">MediReserve</span>
        <span class="logo-sub">管理端</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <template v-for="item in menuItems" :key="item.path">
          <el-menu-item
            v-if="!item.adminOnly || userStore.role === 'SUPER_ADMIN'"
            :index="item.path"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="admin-header">
        <div class="header-left">
          <span class="header-title">MediReserve 管理端</span>
        </div>
        <div class="header-right">
          <span class="header-user">{{ userStore.name || userStore.username }}</span>
          <el-button text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-aside {
  background-color: #304156;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
}

.logo-sub {
  font-size: 11px;
  color: #909399;
}

.el-menu {
  border-right: none;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 0 20px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-user {
  font-size: 14px;
  color: #606266;
}

.admin-main {
  background: #f0f2f5;
  min-height: 0;
}
</style>