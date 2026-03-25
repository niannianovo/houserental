import httpInstance from '@/utils/http'

// ========== 数据概览 ==========
export function getDashboardOverview() {
    return httpInstance({ url: '/dashboard/overview', method: 'get' })
}
export function getOrderTrend(months = 12) {
    return httpInstance({ url: '/dashboard/order-trend', method: 'get', params: { months } })
}

// ========== 用户管理 ==========
export function getUserList(page = 1, size = 10, keyword = '') {
    return httpInstance({ url: '/admin/user/list', method: 'get', params: { page, size, keyword } })
}
export function disableUser(id) {
    return httpInstance({ url: `/admin/user/disable/${id}`, method: 'put' })
}
export function enableUser(id) {
    return httpInstance({ url: `/admin/user/enable/${id}`, method: 'put' })
}
export function deleteUser(id) {
    return httpInstance({ url: `/admin/user/${id}`, method: 'delete' })
}

// ========== 房源审核 ==========
export function getPendingHouses(page = 1, size = 10) {
    return httpInstance({ url: '/verify/pending', method: 'get', params: { page, size } })
}
export function auditHouse(houseId, adminId, action, reason = '') {
    return httpInstance({ url: `/verify/${houseId}`, method: 'post', params: { adminId, action, reason } })
}

// ========== 举报管理 ==========
export function getPendingReports(page = 1, size = 10) {
    return httpInstance({ url: '/report/pending', method: 'get', params: { page, size } })
}
export function handleReport(id, adminId, status, result = '') {
    return httpInstance({ url: `/report/handle/${id}`, method: 'put', params: { adminId, status, result } })
}

// ========== 公告管理 ==========
export function getAnnouncementList(page = 1, size = 10) {
    return httpInstance({ url: '/announcement/list', method: 'get', params: { page, size } })
}
export function publishAnnouncement(data) {
    return httpInstance({ url: '/announcement', method: 'post', data })
}
export function updateAnnouncement(id, data) {
    return httpInstance({ url: `/announcement/${id}`, method: 'put', data })
}
export function deleteAnnouncement(id) {
    return httpInstance({ url: `/announcement/${id}`, method: 'delete' })
}
