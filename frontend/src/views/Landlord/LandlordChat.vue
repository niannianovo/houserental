<template>
    <div class="chat-container">
        <el-row :gutter="0" style="height: 100%;">
            <!-- 左侧：会话列表 -->
            <el-col :span="8" class="conv-panel">
                <div class="conv-header">聊天列表</div>
                <div class="conv-list" v-loading="loadingConv">
                    <div v-if="conversations.length === 0" class="conv-empty">暂无会话</div>
                    <div v-for="conv in conversations" :key="conv.id"
                         class="conv-item" :class="{ active: currentConv?.id === conv.id }"
                         @click="selectConversation(conv)">
                        <div style="position:relative; flex-shrink:0; width:40px; height:40px;">
                            <el-avatar :size="40" style="background:#5b9bd5;">
                                {{ getOtherName(conv).charAt(0) }}
                            </el-avatar>
                            <span v-if="conv.unreadCount > 0" class="unread-dot">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
                        </div>
                        <div class="conv-info">
                            <div class="conv-name">{{ getOtherName(conv) }}</div>
                            <div class="conv-last">{{ conv.lastMessage || '暂无消息' }}</div>
                        </div>
                        <div class="conv-time">{{ formatTime(conv.lastMessageTime) }}</div>
                    </div>
                </div>
            </el-col>

            <!-- 右侧：聊天窗口 -->
            <el-col :span="16" class="chat-panel">
                <template v-if="currentConv">
                    <div class="chat-header">
                        <span>{{ getOtherName(currentConv) }}</span>
                        <el-button type="primary" link size="small" @click="$router.push(`/landlord/user/${getOtherId(currentConv)}`)">查看资料</el-button>
                    </div>
                    <div class="chat-messages" ref="messagesRef">
                        <div v-if="messages.length === 0" class="chat-empty">暂无消息，开始聊天吧</div>
                        <div v-for="msg in sortedMessages" :key="msg.id"
                             class="msg-row" :class="Number(msg.senderId) === userId ? 'msg-mine' : 'msg-other'">
                            <!-- 对方消息：头像在左 -->
                            <template v-if="msg.senderId !== userId">
                                <el-avatar :size="36" class="msg-avatar" style="background:#7ab3e0;">
                                    {{ getOtherName(currentConv).charAt(0) }}
                                </el-avatar>
                                <div class="msg-body">
                                    <div class="msg-bubble other">{{ msg.content }}</div>
                                    <div class="msg-time">{{ formatMsgTime(msg.createTime) }}</div>
                                </div>
                            </template>
                            <!-- 我的消息：头像在右 -->
                            <template v-else>
                                <div class="msg-body" style="align-items:flex-end;">
                                    <div class="msg-bubble mine">{{ msg.content }}</div>
                                    <div class="msg-time">{{ formatMsgTime(msg.createTime) }}</div>
                                </div>
                                <el-avatar :size="36" class="msg-avatar" style="background:#5b9bd5;">
                                    {{ userStore.userInfo.nickname?.charAt(0) || '我' }}
                                </el-avatar>
                            </template>
                        </div>
                    </div>
                    <div class="chat-input">
                        <div class="quick-reply-bar">
                            <el-popover placement="top" :width="300" trigger="click">
                                <template #reference>
                                    <el-button size="small" link style="color: #5b9bd5;">快捷回复</el-button>
                                </template>
                                <div class="quick-reply-list">
                                    <div v-if="quickReplies.length === 0" style="color: #999; text-align: center; padding: 8px;">暂无快捷回复</div>
                                    <div v-for="qr in quickReplies" :key="qr.id" class="quick-reply-item" @click="useQuickReply(qr.content)">
                                        <span class="qr-title">{{ qr.title }}</span>
                                        <span class="qr-content">{{ qr.content }}</span>
                                    </div>
                                </div>
                                <div style="border-top: 1px solid #eee; padding-top: 8px; margin-top: 8px;">
                                    <el-button size="small" type="primary" link @click="showQrManage = true">管理快捷回复</el-button>
                                </div>
                            </el-popover>
                        </div>
                        <div style="display: flex; gap: 8px; flex: 1;">
                            <el-input v-model="inputText" placeholder="输入消息..." @keyup.enter="doSend"
                                      :disabled="sending" />
                            <el-button type="primary" @click="doSend" :loading="sending">发送</el-button>
                        </div>
                    </div>

                    <!-- 快捷回复管理弹窗 -->
                    <el-dialog title="管理快捷回复" v-model="showQrManage" width="500px" append-to-body>
                        <div style="margin-bottom: 16px;">
                            <el-form :inline="true" size="small">
                                <el-form-item label="标题">
                                    <el-input v-model="qrForm.title" placeholder="快捷回复标题" style="width: 120px;" />
                                </el-form-item>
                                <el-form-item label="内容">
                                    <el-input v-model="qrForm.content" placeholder="回复内容" style="width: 180px;" />
                                </el-form-item>
                                <el-form-item>
                                    <el-button type="primary" @click="handleAddQr">添加</el-button>
                                </el-form-item>
                            </el-form>
                        </div>
                        <el-table :data="quickReplies" size="small" max-height="300">
                            <el-table-column prop="title" label="标题" width="120" />
                            <el-table-column prop="content" label="内容" />
                            <el-table-column label="操作" width="80">
                                <template #default="{ row }">
                                    <el-button type="danger" link size="small" @click="handleDeleteQr(row)">删除</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-dialog>
                </template>
                <div v-else class="chat-placeholder">选择一个会话开始聊天</div>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getConversations, getMessages, markAsRead, sendMessage } from '@/api/chat'
