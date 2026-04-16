import httpInstance from '@/utils/http'

// 快捷回复列表
export function getQuickReplyList(userId) {
    return httpInstance({ url: '/quick-reply/list', method: 'get', params: { userId } })
}

// 新增快捷回复
export function addQuickReply(data) {
    return httpInstance({ url: '/quick-reply', method: 'post', data })
}

// 修改快捷回复
export function updateQuickReply(id, data) {
    return httpInstance({ url: `/quick-reply/${id}`, method: 'put', data })
}

// 删除快捷回复
export function deleteQuickReply(id, userId) {
    return httpInstance({ url: `/quick-reply/${id}`, method: 'delete', params: { userId } })
}
