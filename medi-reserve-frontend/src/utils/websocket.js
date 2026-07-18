import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let stompClient = null

/**
 * 连接 WebSocket
 * @param {string} token - JWT Token
 * @param {number} appointmentId - 预约ID
 * @param {Function} onMessage - 收到消息的回调
 */
export function connect(token, appointmentId, onMessage) {
  return new Promise((resolve, reject) => {
    if (stompClient?.connected) {
      resolve(stompClient)
      return
    }

    stompClient = new Client({
      webSocketFactory: () => new SockJS(`/ws/chat?token=${token}&appointmentId=${appointmentId}`),
      reconnectDelay: 5000,
      onConnect: () => {
        stompClient.subscribe(`/topic/room/${appointmentId}`, (message) => {
          const body = JSON.parse(message.body)
          onMessage?.(body)
        })
        resolve(stompClient)
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message'])
        reject(new Error(frame.headers['message']))
      },
      onDisconnect: () => {
        stompClient = null
      },
    })

    stompClient.activate()
  })
}

/**
 * 发送聊天消息
 * @param {number} appointmentId - 预约ID
 * @param {number} receiverId - 接收者ID
 * @param {string} content - 消息内容
 */
export function sendMessage(appointmentId, receiverId, content) {
  if (!stompClient?.connected) {
    console.warn('WebSocket not connected')
    return
  }
  stompClient.publish({
    destination: '/app/chat.send',
    body: JSON.stringify({
      appointmentId,
      receiverId,
      content,
    }),
  })
}

/**
 * 断开 WebSocket
 */
export function disconnect() {
  if (stompClient?.connected) {
    stompClient.deactivate()
    stompClient = null
  }
}