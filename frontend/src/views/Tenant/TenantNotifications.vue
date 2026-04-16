<template>
    <div>
        <el-card class="green-card">
            <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span class="section-title">消息中心</span>
                    <el-button v-if="activeTab === 'notify'" type="primary" link class="more-btn" @click="handleReadAll">全部已读</el-button>
                </div>
            </template>

            <el-tabs v-model="activeTab" class="green-tabs">
                <el-tab-pane label="我的通知" name="notify" />
                <el-tab-pane label="系统公告" name="announcement" />
            </el-tabs>

            <!-- 通知列表 -->
            <div v-if="activeTab === 'notify'" v-loading="loading">
                <div v-if="notifications.length === 0" style="text-align: center; padding: 40px; color: #88a58b;">暂无消息</div>
                <div
                    v-for="item in notifications" :key="item.id"
                    class="notify-item" :class="{ unread: !item.isRead }"
                    @click="handleRead(item)"
                >
                    <div class="notify-header">
                        <div style="display:flex; align-items:center; gap:8px;">
                            <span v-if="!item.isRead" class="unread-dot"></span>
                            <el-tag :type="notifyType(item.type)" size="small">{{ notifyTypeText(item.type) }}</el-tag>
                        </div>
                        <span class="notify-time">{{ item.createTime }}</span>
                    </div>
                    <div class="notify-title">{{ item.title }}</div>
                    <div class="notify-content">{{ item.content }}</div>
                </div>

                <el-pagination
                    v-if="total > 0"
                    style="margin-top: 16px; justify-content: flex-end;"
                    background layout="total, prev, pager, next"
                    :total="total" :page-size="pageSize"
                    v-model:current-page="currentPage"
                    @current-change="loadNotifications"
                />
            </div>

            <!-- 公告列表 -->
            <div v-if="activeTab === 'announcement'" v-loading="loadingAnn">
                <div v-if="announcements.length === 0" style="text-align: center; padding: 40px; color: #88a58b;">暂无公告</div>
                <div
                    v-for="item in announcements" :key="item.id"
                    class="announce-item"
                    @click="openAnnouncement(item)"
                >
                    <div class="notify-header">
                        <el-tag type="danger" size="small">公告</el-tag>
                        <span class="notify-time">{{ item.createTime }}</span>
                    </div>
                    <div class="notify-title">{{ item.title }}</div>
                    <div class="notify-content">{{ item.content?.length > 80 ? item.content.substring(0, 80) + '...' : item.content }}</div>
                </div>

                <el-pagination
                    v-if="annTotal > 0"
                    style="margin-top: 16px; justify-content: flex-end;"
                    background layout="total, prev, pager, next"
                    :total="annTotal" :page-size="pageSize"
                    v-model:current-page="annPage"
                    @current-change="loadAnnouncements"
                />
            </div>
        </el-card>

        <!-- 公告详情弹窗 -->
        <el-dialog v-model="annDialogVisible" :title="currentAnn.title" width="600px">
            <div style="line-height: 1.8; color: #555; white-space: pre-wrap;">{{ currentAnn.content }}</div>
            <div style="margin-top: 16px; text-align: right; color: #999; font-size: 12px;">发布时间：{{ currentAnn.createTime }}</div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { getNotifications, markAsRead, markAllAsRead, getAnnouncements } from '@/api/notification'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const loadingAnn = ref(false)
const activeTab = ref('notify')
const notifications = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const announcements = ref([])
const annTotal = ref(0)
const annPage = ref(1)
const annDialogVisible = ref(false)
const currentAnn = ref({})

const notifyTypeText = (t) => ({ 0: '降价', 1: '公告', 2: '审核', 4: '支付', 5: '合同' }[t] || '通知')
const notifyType = (t) => ({ 0: 'warning', 1: '', 2: 'primary', 4: 'warning', 5: 'success' }[t] || 'info')

const loadNotifications = async () => {
    loading.value = true
    try {
        const res = await getNotifications(userStore.userInfo.id, currentPage.value, pageSize)
        notifications.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载通知失败', e) }
    finally { loading.value = false }
}

const loadAnnouncements = async () => {
    loadingAnn.value = true
    try {
        const res = await getAnnouncements(annPage.value, pageSize)
        announcements.value = res.data?.records || []
        annTotal.value = res.data?.total || 0
    } catch (e) { console.log('加载公告失败', e) }
    finally { loadingAnn.value = false }
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

const openAnnouncement = (item) => {
    currentAnn.value = item
    annDialogVisible.value = true
}

watch(activeTab, (val) => {
    if (val === 'announcement' && announcements.value.length === 0) loadAnnouncements()
})

onMounted(() => loadNotifications())
</script>

<style scoped>
.green-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.more-btn { color: #66bb6a !important; }
.more-btn:hover { color: #2e7d32 !important; }
.green-tabs :deep(.el-tabs__active-bar) { background: #81c784; }
.green-tabs :deep(.el-tabs__item.is-active) { color: #2e7d32; }

.notify-item, .announce-item {
    padding: 16px; border-bottom: 1px solid #e8f5e9;
    cursor: pointer; transition: background 0.2s; border-radius: 8px; margin-bottom: 4px;
}
.notify-item:hover, .announce-item:hover { background: #f3faf3; }
.notify-item.unread { background: #e8f5e9; }
.unread-dot { width: 8px; height: 8px; border-radius: 50%; background: #f56c6c; flex-shrink: 0; }
.notify-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.notify-time { font-size: 12px; color: #b8d4ba; }
.notify-title { font-size: 15px; font-weight: 500; color: #2e5e3a; margin-bottom: 4px; }
.notify-content { font-size: 13px; color: #88a58b; line-height: 1.5; }
:deep(.el-pagination.is-background .el-pager li.is-active) { background: #66bb6a !important; }
</style>
