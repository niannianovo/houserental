<template>
    <el-container style="height: 100vh;">
        <!-- 侧边栏 -->
        <el-aside :width="isCollapse ? '64px' : '200px'" style="background-color: #304156; transition: width 0.3s;">
            <div class="logo" :class="{ 'logo-collapse': isCollapse }">
                <span v-if="!isCollapse">房东管理中心</span>
                <span v-else>房</span>
            </div>
            <el-menu
                :default-active="activeMenu"
                :collapse="isCollapse"
                background-color="#304156"
                text-color="#bfcbd9"
                active-text-color="#409EFF"
                router
            >
                <el-menu-item index="/landlord/dashboard">
                    <el-icon><Odometer /></el-icon>
                    <template #title>首页概览</template>
                </el-menu-item>
                <el-menu-item index="/landlord/houses">
                    <el-icon><House /></el-icon>
                    <template #title>房源管理</template>
                </el-menu-item>
                <el-menu-item index="/landlord/orders">
                    <el-icon><Document /></el-icon>
                    <template #title>订单管理</template>
                </el-menu-item>
                <el-menu-item index="/landlord/payments">
                    <el-icon><Money /></el-icon>
                    <template #title>收租管理</template>
                </el-menu-item>
                <el-menu-item index="/landlord/appointments">
                    <el-icon><Calendar /></el-icon>
                    <template #title>预约看房</template>
                </el-menu-item>
                <el-menu-item index="/landlord/notifications">
                    <el-icon><Bell /></el-icon>
                    <template #title>
                        消息通知
                        <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" class="notify-badge" />
                    </template>
                </el-menu-item>
                <el-menu-item index="/landlord/profile">
                    <el-icon><User /></el-icon>
                    <template #title>个人中心</template>
                </el-menu-item>
            </el-menu>
        </el-aside>

        <el-container>
            <!-- 顶栏 -->
            <el-header style="display: flex; align-items: center; justify-content: space-between; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.08); padding: 0 20px;">
                <div style="display: flex; align-items: center;">
                    <el-icon style="cursor: pointer; font-size: 20px;" @click="isCollapse = !isCollapse">
                        <Fold v-if="!isCollapse" />
                        <Expand v-else />
                    </el-icon>
                </div>
                <div style="display: flex; align-items: center; gap: 16px;">
                    <el-button type="primary" link @click="handleSwitchRole" :loading="switching">
                        切换为租客
                    </el-button>
                    <el-dropdown @command="handleCommand">
                        <span style="cursor: pointer; display: flex; align-items: center; gap: 8px;">
                            <el-avatar :size="32" :src="userStore.userInfo.avatar">
                                {{ userStore.userInfo.nickname?.charAt(0) }}
                            </el-avatar>
                            <span>{{ userStore.userInfo.nickname || '房东' }}</span>
                            <el-icon><ArrowDown /></el-icon>
                        </span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </el-header>

            <!-- 内容区 -->
            <el-main style="background-color: #f0f2f5; padding: 20px;">
                <router-view />
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { switchRole } from '@/api/user'
import { getUnreadCount } from '@/api/notification'
import { ElMessage } from 'element-plus'
import { Odometer, House, Document, Money, Calendar, Bell, User, Fold, Expand, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isCollapse = ref(false)
const switching = ref(false)
const unreadCount = ref(0)

const activeMenu = computed(() => route.path)

onMounted(async () => {
    try {
        const res = await getUnreadCount(userStore.userInfo.id)
        unreadCount.value = res.data || 0
    } catch (e) { /* ignore */ }
})

const handleSwitchRole = async () => {
    switching.value = true
    try {
        const res = await switchRole(userStore.userInfo.id, 0)
        userStore.setUserInfo(res.data)
        ElMessage.success({ message: '已切换为租客', duration: 2000 })
        router.replace('/tenant')
    } catch (e) {
        console.log('切换失败', e)
    } finally {
        switching.value = false
    }
}

const handleCommand = (cmd) => {
    if (cmd === 'logout') {
        userStore.clearUserInfo()
        ElMessage.success({ message: '已退出登录', duration: 2000 })
        router.replace('/login')
    } else if (cmd === 'profile') {
        router.push('/landlord/profile')
    }
}
</script>

<style scoped>
.logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    white-space: nowrap;
    overflow: hidden;
}
.logo-collapse {
    font-size: 20px;
}
.el-menu {
    border-right: none;
}
.notify-badge {
    margin-left: 8px;
}
</style>
