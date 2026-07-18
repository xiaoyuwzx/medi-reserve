<template>
  <div class="audit-page">
    <h2 class="page-title">医生审核</h2>

    <!-- 待审核列表 -->
    <div v-loading="loading" class="table-card">
      <div v-if="doctors.length === 0 && !loading" class="empty-state">
        <el-icon :size="48" color="#c0c4cc"><FolderOpened /></el-icon>
        <p>暂无待审核医生</p>
      </div>

      <el-table v-else :data="doctors" stripe>
        <el-table-column prop="doctorName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">{{ maskPhone(row.phone) }}</template>
        </el-table-column>
        <el-table-column prop="departmentName" label="科室" width="100" />
        <el-table-column prop="titleName" label="职称" width="100" />
        <el-table-column prop="specialty" label="擅长领域" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="注册时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDetail(row)">
              审核
            </el-button>
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

    <!-- 审核弹窗 -->
    <el-dialog v-model="dialogVisible" title="医生审核详情" width="560px" destroy-on-close>
      <div v-if="currentDoctor" class="audit-detail">
        <div class="detail-row">
          <span class="detail-label">姓名</span>
          <span class="detail-value">{{ currentDoctor.doctorName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">手机号</span>
          <span class="detail-value">{{ maskPhone(currentDoctor.phone) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">科室</span>
          <span class="detail-value">{{ currentDoctor.departmentName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">职称</span>
          <span class="detail-value">{{ currentDoctor.titleName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">擅长领域</span>
          <span class="detail-value">{{ currentDoctor.specialty || '无' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">个人简介</span>
          <span class="detail-value detail-intro">{{ currentDoctor.introduction || '无' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">注册时间</span>
          <span class="detail-value">{{ formatDateTime(currentDoctor.createdAt) }}</span>
        </div>
      </div>

      <!-- 拒绝理由 -->
      <div v-if="showRejectInput" class="reject-section">
        <el-input
          v-model="rejectReason"
          type="textarea"
          :rows="3"
          placeholder="请输入驳回原因"
          maxlength="200"
          show-word-limit
        />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button
            v-if="!showRejectInput"
            type="danger"
            @click="showRejectInput = true"
          >
            驳回
          </el-button>
          <template v-else>
            <el-button @click="showRejectInput = false">取消驳回</el-button>
            <el-button
              type="danger"
              :loading="rejectLoading"
              :disabled="!rejectReason.trim()"
              @click="handleReject"
            >
              确认驳回
            </el-button>
          </template>
          <el-button
            type="success"
            :loading="approveLoading"
            @click="handleApprove"
          >
            审核通过
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderOpened } from '@element-plus/icons-vue'
import { getPendingDoctors, getAuditDetail, approveDoctor, rejectDoctor } from '@/api/admin'

// 列表
const doctors = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)

// 弹窗
const dialogVisible = ref(false)
const currentDoctor = ref(null)
const showRejectInput = ref(false)
const rejectReason = ref('')
const approveLoading = ref(false)
const rejectLoading = ref(false)

// 手机号脱敏
const maskPhone = (phone) => {
  if (!phone || phone.length < 7) return phone
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

// 时间格式化
const formatDateTime = (str) => {
  if (!str) return '-'
  return str.replace('T', ' ').substring(0, 16)
}

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await getPendingDoctors({ page: page.value, size: size.value })
    doctors.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch {
    doctors.value = []
  } finally {
    loading.value = false
  }
}

// 打开详情弹窗
const openDetail = async (row) => {
  try {
    // 列表行数据已包含基础信息（name/phone/departmentName/titleName 等）
    const baseInfo = {
      doctorId: row.doctorId,
      doctorName: row.name || row.doctorName,
      phone: row.phone,
      departmentName: row.departmentName,
      titleName: row.titleName,
      specialty: row.specialty || '',
      introduction: row.introduction || '',
      createdAt: row.createdAt,
    }

    // 调用审核详情接口获取审核专有字段
    const res = await getAuditDetail(row.doctorId)
    // 合并数据（审核详情中的 specialty/introduction 可能更新，覆盖）
    currentDoctor.value = {
      ...baseInfo,
      ...res.data,
    }

    showRejectInput.value = false
    rejectReason.value = ''
    dialogVisible.value = true
  } catch {
    // 错误由拦截器处理
  }
}

// 审核通过
const handleApprove = async () => {
  if (!currentDoctor.value) return

  try {
    await ElMessageBox.confirm('确认审核通过该医生的注册申请？', '审核通过', { type: 'success' })
  } catch {
    return
  }

  approveLoading.value = true
  try {
    await approveDoctor(currentDoctor.value.doctorId)
    ElMessage.success('审核通过成功')
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误由拦截器处理
  } finally {
    approveLoading.value = false
  }
}

// 驳回
const handleReject = async () => {
  if (!currentDoctor.value || !rejectReason.value.trim()) return

  rejectLoading.value = true
  try {
    await rejectDoctor(currentDoctor.value.doctorId, {
      rejectReason: rejectReason.value.trim(),
    })
    ElMessage.success('审核驳回成功')
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误由拦截器处理
  } finally {
    rejectLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.audit-page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px;
}

/* ===== 表格 ===== */
.table-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  min-height: 200px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  color: #c0c4cc;
  font-size: 14px;
}

.empty-state p {
  margin: 12px 0 0;
}

/* ===== 分页 ===== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* ===== 审核详情 ===== */
.audit-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  align-items: baseline;
}

.detail-label {
  width: 70px;
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}

.detail-value {
  font-size: 14px;
  color: #303133;
}

.detail-intro {
  white-space: pre-wrap;
  max-height: 120px;
  overflow-y: auto;
}

/* ===== 驳回区域 ===== */
.reject-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>