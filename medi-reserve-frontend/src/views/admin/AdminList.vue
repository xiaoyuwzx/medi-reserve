<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const list = ref<Record<string, unknown>[]>([])
const total = ref(0)
const loading = ref(false)

const queryParams = reactive({
  page: 1,
  size: 10
})

async function loadList() {
  loading.value = true
  try {
    const res = await adminApi.admin.listAdmins({
      page: queryParams.page,
      size: queryParams.size
    })
    const data = res as unknown as { list?: Record<string, unknown>[]; total?: number }
    list.value = data.list ?? []
    total.value = data.total ?? 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onPageChange(page: number) {
  queryParams.page = page
  loadList()
}

async function handleStatusChange(row: Record<string, unknown>) {
  if (row.id === userStore.userId) {
    ElMessage.warning('不能禁用自己')
    loadList()
    return
  }
  try {
    const msg = await adminApi.admin.updateStatus(row.id as number, {
      status: row.status as number
    })
    ElMessage.success((msg as unknown as string) || '状态更新成功')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
    loadList()
  }
}

// 新增弹窗
const dialogVisible = ref(false)
const createLoading = ref(false)
const formRef = ref<FormInstance>()

const createForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
  email: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '4-20位字母数字下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,20}$/, message: '6-20位字母和数字组合', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_r, v, cb) => {
        if (v !== createForm.password) cb(new Error('两次输入的密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

function openCreateDialog() {
  createForm.username = ''
  createForm.password = ''
  createForm.confirmPassword = ''
  createForm.name = ''
  createForm.phone = ''
  createForm.email = ''
  dialogVisible.value = true
}

async function handleCreate() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  createLoading.value = true
  try {
    await adminApi.admin.register({
      username: createForm.username,
      password: createForm.password,
      name: createForm.name,
      phone: createForm.phone || undefined,
      email: createForm.email || undefined,
      role: 2
    })
    ElMessage.success('新增管理员成功')
    dialogVisible.value = false
    loadList()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '新增失败')
  } finally {
    createLoading.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="admin-list-page">
    <div class="page-header">
      <h2 class="page-title">管理员管理</h2>
      <el-button v-if="userStore.role === 'SUPER_ADMIN'" type="primary" @click="openCreateDialog">
        新增管理员
      </el-button>
    </div>

    <div class="table-container" v-loading="loading">
      <el-table :data="list" stripe empty-text="暂无管理员">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'primary'" size="small">
              {{ row.role === 1 ? '超级管理员' : '普通管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val: boolean) => {
                row.status = val ? 1 : 0
                handleStatusChange(row)
              }"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
      </el-table>

      <div v-if="total > queryParams.size" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          :page-size="queryParams.size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="onPageChange"
        />
      </div>
    </div>

    <!-- 新增弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增管理员"
      width="480px"
      :close-on-click-modal="false"
      @close="formRef?.resetFields()"
    >
      <el-form
        ref="formRef"
        :model="createForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="4-20位字母数字下划线" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" placeholder="6-20位字母和数字组合" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="createForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createForm.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreate">
          确认新增
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-list-page {
  max-width: 1100px;
  padding: 24px 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>