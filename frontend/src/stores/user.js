import { defineStore } from "pinia";
import { ref } from "vue";
import { login, adminLogin } from "@/api/user";

// 普通用户 store
export const useUserStore = defineStore('user', () => {
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

    const getUserInfo = async (data) => {
        const res = await login(data)
        userInfo.value = res.data
        localStorage.setItem('userInfo', JSON.stringify(res.data))
    }

    const setUserInfo = (data) => {
        userInfo.value = data
        localStorage.setItem('userInfo', JSON.stringify(data))
    }

    const clearUserInfo = () => {
        userInfo.value = {}
        localStorage.removeItem('userInfo')
    }

    return {
        userInfo,
        getUserInfo,
        setUserInfo,
        clearUserInfo
    }
})

// 管理员 store（独立存储，互不干扰）
export const useAdminStore = defineStore('admin', () => {
    const adminInfo = ref(JSON.parse(localStorage.getItem('adminInfo') || '{}'))

    const getAdminInfo = async (data) => {
        const res = await adminLogin(data)
        adminInfo.value = res.data
        localStorage.setItem('adminInfo', JSON.stringify(res.data))
    }

    const setAdminInfo = (data) => {
        adminInfo.value = data
        localStorage.setItem('adminInfo', JSON.stringify(data))
    }

    const clearAdminInfo = () => {
        adminInfo.value = {}
        localStorage.removeItem('adminInfo')
    }

    return {
        adminInfo,
        getAdminInfo,
        setAdminInfo,
        clearAdminInfo
    }
})
