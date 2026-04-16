<template>
    <el-container style="height: 100vh;">
        <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
            <div class="logo" :class="{ 'logo-collapse': isCollapse }">
                <span v-if="!isCollapse">管理后台</span>
                <span v-else>管</span>
            </div>
            <el-menu
                :default-active="activeMenu"
                :collapse="isCollapse"
                background-color="#1d1e1f"
                text-color="#a6a7a9"
                active-text-color="#409EFF"
                router
            >
                <el-menu-item index="/admin/dashboard">
                    <el-icon><Odometer /></el-icon>
                    <template #title>数据概览</template>
                </el-menu-item>
                <el-menu-item index="/admin/users">
                    <el-icon><User /></el-icon>
                    <template #title>用户管理</template>
                </el-menu-item>
                <el-menu-item index="/admin/houses">
                    <el-icon><House /></el-icon>
                    <template #title>房源管理</template>
                </el-menu-item>
                <el-menu-item index="/admin/verify">
                    <el-icon><CircleCheck /></el-icon>
                    <template #title>房源审核</template>
                </el-menu-item>
                <el-menu-item index="/admin/announcements">
                    <el-icon><Notification /></el-icon>
                    <template #title>公告管理</template>
                </el-menu-item>
            </el-menu>
        </el-aside>

        <el-container>
            <el-header class="top-header">
                <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
                    <Fold v-if="!isCollapse" /><Expand v-else />
                </el-icon>
                <div style="display: flex; align-items: center; gap: 12px;">
                    <el-dropdown @command="handleCommand">
                        <span class="user-info">
                            <el-avatar :size="32" style="background: #409EFF;">管</el-avatar>
                            <span>{{ adminStore.adminInfo.nickname || '管理员' }}</span>
                            <el-icon><ArrowDown /></el-icon>
                        </span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </el-header>

            <el-main style="background: #f5f5f5; padding: 20px;">
                <router-view />
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAdminStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Odometer, User, House, CircleCheck, Notification, Fold, Expand, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()
const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

const handleCommand = (cmd) => {
    if (cmd === 'logout') {
        adminStore.clearAdminInfo()
        ElMessage.success({ message: '已退出登录', duration: 2000 })
        router.replace('/admin/login')
    }
}
</script>

<style scoped>
.sidebar { background: #1d1e1f; transition: width 0.3s; }
.logo {
    height: 60px; display: flex; align-items: center; justify-content: center;
    color: #409EFF; font-size: 18px; font-weight: bold; white-space: nowrap; overflow: hidden;
}
.logo-collapse { font-size: 22px; }
.el-menu { border-right: none; }
:deep(.el-menu-item) { margin: 2px 6px; border-radius: 6px; }
:deep(.el-menu-item:hover) { background: rgba(64,158,255,0.1) !important; }
:deep(.el-menu-item.is-active) { background: rgba(64,158,255,0.2) !important; }

.top-header {
    display: flex; align-items: center; justify-content: space-between;
    background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); padding: 0 20px;
}
.collapse-btn { cursor: pointer; font-size: 20px; color: #333; }
.user-info { cursor: pointer; display: flex; align-items: center; gap: 8px; color: #333; }
</style>
