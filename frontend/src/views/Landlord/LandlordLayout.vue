<template>
    <el-container style="height: 100vh;">
        <!-- 侧边栏 -->
        <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
            <div class="logo" :class="{ 'logo-collapse': isCollapse }">
                <span v-if="!isCollapse">房东管理中心</span>
                <span v-else>房</span>
            </div>
            <el-menu :default-active="activeMenu" :collapse="isCollapse" background-color="#1a3a5c" text-color="#9db0c8"
                active-text-color="#ffffff" router class="landlord-menu">
                <el-menu-item index="/landlord/dashboard">
                    <el-icon>
                        <Odometer />
                    </el-icon>
                    <template #title>首页概览</template>
                </el-menu-item>
                <el-menu-item index="/landlord/houses">
                    <el-icon>
                        <House />
                    </el-icon>
                    <template #title>房源管理</template>
                </el-menu-item>
                <el-menu-item index="/landlord/orders">
                    <el-icon>
                        <Document />
                    </el-icon>
                    <template #title>订单管理</template>
                </el-menu-item>
                <el-menu-item index="/landlord/payments">
                    <el-icon>
                        <Money />
                    </el-icon>
                    <template #title>收租管理</template>
                </el-menu-item>
                <el-menu-item index="/landlord/appointments">
                    <el-icon>
                        <Calendar />
                    </el-icon>
                    <template #title>预约看房</template>
                </el-menu-item>
                <el-menu-item index="/landlord/reviews">
                    <el-icon>
                        <Comment />
                    </el-icon>
                    <template #title>用户互评</template>
                </el-menu-item>
                <el-menu-item index="/landlord/chat">
                    <el-icon>
                        <ChatDotRound />
                    </el-icon>
                    <template #title>
                        在线聊天
                        <el-badge v-if="chatUnread > 0" :value="chatUnread" :max="99" class="notify-badge" />
                    </template>
                </el-menu-item>
                <el-menu-item index="/landlord/notifications">
                    <el-icon>
                        <Bell />
                    </el-icon>
                    <template #title>
                        消息通知
                        <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" class="notify-badge" />
                    </template>
                </el-menu-item>
                <el-menu-item index="/landlord/profile">
                    <el-icon>
                        <User />
                    </el-icon>
                    <template #title>个人中心</template>
                </el-menu-item>
            </el-menu>
        </el-aside>

        <el-container>
            <!-- 顶栏 -->
            <el-header class="top-header">
                <div style="display: flex; align-items: center;">
                    <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
                        <Fold v-if="!isCollapse" />
                        <Expand v-else />
                    </el-icon>
                </div>
                <div style="display: flex; align-items: center; gap: 16px;">
                    <el-button class="switch-btn" link @click="handleSwitchRole" :loading="switching">
                        切换为租客
                    </el-button>
                    <el-dropdown @command="handleCommand">
                        <span class="user-info">
                            <el-avatar :size="32" :src="userStore.userInfo.avatar" style="background: #5b9bd5;">
                                {{ userStore.userInfo.nickname?.charAt(0) }}
                            </el-avatar>
                            <span>{{ userStore.userInfo.nickname || '房东' }}</span>
                            <el-icon>
                                <ArrowDown />
                            </el-icon>
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
            <el-main class="main-content">
                <router-view />
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { switchRole } from '@/api/user'
import { getUnreadCount } from '@/api/notification'
import { getChatUnreadCount } from '@/api/chat'
import { ElMessage } from 'element-plus'
import { Odometer, House, Document, Money, Calendar, Comment, ChatDotRound, Bell, User, Fold, Expand, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isCollapse = ref(false)
const switching = ref(false)
const unreadCount = ref(0)
const chatUnread = ref(0)

const activeMenu = computed(() => route.path)

const refreshUnread = async () => {
    try {
        const res = await getUnreadCount(userStore.userInfo.id)
        unreadCount.value = res.data || 0
    } catch (e) { /* ignore */ }
    try {
        const res = await getChatUnreadCount(userStore.userInfo.id)
        chatUnread.value = res.data || 0
    } catch (e) { /* ignore */ }
}

onMounted(() => refreshUnread())

// 路由切换时刷新未读数
watch(() => route.path, () => refreshUnread())

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
.sidebar {
    background: linear-gradient(180deg, #1a3a5c 0%, #122a42 100%);
    transition: width 0.3s;
}

.logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #5b9bd5;
    font-size: 18px;
    font-weight: bold;
    white-space: nowrap;
    overflow: hidden;
    letter-spacing: 1px;
}

.logo-collapse {
    font-size: 22px;
}

.landlord-menu {
    border-right: none;
}

:deep(.el-menu-item) {
    border-radius: 8px;
    margin: 4px 8px;
    transition: all 0.2s;
}

:deep(.el-menu-item:hover) {
    background: rgba(91, 155, 213, 0.15) !important;
}

:deep(.el-menu-item.is-active) {
    background: rgba(91, 155, 213, 0.25) !important;
    color: #fff !important;
}

.top-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #fff;
    box-shadow: 0 1px 4px rgba(26, 58, 92, 0.08);
    padding: 0 20px;
    border-bottom: 2px solid #e8f0f8;
}

.collapse-btn {
    cursor: pointer;
    font-size: 20px;
    color: #1a3a5c;
    transition: color 0.2s;
}

.collapse-btn:hover {
    color: #5b9bd5;
}

.switch-btn {
    color: #5b9bd5 !important;
    font-weight: 500;
}

.switch-btn:hover {
    color: #1a3a5c !important;
}

.user-info {
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
    color: #1a3a5c;
}

.main-content {
    background: linear-gradient(145deg, #f3f7fb 0%, #e8f0f8 100%);
    padding: 20px;
}

.notify-badge {
    margin-left: 8px;
}</style>