import { getQuickReplyList, addQuickReply, deleteQuickReply } from '@/api/quickReply'
import { ElMessage, ElNotification } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const userId = computed(() => Number(userStore.userInfo.id))

const loadingConv = ref(false)
const sending = ref(false)
const conversations = ref([])
const currentConv = ref(null)
const messages = ref([])
const inputText = ref('')
const messagesRef = ref(null)
let ws = null

// 快捷回复
const quickReplies = ref([])
const showQrManage = ref(false)
const qrForm = ref({ title: '', content: '' })

const loadQuickReplies = async () => {
    try {
        const res = await getQuickReplyList(userId.value)
        quickReplies.value = res.data || []
    } catch (e) { /* ignore */ }
}

const useQuickReply = (content) => {
    inputText.value = content
}

const handleAddQr = async () => {
    if (!qrForm.value.title || !qrForm.value.content) { ElMessage.warning('请填写标题和内容'); return }
    try {
        await addQuickReply({ userId: userId.value, title: qrForm.value.title, content: qrForm.value.content })
        qrForm.value = { title: '', content: '' }
        loadQuickReplies()
        ElMessage.success({ message: '添加成功', duration: 2000 })
    } catch (e) { console.log('添加失败', e) }
}

const handleDeleteQr = async (row) => {
    try {
        await deleteQuickReply(row.id, userId.value)
        loadQuickReplies()
        ElMessage.success({ message: '已删除', duration: 2000 })
    } catch (e) { console.log('删除失败', e) }
}

const sortedMessages = computed(() => [...messages.value].sort((a, b) => new Date(a.createTime) - new Date(b.createTime)))

const getOtherName = (conv) => {
    return Number(conv.user1Id) === userId.value ? conv.user2Nickname : conv.user1Nickname
}

const getOtherId = (conv) => {
    return Number(conv.user1Id) === userId.value ? conv.user2Id : conv.user1Id
}

const formatTime = (t) => {
    if (!t) return ''
    const d = new Date(t)
    const now = new Date()
    if (d.toDateString() === now.toDateString()) {
        return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0')
    }
    return (d.getMonth() + 1) + '/' + d.getDate()
}

const formatMsgTime = (t) => {
    if (!t) return ''
    const d = new Date(t)
    return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0')
}

const scrollToBottom = () => {
    nextTick(() => {
        if (messagesRef.value) {
            messagesRef.value.scrollTop = messagesRef.value.scrollHeight
        }
    })
}

const loadConversations = async () => {
    loadingConv.value = true
    try {
        const res = await getConversations(userId.value)
        const list = res.data || []
        for (const conv of list) { conv.unreadCount = 0 }
        conversations.value = list
        // 如果路由带了 conversationId，自动选中对应会话
        const targetId = Number(route.query.conversationId)
        if (targetId) {
            const target = list.find(c => c.id === targetId)
            if (target) selectConversation(target)
        }
        loadUnreadCounts()
    } catch (e) { console.log('加载会话失败', e) }
    finally { loadingConv.value = false }
}

const loadUnreadCounts = async () => {
    for (const conv of conversations.value) {
        // 跳过当前已选中的会话（已标记已读）
        if (currentConv.value && currentConv.value.id === conv.id) continue
        try {
            const res = await getMessages(conv.id, 1, 100)
            const msgs = res.data?.records || []
            conv.unreadCount = msgs.filter(m => m.senderId !== userId.value && m.isRead === 0).length
        } catch (e) { /* ignore */ }
    }
}

const selectConversation = async (conv) => {
    currentConv.value = conv
    try {
        const res = await getMessages(conv.id)
        messages.value = res.data?.records || []
        scrollToBottom()
        await markAsRead(conv.id, userId.value)
        conv.unreadCount = 0
    } catch (e) { console.log('加载消息失败', e) }
}

const doSend = async () => {
    if (!inputText.value.trim() || !currentConv.value) return
    const content = inputText.value.trim()
    inputText.value = ''
    sending.value = true

    const msgData = {
        conversationId: currentConv.value.id,
        senderId: userId.value,
        content,
        msgType: 0,
        targetUserId: getOtherId(currentConv.value)
    }

    try {
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ type: 'chat', ...msgData }))
        } else {
            const res = await sendMessage(msgData)
            messages.value.push(res.data)
            scrollToBottom()
        }
    } catch (e) { console.log('发送失败', e) }
    finally { sending.value = false }
}

