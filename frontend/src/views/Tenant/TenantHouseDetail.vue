<template>
    <div v-loading="loading">
        <el-page-header @back="$router.back()" style="margin-bottom: 20px;">
            <template #content><span class="section-title">房源详情</span></template>
        </el-page-header>

        <el-row :gutter="20" v-if="house">
            <!-- 左侧：房源信息 -->
            <el-col :span="16">
                <el-card class="detail-card" style="margin-bottom: 20px;">
                    <div v-if="imageList.length > 0">
                        <el-carousel height="360px" indicator-position="outside">
                            <el-carousel-item v-for="(img, idx) in imageList" :key="idx">
                                <img :src="img" alt="" style="width: 100%; height: 100%; object-fit: cover; border-radius: 8px;" />
                            </el-carousel-item>
                        </el-carousel>
                    </div>
                    <div v-else class="no-image">暂无房源图片</div>

                    <h2 class="house-name">{{ house.title }}</h2>
                    <div class="house-address">{{ [house.province, house.city, house.district, house.address].filter(Boolean).join(' ') }}</div>

                    <el-row :gutter="16" style="margin: 20px 0;">
                        <el-col :span="6" class="info-item">
                            <div class="info-label">月租</div>
                            <div class="info-value price-value">{{ house.price }} <span class="unit">元/月</span></div>
                        </el-col>
                        <el-col :span="6" class="info-item">
                            <div class="info-label">户型</div>
                            <div class="info-value">{{ house.roomCount }}室{{ house.hallCount }}厅</div>
                        </el-col>
                        <el-col :span="6" class="info-item">
                            <div class="info-label">面积</div>
                            <div class="info-value">{{ house.area }} m²</div>
                        </el-col>
                        <el-col :span="6" class="info-item">
                            <div class="info-label">楼层</div>
                            <div class="info-value">{{ house.floor }} 楼</div>
                        </el-col>
                    </el-row>

                    <el-divider />
                    <h3 class="sub-title">房源描述</h3>
                    <p class="desc-text">{{ house.description || '暂无描述' }}</p>
                </el-card>

                <!-- 个人笔记 -->
                <el-card class="detail-card" style="margin-bottom: 20px;">
                    <template #header>
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <span class="card-label">我的笔记</span>
                            <div>
                                <el-button v-if="!noteEditing && noteContent" type="danger" link size="small" @click="handleDeleteNote">删除</el-button>
                                <el-button type="primary" link size="small" @click="noteEditing = !noteEditing">
                                    {{ noteEditing ? '取消' : (noteContent ? '编辑' : '添加笔记') }}
                                </el-button>
                            </div>
                        </div>
                    </template>
                    <div v-if="noteEditing">
                        <el-input v-model="noteInput" type="textarea" :rows="3" placeholder="写下你对这个房源的备注..." />
                        <el-button type="primary" size="small" class="green-btn" style="margin-top: 8px;" @click="handleSaveNote" :loading="noteSaving">保存</el-button>
                    </div>
                    <div v-else-if="noteContent" class="note-text">{{ noteContent }}</div>
                    <div v-else style="color: #88a58b; font-size: 13px;">暂无笔记，点击右上角添加</div>
                </el-card>

                <!-- 评论区 -->
                <el-card class="detail-card" style="margin-bottom: 20px;">
                    <template #header><span class="card-label">房源评论 ({{ commentTotal }})</span></template>

                    <!-- 发表评论 -->
                    <div style="margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #e8f5e9;">
                        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                            <span style="color: #2e5e3a; font-size: 14px;">评分：</span>
                            <el-rate v-model="commentRating" :colors="['#81c784', '#66bb6a', '#4caf50']" />
                        </div>
                        <el-input v-model="commentContent" type="textarea" :rows="2" placeholder="分享你对这个房源的看法..." />
                        <el-button type="primary" size="small" class="green-btn" style="margin-top: 8px;" @click="handleAddComment" :loading="commentSubmitting">发表评论</el-button>
                    </div>

                    <!-- 评论列表 -->
                    <div v-loading="commentLoading">
                        <div v-for="c in comments" :key="c.id" class="comment-item">
                            <div class="comment-header">
                                <el-avatar :size="32" :src="c.avatar" style="background: #81c784;">{{ (c.nickname || '?').charAt(0) }}</el-avatar>
                                <div>
                                    <span class="comment-user">{{ c.nickname || '匿名用户' }}</span>
                                    <el-rate v-model="c.rating" disabled :size="'small'" :colors="['#81c784', '#66bb6a', '#4caf50']" style="margin-left: 8px;" />
                                </div>
                                <span class="comment-time">{{ c.createTime }}</span>
                            </div>
                            <div class="comment-content">{{ c.content }}</div>
                        </div>
                        <el-empty v-if="!commentLoading && comments.length === 0" description="暂无评论" :image-size="60" />
                    </div>

                    <el-pagination
                        v-if="commentTotal > 5"
                        style="margin-top: 12px; justify-content: flex-end;"
                        background layout="prev, pager, next"
                        :total="commentTotal" :page-size="5"
                        v-model:current-page="commentPage"
                        @current-change="loadComments"
                    />
                </el-card>

                <!-- 相似房源 -->
                <el-card class="detail-card" v-if="similarList.length > 0">
                    <template #header><span class="card-label">相似房源</span></template>
                    <el-row :gutter="16">
                        <el-col :span="6" v-for="h in similarList" :key="h.id">
                            <div class="house-card" @click="$router.push(`/tenant/house/${h.id}`)">
                                <div class="similar-img">
                                    <img v-if="h.images" :src="h.images.split(',')[0]" alt="" />
                                    <div v-else class="similar-img-placeholder">暂无图片</div>
                                </div>
                                <div class="similar-info">
                                    <div class="similar-title">{{ h.title }}</div>
                                    <div class="similar-price"><span class="price">{{ h.price }}</span> 元/月</div>
                                </div>
                            </div>
                        </el-col>
                    </el-row>
                </el-card>
            </el-col>

            <!-- 右侧：操作 -->
            <el-col :span="8">
                <el-card class="detail-card" style="margin-bottom: 20px;">
                    <div style="display: flex; flex-direction: column; gap: 12px;">
                        <el-button type="danger" size="large" class="rent-btn-lg" @click="openRentDialog">直接租房</el-button>
                        <el-button type="primary" size="large" class="green-btn-lg" @click="openAppointment">预约看房</el-button>
                        <el-button type="info" size="large" class="chat-btn-lg" @click="handleChat">联系房东</el-button>
                        <el-button :class="isFav ? 'fav-active' : 'fav-btn'" size="large" @click="handleFavorite">
                            {{ isFav ? '已收藏' : '收藏房源' }}
                        </el-button>
                    </div>
                </el-card>

                <el-card class="detail-card" style="margin-bottom: 20px;">
                    <template #header><span class="card-label">房东信息</span></template>
                    <div v-if="ownerInfo" class="owner-info">
                        <div class="owner-header" @click="router.push(`/tenant/user/${ownerInfo.id}`)" style="cursor: pointer;">
                            <el-avatar :size="48" :src="ownerInfo.avatar" style="background: #81c784;">
                                {{ (ownerInfo.nickname || '?').charAt(0) }}
                            </el-avatar>
                            <div>
                                <div class="owner-name">{{ ownerInfo.nickname || '未知用户' }}</div>
                                <div class="owner-tags">
                                    <el-tag v-if="ownerInfo.isEmailVerified === 1" type="warning" size="small">已验证邮箱</el-tag>
                                </div>
                            </div>
                        </div>
                        <el-button type="primary" link size="small" @click="router.push(`/tenant/user/${ownerInfo.id}`)" style="margin-top: 8px;">
                            查看主页与评价 →
                        </el-button>
                    </div>
                    <el-divider v-if="ownerInfo" style="margin: 12px 0;" />
                    <div class="contact-list">
                        <div><span class="contact-label">联系人</span>{{ house.contactName || '未填写' }}</div>
                        <div><span class="contact-label">电话</span>{{ house.contactPhone || '未填写' }}</div>
                    </div>
                </el-card>

                <el-card class="detail-card">
                    <template #header><span class="card-label">房源数据</span></template>
                    <div class="data-list">
                        <div class="data-row"><span>押金</span><span>{{ house.deposit || 0 }} 元</span></div>
                        <div class="data-row"><span>类型</span><span>{{ { 0: '整租', 1: '合租', 2: '公寓' }[house.houseType] || '未知' }}</span></div>
                        <div class="data-row"><span>浏览量</span><span>{{ house.viewCount || 0 }}</span></div>
                        <div class="data-row"><span>收藏数</span><span>{{ house.favoriteCount || 0 }}</span></div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 直接租房弹窗 -->
        <el-dialog title="直接租房" v-model="rentVisible" width="420px">
            <el-form label-width="80px">
                <el-form-item label="租期起始">
                    <el-date-picker v-model="rentStartDate" type="date" value-format="YYYY-MM-DD" placeholder="选择起始日期" style="width: 100%;" />
                </el-form-item>
                <el-form-item label="租期结束">
                    <el-date-picker v-model="rentEndDate" type="date" value-format="YYYY-MM-DD" placeholder="选择结束日期" style="width: 100%;" />
                </el-form-item>
                <el-form-item label="月租">
                    <span style="font-size: 18px; color: #e65100; font-weight: bold;">{{ house?.price }} 元/月</span>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="rentVisible = false">取消</el-button>
                <el-button type="primary" class="rent-btn" @click="handleRent" :loading="submitting">确认租房</el-button>
            </template>
        </el-dialog>

        <!-- 预约弹窗 -->
        <el-dialog title="预约看房" v-model="appointmentVisible" width="420px">
            <el-form label-width="80px">
                <el-form-item label="预约时间">
                    <el-date-picker v-model="appointmentTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择预约时间" style="width: 100%;" />
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="appointmentRemark" type="textarea" :rows="2" placeholder="备注信息（选填）" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="appointmentVisible = false">取消</el-button>
                <el-button type="primary" class="green-btn" @click="handleAppointment" :loading="submitting">提交预约</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getHouseDetail } from '@/api/house'
