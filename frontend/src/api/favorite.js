import httpInstance from '@/utils/http'

// 切换收藏
export function toggleFavorite(houseId, userId) {
    return httpInstance({ url: `/favorite/${houseId}`, method: 'post', params: { userId } })
}

// 我的收藏列表
export function getMyFavorites(userId, page = 1, size = 10) {
    return httpInstance({ url: '/favorite/list', method: 'get', params: { userId, page, size } })
}
