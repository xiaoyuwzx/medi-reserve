<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { websocketApi } from '@/api/websocket'
import { useWebSocket } from '@/composables/useWebSocket'
import { useUserStore } from '@/stores/user'
import type { ChatMessageVO, ConsultationRoomVO } from '@/api/websocket/websocketApi'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isConnected, connecting, error, connect, disconnect, sendMessage, subscribeRoom, subscribeUser } = useWebSocket()

const appointmentId = Number(route.params.appointmentId)

// ========== 房间信息 ==========
const roomInfo = ref<ConsultationRoomVO | null>(null)
const roomLoading = ref(false)

async function loadRoomInfo() {
  roomLoading.value = true
  try {
    const res = await websocketApi.consultation.getRoomInfo(appointmentId)
    roomInfo.value = res as unknown as ConsultationRoomVO
  } catch {
    roomInfo.value = null
  } finally {
    roomLoading.value = false
  }
}

// ========== 消息列表 ==========
const messages = ref<ChatMessageVO[]>([])
const msgLoading = ref(false)
const msgPage = reactive({ page: 1, size: 20 })
const hasMore = ref(true)
const msgListRef = ref<HTMLDivElement>()

async function loadHistory() {
  msgLoading.value = true
  try {
    const res = await websocketApi.consultation.getHistory(appointmentId, {
      page: msgPage.page,
      size: msgPage.size
    })
    const data = res as unknown as { list?: ChatMessageVO[]; total?: number }

    const newMessages = (data.list ?? []).map((msg: any) => {
      return { ...msg, isSelf: Number(msg.senderId) === Number(userStore.userId) }
    })

    messages.value = [...newMessages, ...messages.value]
    hasMore.value = (data.total ?? 0) > messages.value.length
  } catch {
    // ignore
  } finally {
    msgLoading.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    }
  })
}

function handleScroll() {
  if (!msgListRef.value || !hasMore.value) return
  if (msgListRef.value.scrollTop === 0) {
    msgPage.page++
    loadHistory()
  }
}

// ========== 发送消息 ==========
const inputContent = ref('')
const sending = ref(false)

async function handleSend() {
  const content = inputContent.value.trim()
  if (!content) return

  if (!isConnected.value) {
    ElMessage.warning('连接已断开，无法发送消息')
    return
  }

  const receiverId = userStore.role === 'PATIENT'
    ? (roomInfo.value?.doctorId ?? 0)
    : (roomInfo.value?.patientId ?? 0)

  sending.value = true
  try {
    sendMessage(appointmentId, receiverId, content)
    inputContent.value = ''
  } catch {
    // ignore
  } finally {
    sending.value = false
  }
}

// ========== 接收实时消息 ==========
function onRoomMessage(msg: ChatMessageVO) {
  msg.isSelf = Number(msg.senderId) === Number(userStore.userId)
  messages.value.push(msg)
  scrollToBottom()
}

function onUserMessage(msg: ChatMessageVO) {
  msg.isSelf = Number(msg.senderId) === Number(userStore.userId)
  const alreadyExists = messages.value.some(m => m.messageId === msg.messageId)
  if (!alreadyExists) {
    messages.value.push(msg)
    scrollToBottom()
  }
}

