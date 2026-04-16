import httpInstance from '@/utils/http'

// 保存/更新笔记
export function saveNote(houseId, userId, content) {
    return httpInstance({ url: `/house-note/${houseId}`, method: 'post', params: { userId, content } })
}

// 获取笔记
export function getNote(houseId, userId) {
    return httpInstance({ url: `/house-note/${houseId}`, method: 'get', params: { userId } })
}

// 删除笔记
export function deleteNote(houseId, userId) {
    return httpInstance({ url: `/house-note/${houseId}`, method: 'delete', params: { userId } })
}
