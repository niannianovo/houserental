<template>
    <div v-loading="loading">
        <el-page-header @back="$router.back()" style="margin-bottom: 20px;">
            <template #content><span class="section-title">房源详情</span></template>
        </el-page-header>

        <el-row :gutter="20" v-if="house">
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

                    <div class="title-row">
                        <h2 class="house-name">{{ house.title }}</h2>
                        <el-tag :type="statusTag(house.status).type" size="small">{{ statusTag(house.status).text }}</el-tag>
                        <el-tag v-if="house.verifyStatus !== 1" :type="verifyTag(house.verifyStatus).type" size="small">
                            {{ verifyTag(house.verifyStatus).text }}
                        </el-tag>
                    </div>
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

                <el-card class="detail-card" style="margin-bottom: 20px;">
                    <template #header><span class="card-label">房源评论 ({{ commentTotal }})</span></template>
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
            </el-col>

            <el-col :span="8">
                <el-card class="detail-card" style="margin-bottom: 20px;">
                    <template #header><span class="card-label">联系方式</span></template>
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
                        <div class="data-row"><span>沟通数</span><span>{{ house.contactCount || 0 }}</span></div>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getHouseDetail } from '@/api/house'
import { getCommentsByHouse } from '@/api/comment'

const route = useRoute()
const loading = ref(false)
const house = ref(null)

const comments = ref([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentLoading = ref(false)

const imageList = computed(() => house.value?.images ? house.value.images.split(',').filter(Boolean) : [])

const statusTag = (s) => ({
    0: { text: '待审核', type: 'warning' },
    1: { text: '已上架', type: 'success' },
    2: { text: '已下架', type: 'info' },
    3: { text: '已出租', type: 'danger' }
}[s] || { text: '未知', type: 'info' })

const verifyTag = (s) => ({
    0: { text: '未审核', type: 'warning' },
    1: { text: '已通过', type: 'success' },
    2: { text: '已驳回', type: 'danger' }
}[s] || { text: '未知', type: 'info' })

const loadData = async () => {
    loading.value = true
    try { const res = await getHouseDetail(route.params.id); house.value = res.data }
    catch (e) { console.log('加载失败', e) }
    finally { loading.value = false }
    loadComments()
}

const loadComments = async () => {
    commentLoading.value = true
    try {
        const res = await getCommentsByHouse(route.params.id, commentPage.value, 5)
        comments.value = res.data?.records || []
        commentTotal.value = res.data?.total || 0
    } catch (e) { console.log('加载评论失败', e) }
    finally { commentLoading.value = false }
}

onMounted(() => loadData())
watch(() => route.params.id, (newId) => { if (newId) loadData() })
</script>

<style scoped>
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.detail-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.no-image { height: 300px; background: #e8f5e9; display: flex; align-items: center; justify-content: center; color: #81c784; font-size: 16px; border-radius: 8px; }
.title-row { display: flex; align-items: center; gap: 10px; margin: 16px 0 8px; flex-wrap: wrap; }
.house-name { margin: 0; color: #2e5e3a; font-size: 22px; }
.house-address { color: #88a58b; font-size: 14px; }
.info-item { text-align: center; }
.info-label { font-size: 13px; color: #88a58b; margin-bottom: 4px; }
.info-value { font-size: 18px; font-weight: 500; color: #2e5e3a; }
.price-value { color: #e65100; font-size: 24px; font-weight: bold; }
.price-value .unit { font-size: 13px; color: #88a58b; font-weight: normal; }
.sub-title { color: #2e5e3a; font-size: 16px; margin-bottom: 8px; }
.desc-text { color: #606266; line-height: 1.8; white-space: pre-wrap; }

.card-label { color: #2e5e3a; font-weight: 500; }
.contact-list { display: flex; flex-direction: column; gap: 12px; color: #2e5e3a; }
.contact-label { color: #88a58b; margin-right: 8px; }
.data-list { display: flex; flex-direction: column; gap: 12px; }
.data-row { display: flex; justify-content: space-between; color: #2e5e3a; }
.data-row span:first-child { color: #88a58b; }

.comment-item { padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.comment-item:last-child { border-bottom: none; }
.comment-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.comment-user { font-size: 13px; font-weight: 500; color: #2e5e3a; }
.comment-time { margin-left: auto; font-size: 12px; color: #aaa; }
.comment-content { font-size: 14px; color: #606266; line-height: 1.6; padding-left: 40px; }

:deep(.el-pagination.is-background .el-pager li.is-active) { background: #66bb6a !important; }
</style>
