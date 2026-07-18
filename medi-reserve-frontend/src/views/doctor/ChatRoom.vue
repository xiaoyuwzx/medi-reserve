<template>
  <div class="chat-room">
    <!-- ===== 视图一：预约列表 ===== -->
    <template v-if="!currentAppointmentId">
      <div class="page-header">
        <h2 class="page-title">
          <el-icon color="#409EFF"><ChatLineSquare /></el-icon>
          在线问诊
        </h2>
      </div>

      <!-- 日期筛选 -->
      <div class="filter-bar">
        <span class="filter-label">日期</span>
        <el-date-picker
          v-model="filterDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          style="width: 180px;"
        />
        <el-button type="primary" @click="fetchAppointments">
          <el-icon><Search /></el-icon>查询
        </el-button>
      </div>

      <!-- 预约列表 -->
      <div v-loading="loading" class="appointment-list">
        <div v-if="appointments.length === 0 && !loading" class="empty-state">
          <el-icon :size="48" color="#c0c4cc"><FolderOpened /></el-icon>
          <p>暂无待问诊患者</p>
        </div>

        <div
          v-for="appt in appointments"
          :key="appt.id"
          class="appointment-card"
        >
          <div class="card-info">
            <div class="patient-name">
              <el-icon><UserFilled /></el-icon>
              {{ appt.patientName || '未知患者' }}
            </div>
            <div class="appointment-meta">
              <span>{{ appt.scheduleDate }}</span>
              <el-tag :type="appt.period === 1 ? 'warning' : 'info'" size="small">
                {{ appt.periodText }}
              </el-tag>
              <el-tag type="success" size="small">
                {{ appt.status === 1 ? '待问诊' : appt.status === 2 ? '已完成' : '其他' }}
              </el-tag>
            </div>
          </div>
          <div class="card-action">
            <el-button type="primary" size="small" @click="enterRoom(appt)">
              <el-icon><ChatDotRound /></el-icon>
              进入问诊室
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 30]"
          layout="total, prev, pager, next"
          @current-change="fetchAppointments"
          @size-change="fetchAppointments"
        />
      </div>
    </template>

    <!-- ===== 视图二：聊天室 ===== -->
    <template v-else>
      <!-- 顶部信息栏 -->
      <div class="chat-header">
        <div class="header-left">
          <el-button text @click="leaveRoom">
            <el-icon><ArrowLeft /></el-icon>返回列表
          </el-button>
          <span class="chat-title">
            正在与 <strong>{{ roomInfo?.patientName || '患者' }}</strong> 问诊
          </span>
        </div>
        <div class="header-right">
          <span class="online-dot" :class="roomInfo?.patientOnline ? 'online' : 'offline'"></span>
          {{ roomInfo?.patientOnline ? '患者在线' : '患者离线' }}
        </div>
      </div>

      <!-- 消息列表 -->
      <div ref="msgListRef" class="chat-messages">
        <div v-if="messages.length === 0" class="empty-chat">
          <p>暂无消息，请开始问诊</p>
        </div>
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="message-item"
          :class="msg.senderRole === 'DOCTOR' ? 'msg-self' : 'msg-other'"
        >
          <div class="msg-bubble">
            <div class="msg-sender">{{ msg.senderRole === 'DOCTOR' ? '医生' : '患者' }}</div>
            <div class="msg-content">{{ msg.content }}</div>
            <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="请输入消息..."
          @keyup.enter.exact="handleSend"
          :disabled="sending"
        />
        <div class="input-actions">
          <el-button type="primary" :loading="sending" @click="handleSend">
            发送
          </el-button>
          <el-button type="danger" plain @click="confirmEndConsultation">
            结束问诊
          </el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatLineSquare, Search, FolderOpened, UserFilled, ChatDotRound,
  ArrowLeft,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  getDoctorAppointments, getConsultationRoom, getChatHistory, endConsultation,
} from '@/api/doctor'
import { connect, sendMessage, disconnect } from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// ----- 列表视图 -----
const filterDate = ref(new Date().toISOString().slice(0, 10))
const appointments = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(20)

// ----- 聊天视图 -----
const currentAppointmentId = ref(route.params.appointmentId ? Number(route.params.appointmentId) : null)
const roomInfo = ref(null)
const messages = ref([])
const inputText = ref('')
const sending = ref(false)
const msgListRef = ref(null)

// ===== 列表逻辑 =====
const fetchAppointments = async () => {
  loading.value = true
  try {
    const res = await getDoctorAppointments({
      date: filterDate.value,
      page: page.value,
      size: size.value,
    })
    const data = res.data
    appointments.value = data.list || []
    total.value = data.total || 0
  } catch {
    appointments.value = []
  } finally {
    loading.value = false
  }
}

