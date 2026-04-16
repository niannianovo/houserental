import httpInstance from '@/utils/http'

// 添加评论
export function addComment(data) {
    return httpInstance({ url: '/house-comment', method: 'post', data })
}

// 房源评论列表
export function getCommentsByHouse(houseId, page = 1, size = 10) {
    return httpInstance({ url: `/house-comment/list/${houseId}`, method: 'get', params: { page, size } })
}

// 我的评论
export function getMyComments(userId, page = 1, size = 10) {
    return httpInstance({ url: '/house-comment/my', method: 'get', params: { userId, page, size } })
}
