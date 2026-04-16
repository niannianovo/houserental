import httpInstance from '@/utils/http'

// 获取预约列表
export function getMyAppointments(userId, page = 1, size = 10) {
    return httpInstance({ url: '/appointment/list', method: 'get', params: { userId, page, size } })
}

// 获取房东的预约列表
export function getLandlordAppointments(ownerId, page = 1, size = 10) {
    return httpInstance({ url: '/appointment/landlord/list', method: 'get', params: { ownerId, page, size } })
}

// 更新预约状态
export function updateAppointmentStatus(id, status) {
    return httpInstance({ url: `/appointment/${id}`, method: 'put', params: { status } })
}
