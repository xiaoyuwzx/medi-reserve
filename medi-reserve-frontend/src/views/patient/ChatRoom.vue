<template>
  <div class="chat-room">
    <!-- 顶部信息栏 -->
    <div class="chat-header">
      <div class="header-left">
        <el-button text @click="leaveRoom">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <span class="chat-title">
          正在与 <strong>{{ roomInfo?.doctorName || '医生' }}</strong> 问诊
        </span>
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
        :class="msg.senderRole === 'PATIENT' ? 'msg-self' : 'msg-other'"
      >
        <div class="msg-bubble">
          <div class="msg-sender">{{ msg.senderRole === 'PATIENT' ? '我' : roomInfo?.doctorName || '医生' }}</div>
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
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getConsultationRoom, getChatHistory, endConsultation } from '@/api/doctor'
import { connect, sendMessage, disconnect } from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const appointmentId = Number(route.params.appointmentId)
const roomInfo = ref(null)
const messages = ref([])
const inputText = ref('')
const sending = ref(false)
const msgListRef = ref(null)

// 格式化时间
const formatTime = (str) => {
  if (!str) return ''
  return str.replace('T', ' ').substring(11, 16)
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (msgListRef.value) {
    msgListRef.value.scrollTop = msgListRef.value.scrollHeight
  }
}

// 初始化聊天
const initChat = async () => {
  // 获取问诊室信息
  try {
    const res = await getConsultationRoom(appointmentId)
    roomInfo.value = res.data
  } catch {
    //
  }

  // 加载历史消息
  try {
    const res = await getChatHistory(appointmentId, { page: 1, size: 50 })
    messages.value = res.data?.list || []
  } catch {
    messages.value = []
  }

  // 连接 WebSocket
  const token = localStorage.getItem('token')
  try {
    await connect(token, appointmentId, (msg) => {
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

// 发送消息
const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text) return

  inputText.value = ''
  sending.value = true
  try {
    sendMessage(appointmentId, roomInfo.value?.doctorId, text)
    // 乐观插入本地消息
    messages.value.push({
      senderRole: 'PATIENT',
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

// 结束问诊
const confirmEndConsultation = async () => {
  try {
    await ElMessageBox.confirm('确认结束问诊？结束后将无法发送消息。', '结束问诊', { type: 'warning' })
  } catch {
    return
  }
  try {
    await endConsultation(appointmentId)
    ElMessage.success('问诊已结束')
    leaveRoom()
  } catch {
    //
  }
}

// 离开房间
const leaveRoom = () => {
  disconnect()
  router.push('/patient/orders')
}

onMounted(() => {
  initChat()
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