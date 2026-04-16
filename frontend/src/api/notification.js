import httpInstance from '@/utils/http'

// 获取通知列表
export function getNotifications(userId, page = 1, size = 10) {
    return httpInstance({ url: '/notification/list', method: 'get', params: { userId, page, size } })
}

// 未读数量
export function getUnreadCount(userId) {
    return httpInstance({ url: '/notification/unread-count', method: 'get', params: { userId } })
}

// 标记已读
export function markAsRead(id, userId) {
    return httpInstance({ url: `/notification/read/${id}`, method: 'put', params: { userId } })
}

// 全部已读
export function markAllAsRead(userId) {
    return httpInstance({ url: '/notification/read-all', method: 'put', params: { userId } })
}

// 获取已发布的系统公告
export function getAnnouncements(page = 1, size = 10) {
    return httpInstance({ url: '/announcement/list', method: 'get', params: { status: 1, page, size } })
}
