<template>
    <div>
        <el-card>
            <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>消息通知</span>
                    <el-button type="primary" link @click="handleReadAll">全部已读</el-button>
                </div>
            </template>

            <div v-loading="loading">
                <div v-if="notifications.length === 0" style="text-align: center; padding: 40px; color: #909399;">
                    暂无消息
                </div>
                <div
                    v-for="item in notifications" :key="item.id"
                    class="notify-item"
                    :class="{ unread: !item.isRead }"
                    @click="handleRead(item)"
                >
                    <div class="notify-header">
                        <el-tag :type="notifyType(item.type)" size="small">{{ notifyTypeText(item.type) }}</el-tag>
                        <span class="notify-time">{{ item.createTime }}</span>
                    </div>
                    <div class="notify-title">{{ item.title }}</div>
                    <div class="notify-content">{{ item.content }}</div>
                </div>
            </div>

            <el-pagination
                v-if="total > 0"
                style="margin-top: 16px; justify-content: flex-end;"
                background layout="total, prev, pager, next"
                :total="total" :page-size="pageSize"
                v-model:current-page="currentPage"
                @current-change="loadNotifications"
            />
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getNotifications, markAsRead, markAllAsRead } from '@/api/notification'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const notifications = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const notifyTypeText = (t) => ({ 0: '系统', 1: '订单', 2: '支付', 3: '预约', 4: '审核' }[t] || '通知')
const notifyType = (t) => ({ 0: '', 1: 'success', 2: 'warning', 3: 'primary', 4: 'danger' }[t] || 'info')

const loadNotifications = async () => {
    loading.value = true
    try {
        const res = await getNotifications(userStore.userInfo.id, currentPage.value, pageSize)
        notifications.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载通知失败', e) }
    finally { loading.value = false }
}

const handleRead = async (item) => {
    if (item.isRead) return
    try {
        await markAsRead(item.id, userStore.userInfo.id)
        item.isRead = 1
    } catch (e) { /* ignore */ }
}

const handleReadAll = async () => {
    try {
        await markAllAsRead(userStore.userInfo.id)
        notifications.value.forEach(n => n.isRead = 1)
        ElMessage.success({ message: '已全部标记为已读', duration: 2000 })
    } catch (e) { console.log('操作失败', e) }
}

onMounted(() => loadNotifications())
</script>

<style scoped>
.notify-item {
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background 0.2s;
}
.notify-item:hover {
    background: #f5f7fa;
}
.notify-item.unread {
    background: #ecf5ff;
}
.notify-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}
.notify-time {
    font-size: 12px;
    color: #c0c4cc;
}
.notify-title {
    font-size: 15px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 4px;
}
.notify-content {
    font-size: 13px;
    color: #909399;
    line-height: 1.5;
}
</style>
