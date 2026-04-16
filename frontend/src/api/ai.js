import httpInstance from '@/utils/http'

export function aiSmartSearch(history, userId) {
    return httpInstance({
        url: '/ai/search',
        method: 'post',
        data: { history, userId: String(userId) },
        timeout: 30000
    })
}
