import httpInstance from '@/utils/http'

// 创建/获取会话
export function createConversation(user1Id, user2Id, houseId, type = 0) {
    return httpInstance({ url: '/chat/conversation', method: 'post', params: { user1Id, user2Id, houseId, type } })
}

// 获取我的会话列表
export function getConversations(userId) {
    return httpInstance({ url: '/chat/conversations', method: 'get', params: { userId } })
}

// 获取历史消息
export function getMessages(conversationId, page = 1, size = 50) {
    return httpInstance({ url: `/chat/messages/${conversationId}`, method: 'get', params: { page, size } })
}

// 通过HTTP发送消息（备用，主要通过WebSocket发）
export function sendMessage(data) {
    return httpInstance({ url: '/chat/send', method: 'post', data })
}

// 标记已读
export function markAsRead(conversationId, userId) {
    return httpInstance({ url: `/chat/read/${conversationId}`, method: 'put', params: { userId } })
}

// 获取未读数
export function getChatUnreadCount(userId) {
    return httpInstance({ url: '/chat/unread', method: 'get', params: { userId } })
}
