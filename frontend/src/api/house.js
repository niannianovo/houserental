import httpInstance from '@/utils/http'

// 发布房源
export function publishHouse(data) {
    return httpInstance({ url: '/house', method: 'post', data })
}

// 获取房源详情
export function getHouseDetail(id) {
    return httpInstance({ url: `/house/${id}`, method: 'get' })
}

// 更新房源
export function updateHouse(id, data) {
    return httpInstance({ url: `/house/${id}`, method: 'put', data })
}

// 删除房源
export function deleteHouse(id, ownerId) {
    return httpInstance({ url: `/house/${id}`, method: 'delete', params: { ownerId } })
}

// 我的房源列表
export function getMyHouses(ownerId, page = 1, size = 10) {
    return httpInstance({ url: '/house/my', method: 'get', params: { ownerId, page, size } })
}

// 搜索房源
export function searchHouses(params) {
    return httpInstance({ url: '/house/list', method: 'get', params })
}

// 管理员强制下架房源
export function adminTakedownHouse(id, reason) {
    return httpInstance({ url: `/admin/house/takedown/${id}`, method: 'put', params: { reason } })
}

// 管理员删除房源
export function adminDeleteHouse(id, reason) {
    return httpInstance({ url: `/admin/house/${id}`, method: 'delete', params: { reason } })
}

// 上传图片
export function uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return httpInstance({
        url: '/upload',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}
