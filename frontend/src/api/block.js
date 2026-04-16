import httpInstance from '@/utils/http'

// 拉黑用户
export function blockUser(userId, blockedUserId) {
    return httpInstance({ url: `/block/${blockedUserId}`, method: 'post', params: { userId } })
}

// 取消拉黑
export function unblockUser(userId, blockedUserId) {
    return httpInstance({ url: `/block/${blockedUserId}`, method: 'delete', params: { userId } })
}

// 黑名单列表
export function getBlockList(userId) {
    return httpInstance({ url: '/block/list', method: 'get', params: { userId } })
}
