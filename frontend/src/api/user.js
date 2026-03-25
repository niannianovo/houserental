import httpInstance from '@/utils/http'

// 用户登录
export function login(data) {
    return httpInstance({
        url: '/login',
        method: 'post',
        data
    })
}

// 管理员登录
export function adminLogin(data) {
    return httpInstance({
        url: '/admin/login',
        method: 'post',
        data
    })
}

// 发送注册验证码
export function sendRegisterCode(email) {
    return httpInstance({
        url: '/register/send-code',
        method: 'post',
        params: { email }
    })
}

// 用户注册
export function register(data) {
    return httpInstance({
        url: '/register',
        method: 'post',
        data
    })
}

// 切换身份
export function switchRole(userId, role) {
    return httpInstance({
        url: '/login/role',
        method: 'post',
        params: { userId, role }
    })
}

// 获取用户信息
export function getUserProfile(id) {
    return httpInstance({
        url: `/user/profile/${id}`,
        method: 'get'
    })
}