// ========== 结束问诊 ==========
async function handleEndConsultation() {
  try {
    await ElMessageBox.confirm('确定要结束本次问诊吗？', '确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    await websocketApi.consultation.endConsultation(appointmentId)
    ElMessage.success('问诊已结束')
    disconnect()
    router.back()
  } catch (err) {
    ElMessage.error(err instanceof Error ? err.message : '操作失败')
  }
}

function goBack() {
  router.back()
}

// ========== 生命周期 ==========
onMounted(async () => {
  await loadRoomInfo()
  await loadHistory()

  try {
    await connect(appointmentId)
    // 连接成功后重新获取房间信息，更新在线人数
    await loadRoomInfo()
    subscribeRoom(appointmentId, onRoomMessage)
    subscribeUser(onUserMessage)
    scrollToBottom()
  } catch (err) {
    ElMessage.error(err instanceof Error ? err.message : '连接失败')
  }
})

onUnmounted(() => {
  disconnect()
})

watch(() => messages.value.length, () => {
  scrollToBottom()
})
</script>

<template>
  <div class="consultation-room">
    <!-- 顶部导航 -->
    <div class="header-bar">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <div class="header-info">
        <span class="header-name">
          {{ userStore.role === 'PATIENT' ? (roomInfo?.doctorName || '医生') : (roomInfo?.patientName || '患者') }}
        </span>
        <el-tag v-if="isConnected" type="success" size="small">在线</el-tag>
        <el-tag v-else-if="connecting" type="warning" size="small">连接中...</el-tag>
        <el-tag v-else type="info" size="small">离线</el-tag>
      </div>
      <el-button v-if="roomInfo?.status === 1" type="danger" size="small" @click="handleEndConsultation">
        结束问诊
      </el-button>
    </div>

    <!-- 房间信息 -->
    <div class="room-info" v-if="roomInfo">
      <span class="info-item">{{ roomInfo.departmentName }}</span>
      <span class="info-sep">·</span>
      <span class="info-item">{{ roomInfo.scheduleDate }}</span>
      <span class="info-sep">·</span>
      <span class="info-item">在线 {{ roomInfo.onlineCount }} 人</span>
    </div>

    <!-- 连接错误提示 -->
    <el-alert v-if="error" :title="error" type="error" show-icon class="error-bar" />

    <!-- 消息列表 -->
    <div
      ref="msgListRef"
      class="message-list"
      @scroll="handleScroll"
    >
      <div v-if="msgLoading" class="loading-hint">加载中...</div>
      <div
        v-for="msg in messages"
        :key="msg.messageId"
        class="message-bubble-row"
        :class="{ 'is-self': msg.isSelf }"
      >
        <div v-if="!msg.isSelf" class="sender-name">{{ msg.senderName }}</div>
        <div class="bubble" :class="msg.isSelf ? 'bubble-self' : 'bubble-other'">
          {{ msg.content }}
        </div>
        <div class="msg-time">{{ msg.sendTime?.substring(11, 16) }}</div>
      </div>
      <div v-if="!hasMore && messages.length > 0" class="loading-hint">— 暂无更多消息 —</div>
      <div v-if="messages.length === 0 && !msgLoading" class="empty-hint">
        暂无消息，开始对话吧
      </div>
    </div>

    <!-- 输入区 -->
    <div class="input-area">
      <el-input
        v-model="inputContent"
        type="textarea"
        :rows="2"
        placeholder="输入消息..."
        :disabled="!isConnected || roomInfo?.status === 0"
        @keyup.enter.exact="handleSend"
      />
      <el-button
        type="primary"
        :disabled="!isConnected || roomInfo?.status === 0"
        :loading="sending"
        @click="handleSend"
      >
        发送
      </el-button>
    </div>
    <div v-if="roomInfo?.status === 0" class="end-hint">本次问诊已结束</div>
  </div>
</template>

<style scoped>
.consultation-room {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
  max-width: 750px;
  margin: 0 auto;
}

/* 顶部导航 */
.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.header-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 房间信息 */
.room-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  font-size: 12px;
  color: #909399;
}

.info-sep {
  color: #dcdfe6;
}

.error-bar {
  margin: 0;
  border-radius: 0;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.loading-hint,
.empty-hint {
  text-align: center;
  color: #c0c4cc;
  font-size: 13px;
  padding: 20px 0;
}

.message-bubble-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  max-width: 75%;
}

.message-bubble-row.is-self {
  align-self: flex-end;
  align-items: flex-end;
}

.sender-name {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}

.bubble {
  display: inline-block;
  max-width: 100%;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.bubble-other {
  background: #fff;
  color: #303133;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.bubble-self {
  background: #409eff;
  color: #fff;
}

.msg-time {
  font-size: 11px;
  color: #c0c4cc;
}

/* 输入区 */
.input-area {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  align-items: flex-end;
}

.input-area :deep(.el-textarea__inner) {
  resize: none;
}

.end-hint {
  text-align: center;
  padding: 8px;
  font-size: 13px;
  color: #f56c6c;
  background: #fef0f0;
}
</style>