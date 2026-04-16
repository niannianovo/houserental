<template>
    <el-container style="height: 100vh;">
        <!-- 侧边栏 -->
        <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
            <div class="logo" :class="{ 'logo-collapse': isCollapse }">
                <span v-if="!isCollapse">租客中心</span>
                <span v-else>租</span>
            </div>
            <el-menu :default-active="activeMenu" :collapse="isCollapse" background-color="#2e5e3a" text-color="#b8d4ba"
                active-text-color="#ffffff" router class="tenant-menu">
                <el-menu-item index="/tenant/home">
                    <el-icon>
                        <HomeFilled />
                    </el-icon>
                    <template #title>首页</template>
                </el-menu-item>
                <el-menu-item index="/tenant/search">
                    <el-icon>
                        <Search />
                    </el-icon>
                    <template #title>找房</template>
                </el-menu-item>
                <el-menu-item index="/tenant/orders">
                    <el-icon>
                        <Document />
                    </el-icon>
                    <template #title>我的订单</template>
                </el-menu-item>
                <el-menu-item index="/tenant/favorites">
                    <el-icon>
                        <Star />
                    </el-icon>
                    <template #title>我的收藏</template>
                </el-menu-item>
                <el-menu-item index="/tenant/appointments">
                    <el-icon>
                        <Calendar />
                    </el-icon>
                    <template #title>预约记录</template>
                </el-menu-item>
                <el-menu-item index="/tenant/reviews">
                    <el-icon>
                        <Comment />
                    </el-icon>
                    <template #title>用户互评</template>
                </el-menu-item>
                <el-menu-item index="/tenant/chat">
                    <el-icon>
                        <ChatDotRound />
                    </el-icon>
                    <template #title>
                        在线聊天
                        <el-badge v-if="chatUnread > 0" :value="chatUnread" :max="99" class="notify-badge" />
                    </template>
                </el-menu-item>
                <el-menu-item index="/tenant/notifications">
                    <el-icon>
                        <Bell />
                    </el-icon>
                    <template #title>
                        消息通知
                        <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" class="notify-badge" />
                    </template>
                </el-menu-item>
                <el-menu-item index="/tenant/profile">
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
                        切换为房东
                    </el-button>
                    <el-dropdown @command="handleCommand">
                        <span class="user-info">
                            <el-avatar :size="32" :src="userStore.userInfo.avatar" style="background: #81c784;">
                                {{ userStore.userInfo.nickname?.charAt(0) }}
                            </el-avatar>
                            <span>{{ userStore.userInfo.nickname || '租客' }}</span>
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
import { HomeFilled, Search, Document, Star, Calendar, Comment, ChatDotRound, Bell, User, Fold, Expand, ArrowDown } from '@element-plus/icons-vue'

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
        const res = await switchRole(userStore.userInfo.id, 1)
        userStore.setUserInfo(res.data)
        ElMessage.success({ message: '已切换为房东', duration: 2000 })
        router.replace('/landlord')
    } catch (e) { console.log('切换失败', e) }
    finally { switching.value = false }
}

const handleCommand = (cmd) => {
    if (cmd === 'logout') {
        userStore.clearUserInfo()
        ElMessage.success({ message: '已退出登录', duration: 2000 })
        router.replace('/login')
    } else if (cmd === 'profile') {
        router.push('/tenant/profile')
    }
}
</script>

<style scoped>
.sidebar {
    background: linear-gradient(180deg, #2e5e3a 0%, #1b4226 100%);
    transition: width 0.3s;
}

.logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #81c784;
    font-size: 18px;
    font-weight: bold;
    white-space: nowrap;
    overflow: hidden;
    letter-spacing: 1px;
}

.logo-collapse {
    font-size: 22px;
}

.tenant-menu {
    border-right: none;
}

:deep(.el-menu-item) {
    border-radius: 8px;
    margin: 4px 8px;
    transition: all 0.2s;
}

:deep(.el-menu-item:hover) {
    background: rgba(129, 199, 132, 0.15) !important;
}

:deep(.el-menu-item.is-active) {
    background: rgba(129, 199, 132, 0.25) !important;
    color: #fff !important;
}

.top-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #fff;
    box-shadow: 0 1px 4px rgba(46, 125, 50, 0.08);
    padding: 0 20px;
    border-bottom: 2px solid #e8f5e9;
}

.collapse-btn {
    cursor: pointer;
    font-size: 20px;
    color: #2e5e3a;
    transition: color 0.2s;
}

.collapse-btn:hover {
    color: #81c784;
}

.switch-btn {
    color: #66bb6a !important;
    font-weight: 500;
}

.switch-btn:hover {
    color: #2e7d32 !important;
}

.user-info {
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
    color: #2e5e3a;
}

.main-content {
    background: linear-gradient(145deg, #f3faf3 0%, #e8f5e9 100%);
    padding: 20px;
}

.notify-badge {
    margin-left: 8px;
}
</style>
