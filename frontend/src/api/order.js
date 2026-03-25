import httpInstance from '@/utils/http'

// 获取我的订单列表
export function getMyOrders(userId, page = 1, size = 10) {
    return httpInstance({ url: '/rental-order/list', method: 'get', params: { userId, page, size } })
}

// 获取历史订单
export function getOrderHistory(userId, page = 1, size = 10) {
    return httpInstance({ url: '/rental-order/history', method: 'get', params: { userId, page, size } })
}

// 签约
export function signOrder(id, tenantId) {
    return httpInstance({ url: `/rental-order/sign/${id}`, method: 'put', params: { tenantId } })
}

// 续租
export function renewOrder(id, newEndDate) {
    return httpInstance({ url: `/rental-order/renew/${id}`, method: 'put', params: { newEndDate } })
}

// 申请退租
export function quitApply(id, operatorId) {
    return httpInstance({ url: `/rental-order/quit/${id}`, method: 'put', params: { operatorId } })
}

// 确认退租
export function quitConfirm(id, operatorId) {
    return httpInstance({ url: `/rental-order/quit-confirm/${id}`, method: 'put', params: { operatorId } })
}

// 获取订单的租金记录
export function getPaymentList(orderId) {
    return httpInstance({ url: `/rent-payment/list/${orderId}`, method: 'get' })
}

// 确认收款
export function confirmPayment(id, ownerId) {
    return httpInstance({ url: `/rent-payment/confirm/${id}`, method: 'put', params: { ownerId } })
}