const enterRoom = (appt) => {
  router.replace({ path: `/doctor/chat/${appt.id}` })
  initChat(appt)
}

// ===== 聊天逻辑 =====
const initChat = async (appt) => {
  currentAppointmentId.value = appt.id

  // 获取问诊室信息
  try {
    const res = await getConsultationRoom(appt.id)
    roomInfo.value = res.data
  } catch {
    //
  }

  // 加载历史消息
  try {
    const res = await getChatHistory(appt.id, { page: 1, size: 50 })
    messages.value = res.data?.list || []
  } catch {
    messages.value = []
  }

  // 连接 WebSocket
  const token = localStorage.getItem('token')
  try {
    await connect(token, appt.id, (msg) => {
      // 跳过自己发送的消息（已乐观插入）
      if (msg.senderId === userStore.userId) return
      messages.value.push(msg)
      scrollToBottom()
    })
  } catch {
    ElMessage.error('连接问诊室失败')
  }

  scrollToBottom()
}

const scrollToBottom = async () => {
  await nextTick()
  if (msgListRef.value) {
    msgListRef.value.scrollTop = msgListRef.value.scrollHeight
  }
}

const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text) return

  inputText.value = ''
  sending.value = true
  try {
    sendMessage(currentAppointmentId.value, roomInfo.value?.patientId, text)
    // 乐观插入本地消息
    messages.value.push({
      senderRole: 'DOCTOR',
      content: text,
      createdAt: new Date().toISOString(),
    })
    scrollToBottom()
  } catch {
    //
  } finally {
    sending.value = false
  }
}

const confirmEndConsultation = async () => {
  try {
    await ElMessageBox.confirm('确认结束问诊？结束后患者将无法继续发送消息。', '结束问诊', { type: 'warning' })
  } catch {
    return
  }
  try {
    await endConsultation(currentAppointmentId.value)
    ElMessage.success('问诊已结束')
    leaveRoom()
    fetchAppointments()
  } catch {
    //
  }
}

const leaveRoom = () => {
  disconnect()
  currentAppointmentId.value = null
  roomInfo.value = null
  messages.value = []
  router.replace('/doctor/chat')
}

const formatTime = (str) => {
  if (!str) return ''
  return str.replace('T', ' ').substring(11, 16)
}

onMounted(() => {
  if (currentAppointmentId.value) {
    // 从路由参数进入聊天
    initChat({ id: currentAppointmentId.value })
  } else {
    fetchAppointments()
  }
})

onUnmounted(() => {
  disconnect()
})
</script>

<style scoped>
.chat-room {
  max-width: 800px;
  margin: 0 auto;
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

/* ===== 页面标题 ===== */
.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ===== 筛选 ===== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-label {
  font-size: 13px;
  color: #606266;
}

/* ===== 预约列表 ===== */
.appointment-list {
  flex: 1;
  overflow-y: auto;
}

.appointment-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 12px;
  transition: all 0.2s;
}

.appointment-card:hover {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
}

.patient-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.appointment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #909399;
}

/* ===== 聊天头部 ===== */
.chat-header {
  background: #fff;
  border-radius: 10px 10px 0 0;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-title {
  font-size: 15px;
  color: #303133;
}

.online-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
}

.online-dot.online {
  background: #67c23a;
}

.online-dot.offline {
  background: #c0c4cc;
}

/* ===== 消息列表 ===== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  background: #f0f2f5;
  padding: 20px;
}

.empty-chat {
  display: flex;
  justify-content: center;
  padding: 60px 0;
  color: #c0c4cc;
}

.message-item {
  margin-bottom: 16px;
  display: flex;
}

.msg-self {
  justify-content: flex-end;
}

.msg-other {
  justify-content: flex-start;
}

.msg-bubble {
  max-width: 60%;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.5;
}

.msg-self .msg-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 2px;
}

.msg-other .msg-bubble {
  background: #fff;
  color: #303133;
  border-bottom-left-radius: 2px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.msg-sender {
  font-size: 11px;
  opacity: 0.7;
  margin-bottom: 4px;
}

.msg-content {
  word-break: break-word;
}

.msg-time {
  font-size: 11px;
  opacity: 0.6;
  margin-top: 4px;
  text-align: right;
}

/* ===== 输入区域 ===== */
.chat-input {
  background: #fff;
  border-radius: 0 0 10px 10px;
  padding: 12px 20px;
  box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.input-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  justify-content: flex-end;
}

/* ===== 空状态 ===== */
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

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .chat-room {
    height: calc(100vh - 80px);
  }

  .msg-bubble {
    max-width: 80%;
  }
}
</style>