const connectWs = () => {
    const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
    const host = location.hostname
    ws = new WebSocket(`${protocol}://${host}:8080/ws/chat?userId=${userId.value}`)

    ws.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data)
            if (data.type === 'chat' && data.data) {
                const msg = data.data
                if (currentConv.value && msg.conversationId === currentConv.value.id) {
                    messages.value.push(msg)
                    scrollToBottom()
                    markAsRead(currentConv.value.id, userId.value)
                } else {
                    const conv = conversations.value.find(c => c.id === msg.conversationId)
                    if (conv) {
                        conv.unreadCount = (conv.unreadCount || 0) + 1
                    }
                    ElNotification({
                        title: '新消息',
                        message: msg.content.length > 30 ? msg.content.slice(0, 30) + '...' : msg.content,
                        type: 'info',
                        duration: 4000,
                        position: 'bottom-right'
                    })
                }
                const conv = conversations.value.find(c => c.id === msg.conversationId)
                if (conv) {
                    conv.lastMessage = msg.content
                    conv.lastMessageTime = msg.createTime
                }
            } else if (data.type === 'ack' && data.data) {
                messages.value.push(data.data)
                scrollToBottom()
                if (currentConv.value) {
                    currentConv.value.lastMessage = data.data.content
                    currentConv.value.lastMessageTime = data.data.createTime
                }
            }
        } catch (e) { console.log('解析WS消息失败', e) }
    }

    ws.onclose = () => {
        setTimeout(() => { if (userId.value) connectWs() }, 3000)
    }
}

onMounted(() => {
    loadConversations()
    connectWs()
    loadQuickReplies()
})

onUnmounted(() => {
    if (ws) { ws.onclose = null; ws.close() }
})
</script>

<style scoped>
.chat-container { height: calc(100vh - 140px); background: #fff; border-radius: 16px; overflow: hidden; border: 1px solid #e8f0f8; }
.conv-panel { border-right: 1px solid #e8f0f8; height: 100%; display: flex; flex-direction: column; }
.conv-header { padding: 16px; font-size: 16px; font-weight: 600; color: #1a3a5c; border-bottom: 1px solid #e8f0f8; }
.conv-list { flex: 1; overflow-y: auto; }
.conv-empty { padding: 40px; text-align: center; color: #7a94a8; }
.conv-item { display: flex; align-items: center; gap: 12px; padding: 12px 16px; cursor: pointer; transition: background 0.2s; }
.conv-item:hover { background: #f3f7fb; }
.conv-item.active { background: #e8f0f8; }
.conv-info { flex: 1; min-width: 0; }
.conv-name { font-size: 14px; font-weight: 500; color: #1a3a5c; }
.conv-last { font-size: 12px; color: #7a94a8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 4px; }
.conv-time { font-size: 11px; color: #aaa; flex-shrink: 0; }
.unread-dot { position: absolute; top: -4px; right: -4px; background: #f56c6c; color: #fff; font-size: 11px; min-width: 18px; height: 18px; line-height: 18px; text-align: center; border-radius: 9px; padding: 0 4px; }

.chat-panel { height: 100%; display: flex; flex-direction: column; }
.chat-header { padding: 16px; font-size: 16px; font-weight: 600; color: #1a3a5c; border-bottom: 1px solid #e8f0f8; display: flex; justify-content: space-between; align-items: center; }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 16px; }
.chat-empty { text-align: center; color: #7a94a8; margin-top: 40px; }
.chat-placeholder { display: flex; align-items: center; justify-content: center; height: 100%; color: #7a94a8; font-size: 16px; }

.msg-row { display: flex; align-items: flex-start; gap: 10px; }
.msg-row.msg-mine { flex-direction: row; justify-content: flex-end; }
.msg-row.msg-other { flex-direction: row; justify-content: flex-start; }
.msg-avatar { flex-shrink: 0; }
.msg-body { display: flex; flex-direction: column; max-width: 60%; }
.msg-bubble { padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.5; word-break: break-word; }
.msg-bubble.other { background: #f0f2f5; color: #333; border-top-left-radius: 4px; }
.msg-bubble.mine { background: #5b9bd5; color: #fff; border-top-right-radius: 4px; }
.msg-time { font-size: 11px; color: #aaa; margin-top: 4px; }

.chat-input { display: flex; flex-direction: column; gap: 8px; padding: 12px 16px; border-top: 1px solid #e8f0f8; }
.chat-input .el-input { flex: 1; }
.quick-reply-bar { display: flex; align-items: center; }
.quick-reply-list { max-height: 200px; overflow-y: auto; }
.quick-reply-item { padding: 6px 8px; cursor: pointer; border-radius: 4px; transition: background 0.2s; }
.quick-reply-item:hover { background: #f3f7fb; }
.qr-title { font-size: 13px; font-weight: 500; color: #1a3a5c; margin-right: 8px; }
.qr-content { font-size: 12px; color: #7a94a8; }
</style>
