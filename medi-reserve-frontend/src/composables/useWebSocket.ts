import { ref, onUnmounted } from 'vue'
import SockJS from 'sockjs-client'
import { Client, type IFrame, type IMessage } from '@stomp/stompjs'
import type { ChatMessageVO } from '@/api/websocket/websocketApi'
import { useUserStore } from '@/stores/user'

export function useWebSocket() {
  const userStore = useUserStore()
  const client = ref<Client | null>(null)
  const isConnected = ref(false)
  const error = ref<string | null>(null)
  const connecting = ref(false)

  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let disconnectCalled = false

  function connect(appointmentId: number): Promise<void> {
    return new Promise((resolve, reject) => {
      if (client.value?.active) {
        resolve()
        return
      }

      const token = userStore.token
      if (!token) {
        const err = '未登录，无法建立连接'
        error.value = err
        reject(new Error(err))
        return
      }

      connecting.value = true
      error.value = null
      disconnectCalled = false

      const sock = new SockJS(
        `/api-websocket/ws/chat?token=${encodeURIComponent(token)}&appointmentId=${appointmentId}`
      )

      const stompClient = new Client({
        webSocketFactory: () => sock as any,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame: IFrame) => {
          isConnected.value = true
          connecting.value = false
          error.value = null
          resolve()
        },
        onStompError: (frame: IFrame) => {
          const errMsg = frame.headers?.['message'] || 'WebSocket 连接失败'
          error.value = errMsg
          connecting.value = false
          isConnected.value = false
          reject(new Error(errMsg))
        },
        onWebSocketClose: () => {
          isConnected.value = false
          if (!disconnectCalled && reconnectTimer === null) {
            error.value = '连接已断开，正在尝试重连...'
          }
        }
      })

      stompClient.activate()
      client.value = stompClient
    })
  }

  function disconnect(): void {
    disconnectCalled = true
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (client.value?.active) {
      client.value.deactivate()
    }
    client.value = null
    isConnected.value = false
    connecting.value = false
    error.value = null
  }

  function sendMessage(appointmentId: number, receiverId: number, content: string): void {
    if (!client.value?.active) {
      console.warn('WebSocket 未连接，无法发送消息')
      return
    }

    const payload = {
      appointmentId,
      receiverId,
      content,
      msgType: 1
    }

    client.value.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(payload)
    })
  }

  function subscribeRoom(
    appointmentId: number,
    callback: (msg: ChatMessageVO) => void
  ): void {
    if (!client.value?.active) return

    client.value.subscribe(`/topic/room/${appointmentId}`, (message: IMessage) => {
      try {
        const msg = JSON.parse(message.body) as ChatMessageVO
        callback(msg)
      } catch {
        console.warn('收到无法解析的消息:', message.body)
      }
    })
  }

  function subscribeUser(callback: (msg: ChatMessageVO) => void): void {
    if (!client.value?.active) return

    client.value.subscribe('/user/queue/messages', (message: IMessage) => {
      try {
        const msg = JSON.parse(message.body) as ChatMessageVO
        callback(msg)
      } catch {
        console.warn('收到无法解析的离线消息:', message.body)
      }
    })
  }

  // Ensure cleanup
  onUnmounted(() => {
    disconnect()
  })

  return {
    client,
    isConnected,
    connecting,
    error,
    connect,
    disconnect,
    sendMessage,
    subscribeRoom,
    subscribeUser
  }
}