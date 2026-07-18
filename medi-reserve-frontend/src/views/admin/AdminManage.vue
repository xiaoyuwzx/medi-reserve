<template>
  <div class="admin-page">
    <div class="page-header">
      <h2 class="page-title">管理员管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>添加管理员
      </el-button>
    </div>

    <!-- 管理员列表 -->
    <div v-loading="loading" class="table-card">
      <div v-if="admins.length === 0 && !loading" class="empty-state">
        <p>暂无管理员数据</p>
      </div>

      <el-table v-else :data="admins" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">{{ maskPhone(row.phone) }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
        <el-table-column label="角色" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'info'" size="small">
              {{ row.role === 1 ? '超级管理员' : '普通管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="dark">
              {{ row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <template v-if="row.role !== 1">
              <el-button
                v-if="row.status === 1"
                type="danger"
                size="small"
                plain
                @click="handleToggleStatus(row)"
              >
                禁用
              </el-button>
              <el-button
                v-else
                type="success"
                size="small"
                plain
                @click="handleToggleStatus(row)"
              >
                启用
              </el-button>
            </template>
            <span v-else style="color: #c0c4cc; font-size: 13px;">不可操作</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 30]"
        layout="total, prev, pager, next"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>

    <!-- 添加管理员弹窗 -->
    <el-dialog v-model="dialogVisible" title="添加管理员" width="480px" destroy-on-close>
      <el-form
        ref="formRef"
        :model="addForm"
        :rules="addRules"
        label-position="top"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="addForm.username" placeholder="4-20位字母数字下划线" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="addForm.password" type="password" placeholder="6-20位字母数字组合" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="addForm.name" placeholder="请输入姓名" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="addForm.phone" placeholder="选填" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addForm.email" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addLoading" @click="handleAdd">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAdminList, updateAdminStatus } from '@/api/admin'
import { isValidPhone } from '@/utils/validate'
import request from '@/api/request'

const admins = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

// 添加弹窗
const dialogVisible = ref(false)
const formRef = ref(null)
const addLoading = ref(false)
const addForm = reactive({
  username: '',
  password: '',
  name: '',
  phone: '',
  email: '',
})

// 校验规则
const validateUsername = (_rule, value, callback) => {
  if (!value) callback(new Error('请输入用户名'))
  else if (!/^[a-zA-Z0-9_]{4,20}$/.test(value)) callback(new Error('4-20位字母数字下划线'))
  else callback()
}
const validatePhone = (_rule, value, callback) => {
  if (!value) callback()
  else if (!isValidPhone(value)) callback(new Error('手机号格式不正确'))
  else callback()
}
const addRules = {
  username: [{ required: true, validator: validateUsername, trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
}

const maskPhone = (phone) => {
  if (!phone || phone.length < 7) return phone
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}
const formatDateTime = (str) => {
  if (!str) return '-'
  return str.replace('T', ' ').substring(0, 16)
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getAdminList({ page: page.value, size: size.value })
    admins.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch { admins.value = [] } finally { loading.value = false }
}

const openAddDialog = () => {
  addForm.username = ''
  addForm.password = ''
  addForm.name = ''
  addForm.phone = ''
  addForm.email = ''
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

const handleAdd = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  addLoading.value = true
  try {
    // 使用 request 直接调用注册接口（已存在的 POST /admin/register）
    await request({ url: '/admin/register', method: 'post', data: { ...addForm } })
    ElMessage.success('管理员添加成功')
    dialogVisible.value = false
    fetchList()
  } catch { /* 拦截器已处理 */ } finally { addLoading.value = false }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}管理员 "${row.username}"？`, `${action}确认`, { type: 'warning' })
  } catch { return }
  try {
    await updateAdminStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch { /* 拦截器已处理 */ }
}

onMounted(() => fetchList())
</script>

<style scoped>
.admin-page { max-width: 1100px; margin: 0 auto; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0; }
.table-card { background: #fff; border-radius: 10px; padding: 16px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); min-height: 200px; }
.empty-state { display: flex; justify-content: center; padding: 60px 0; color: #c0c4cc; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 20px; }
</style>