import httpInstance from '@/utils/http'

// 个性化推荐
export function getRecommendList(userId, limit = 8) {
    return httpInstance({ url: '/recommend/list', method: 'get', params: { userId, limit } })
}

// 推荐搜索（找房页面，推荐排序+筛选+分页）
export function recommendSearch(params) {
    return httpInstance({ url: '/recommend/search', method: 'get', params })
}

// 相似房源
export function getSimilarHouses(houseId, limit = 4) {
    return httpInstance({ url: `/recommend/similar/${houseId}`, method: 'get', params: { limit } })
}