import { toggleFavorite } from '@/api/favorite'
import { createOrder } from '@/api/order'
import { createConversation } from '@/api/chat'
import { getCommentsByHouse, addComment } from '@/api/comment'
import { getNote, saveNote, deleteNote } from '@/api/note'
import { getSimilarHouses } from '@/api/recommend'
import { getUserProfile } from '@/api/user'
import { ElMessage } from 'element-plus'
import httpInstance from '@/utils/http'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const house = ref(null)
const isFav = ref(false)
const rentVisible = ref(false)
const rentStartDate = ref('')
const rentEndDate = ref('')
const appointmentVisible = ref(false)
const appointmentTime = ref('')
const appointmentRemark = ref('')

// 评论相关
const comments = ref([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentLoading = ref(false)
const commentSubmitting = ref(false)
const commentContent = ref('')
const commentRating = ref(5)

// 笔记相关
const noteContent = ref('')
const noteInput = ref('')
const noteEditing = ref(false)
const noteSaving = ref(false)

// 房东信息
const ownerInfo = ref(null)

// 相似房源
const similarList = ref([])

const imageList = computed(() => house.value?.images ? house.value.images.split(',').filter(Boolean) : [])

const loadData = async () => {
    const houseId = route.params.id
    loading.value = true
    try { const res = await getHouseDetail(houseId); house.value = res.data }
    catch (e) { console.log('加载失败', e) }
    finally { loading.value = false }

    loadComments()
    loadNote()
    loadSimilar()
    loadOwnerInfo()
}

onMounted(() => loadData())

// 路由参数变化时重新加载（从相似房源点进来）
watch(() => route.params.id, (newId) => {
    if (newId) { loadData() }
})

// 评论
const loadComments = async () => {
    commentLoading.value = true
    try {
        const res = await getCommentsByHouse(route.params.id, commentPage.value, 5)
        comments.value = res.data?.records || []
        commentTotal.value = res.data?.total || 0
    } catch (e) { console.log('加载评论失败', e) }
    finally { commentLoading.value = false }
}

const handleAddComment = async () => {
    if (!commentContent.value.trim()) { ElMessage.warning('请输入评论内容'); return }
    commentSubmitting.value = true
    try {
        await addComment({ houseId: Number(route.params.id), userId: userStore.userInfo.id, content: commentContent.value.trim(), rating: commentRating.value })
        ElMessage.success({ message: '评论成功', duration: 2000 })
        commentContent.value = ''
        commentRating.value = 5
        commentPage.value = 1
        loadComments()
    } catch (e) { console.log('评论失败', e) }
    finally { commentSubmitting.value = false }
}

// 笔记
const loadNote = async () => {
    try {
        const res = await getNote(route.params.id, userStore.userInfo.id)
        noteContent.value = res.data?.content || ''
        noteInput.value = noteContent.value
    } catch (e) { /* ignore */ }
}

const handleSaveNote = async () => {
    if (!noteInput.value.trim()) { ElMessage.warning('请输入笔记内容'); return }
    noteSaving.value = true
    try {
        await saveNote(route.params.id, userStore.userInfo.id, noteInput.value.trim())
        ElMessage.success({ message: '笔记已保存', duration: 2000 })
        noteContent.value = noteInput.value.trim()
        noteEditing.value = false
    } catch (e) { console.log('保存失败', e) }
    finally { noteSaving.value = false }
}

const handleDeleteNote = async () => {
    try {
        await deleteNote(route.params.id, userStore.userInfo.id)
        ElMessage.success({ message: '笔记已删除', duration: 2000 })
        noteContent.value = ''
        noteInput.value = ''
    } catch (e) { console.log('删除失败', e) }
}

// 房东信息
const loadOwnerInfo = async () => {
    if (!house.value?.ownerId) return
    try {
        const res = await getUserProfile(house.value.ownerId)
        ownerInfo.value = res.data
    } catch (e) { /* ignore */ }
}

// 相似房源
const loadSimilar = async () => {
    try {
        const res = await getSimilarHouses(route.params.id, 4)
        similarList.value = res.data || []
    } catch (e) { /* ignore */ }
}

const handleChat = async () => {
    try {
        const res = await createConversation(userStore.userInfo.id, house.value.ownerId, house.value.id, 0)
        router.push({ path: '/tenant/chat', query: { conversationId: res.data.id } })
    } catch (e) { console.log('创建会话失败', e) }
}

const openRentDialog = () => { rentStartDate.value = ''; rentEndDate.value = ''; rentVisible.value = true }

const handleRent = async () => {
    if (!rentStartDate.value || !rentEndDate.value) { ElMessage.warning('请选择租期起止日期'); return }
    if (rentStartDate.value >= rentEndDate.value) { ElMessage.warning('结束日期必须晚于起始日期'); return }
    submitting.value = true
    try {
        await createOrder({
            houseId: house.value.id,
            tenantId: userStore.userInfo.id,
            ownerId: house.value.ownerId,
            startDate: rentStartDate.value,
            endDate: rentEndDate.value
        })
        ElMessage.success({ message: '租房申请已提交，等待房东确认签约', duration: 3000 })
        rentVisible.value = false
    } catch (e) { console.log('租房失败', e) }
    finally { submitting.value = false }
}

const handleFavorite = async () => {
    try {
        await toggleFavorite(house.value.id, userStore.userInfo.id)
        isFav.value = !isFav.value
        ElMessage.success({ message: isFav.value ? '已收藏' : '已取消收藏', duration: 2000 })
    } catch (e) { console.log('操作失败', e) }
}

const openAppointment = () => { appointmentTime.value = ''; appointmentRemark.value = ''; appointmentVisible.value = true }

const handleAppointment = async () => {
    if (!appointmentTime.value) { ElMessage.warning('请选择预约时间'); return }
    submitting.value = true
    try {
        await httpInstance({ url: '/appointment', method: 'post', data: { houseId: house.value.id, userId: userStore.userInfo.id, appointmentTime: appointmentTime.value, remark: appointmentRemark.value } })
        ElMessage.success({ message: '预约提交成功', duration: 2000 })
        appointmentVisible.value = false
    } catch (e) { console.log('预约失败', e) }
    finally { submitting.value = false }
}
</script>

<style scoped>
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.detail-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.no-image { height: 300px; background: #e8f5e9; display: flex; align-items: center; justify-content: center; color: #81c784; font-size: 16px; border-radius: 8px; }
.house-name { margin: 16px 0 8px; color: #2e5e3a; font-size: 22px; }
.house-address { color: #88a58b; font-size: 14px; }
.info-item { text-align: center; }
.info-label { font-size: 13px; color: #88a58b; margin-bottom: 4px; }
.info-value { font-size: 18px; font-weight: 500; color: #2e5e3a; }
.price-value { color: #e65100; font-size: 24px; font-weight: bold; }
.price-value .unit { font-size: 13px; color: #88a58b; font-weight: normal; }
.sub-title { color: #2e5e3a; font-size: 16px; margin-bottom: 8px; }
.desc-text { color: #606266; line-height: 1.8; white-space: pre-wrap; }

.chat-btn-lg { background: #5c6bc0 !important; border: none !important; border-radius: 12px !important; font-weight: 500; width: 100%; color: #fff !important; }
.chat-btn-lg:hover { background: #3f51b5 !important; }
.rent-btn-lg { background: #e65100 !important; border: none !important; border-radius: 12px !important; font-weight: 500; width: 100%; color: #fff !important; }
.rent-btn-lg:hover { background: #bf360c !important; }
.rent-btn { background: #e65100 !important; border: none !important; color: #fff !important; }
.rent-btn:hover { background: #bf360c !important; }
.green-btn-lg { background: #81c784 !important; border: none !important; border-radius: 12px !important; font-weight: 500; width: 100%; }
.green-btn-lg:hover { background: #66bb6a !important; }
.green-btn { background: #81c784 !important; border: none !important; color: #fff !important; }
.green-btn:hover { background: #66bb6a !important; }
.fav-btn { width: 100%; border-radius: 12px !important; border: 1px solid #c8e6c9 !important; color: #2e5e3a !important; }
.fav-btn:hover { border-color: #81c784 !important; color: #4caf50 !important; }
.fav-active { width: 100%; border-radius: 12px !important; background: #e8f5e9 !important; border: 1px solid #81c784 !important; color: #2e7d32 !important; }

.card-label { color: #2e5e3a; font-weight: 500; }
.owner-info { display: flex; flex-direction: column; }
.owner-header { display: flex; align-items: center; gap: 12px; }
.owner-header:hover .owner-name { color: #4caf50; }
.owner-name { font-size: 16px; font-weight: 500; color: #2e5e3a; transition: color 0.2s; }
.owner-tags { margin-top: 4px; }
.contact-list { display: flex; flex-direction: column; gap: 12px; color: #2e5e3a; }
.contact-label { color: #88a58b; margin-right: 8px; }
.data-list { display: flex; flex-direction: column; gap: 12px; }
.data-row { display: flex; justify-content: space-between; color: #2e5e3a; }
.data-row span:first-child { color: #88a58b; }

/* 笔记 */
.note-text { color: #606266; line-height: 1.8; white-space: pre-wrap; font-size: 14px; }

/* 评论 */
.comment-item { padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.comment-item:last-child { border-bottom: none; }
.comment-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.comment-user { font-size: 13px; font-weight: 500; color: #2e5e3a; }
.comment-time { margin-left: auto; font-size: 12px; color: #aaa; }
.comment-content { font-size: 14px; color: #606266; line-height: 1.6; padding-left: 40px; }

/* 相似房源 */
.house-card { cursor: pointer; border-radius: 12px; overflow: hidden; background: #fff; border: 1px solid #e8f5e9; transition: all 0.3s; }
.house-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(76,175,80,0.12); border-color: #a5d6a7; }
.similar-img { height: 100px; overflow: hidden; background: #e8f5e9; }
.similar-img img { width: 100%; height: 100%; object-fit: cover; }
.similar-img-placeholder { height: 100%; display: flex; align-items: center; justify-content: center; color: #81c784; font-size: 13px; }
.similar-info { padding: 8px 12px; }
.similar-title { font-size: 13px; font-weight: 500; color: #2e5e3a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.similar-price { font-size: 12px; color: #88a58b; margin-top: 4px; }
.similar-price .price { font-size: 16px; font-weight: bold; color: #e65100; }

:deep(.el-pagination.is-background .el-pager li.is-active) { background: #66bb6a !important; }
</style>
