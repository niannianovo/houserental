import httpInstance from '@/utils/http'

// 提交互评
export function submitReview(data) {
    return httpInstance({ url: '/user-review', method: 'post', data })
}

// 某用户收到的评价
export function getReviewsByTarget(userId, page = 1, size = 10) {
    return httpInstance({ url: `/user-review/${userId}`, method: 'get', params: { page, size } })
}

// 我发出的评价
export function getMySentReviews(reviewerId, page = 1, size = 10) {
    return httpInstance({ url: '/user-review/my-sent', method: 'get', params: { reviewerId, page, size } })
}

// 我收到的评价
export function getMyReceivedReviews(targetId, page = 1, size = 10) {
    return httpInstance({ url: '/user-review/my-received', method: 'get', params: { targetId, page, size } })
}
