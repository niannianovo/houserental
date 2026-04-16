import httpInstance from '@/utils/http'

// 创建租约（直接租房）
export function createOrder(data) {
    return httpInstance({ url: '/rental-order', method: 'post', data })
}

// 获取我的订单列表（role: 0租客 1房东）
export function getMyOrders(userId, page = 1, size = 10, role) {
    return httpInstance({ url: '/rental-order/list', method: 'get', params: { userId, page, size, role } })
}

// 获取历史订单
export function getOrderHistory(userId, page = 1, size = 10, role) {
    return httpInstance({ url: '/rental-order/history', method: 'get', params: { userId, page, size, role } })
}

// 房东签约
export function signOrder(id, ownerId) {
    return httpInstance({ url: `/rental-order/sign/${id}`, method: 'put', params: { ownerId } })
}

// 房东拒绝签约
export function rejectOrder(id, ownerId) {
    return httpInstance({ url: `/rental-order/reject/${id}`, method: 'put', params: { ownerId } })
}

// 租客缴纳押金
export function payDeposit(id, tenantId) {
    return httpInstance({ url: `/rental-order/pay-deposit/${id}`, method: 'put', params: { tenantId } })
}

// 房东退还押金
export function refundDeposit(id, ownerId) {
    return httpInstance({ url: `/rental-order/refund-deposit/${id}`, method: 'put', params: { ownerId } })
}

// 租客申请续租
export function renewApply(id, tenantId, newEndDate) {
    return httpInstance({ url: `/rental-order/renew-apply/${id}`, method: 'put', params: { tenantId, newEndDate } })
}

// 房东确认续租
export function renewConfirm(id, ownerId) {
    return httpInstance({ url: `/rental-order/renew-confirm/${id}`, method: 'put', params: { ownerId } })
}

// 申请退租
export function quitApply(id, operatorId) {
    return httpInstance({ url: `/rental-order/quit/${id}`, method: 'put', params: { operatorId } })
}

// 确认退租
export function quitConfirm(id, operatorId) {
    return httpInstance({ url: `/rental-order/quit-confirm/${id}`, method: 'put', params: { operatorId } })
}

// 撤回退租
export function quitCancel(id, operatorId) {
    return httpInstance({ url: `/rental-order/quit-cancel/${id}`, method: 'put', params: { operatorId } })
}

// 取消待签约订单
export function cancelOrder(id, operatorId) {
    return httpInstance({ url: `/rental-order/cancel/${id}`, method: 'put', params: { operatorId } })
}

// 获取订单的租金记录
export function getPaymentList(orderId) {
    return httpInstance({ url: `/rent-payment/list/${orderId}`, method: 'get' })
}

// 确认收款
export function confirmPayment(id, ownerId) {
    return httpInstance({ url: `/rent-payment/confirm/${id}`, method: 'put', params: { ownerId } })
}
