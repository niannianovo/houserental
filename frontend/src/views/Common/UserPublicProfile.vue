<template>
    <div v-loading="loading">
        <el-page-header @back="$router.back()" style="margin-bottom: 20px;">
            <template #content><span class="section-title">用户主页</span></template>
        </el-page-header>

        <el-row :gutter="20" v-if="userInfo">
            <!-- 左侧：基本信息 -->
            <el-col :span="8">
                <el-card class="green-card">
                    <div class="profile-header">
                        <el-avatar :size="80" :src="userInfo.avatar" style="background: #81c784;">
                            {{ (userInfo.nickname || '?').charAt(0) }}
                        </el-avatar>
                        <h2 class="profile-name">{{ userInfo.nickname || '未知用户' }}</h2>
                        <div class="profile-tags">
                            <el-tag v-if="userInfo.currentRole === 1" type="success" size="small">房东</el-tag>
                            <el-tag v-else size="small">租客</el-tag>
                            <el-tag v-if="userInfo.isEmailVerified === 1" type="warning" size="small">已验证邮箱</el-tag>
                        </div>
                        <div class="profile-meta">注册时间：{{ userInfo.createTime }}</div>
                    </div>

                    <el-divider />

                    <div class="stats-row">
                        <div class="stat-item">
                            <div class="stat-value">{{ reviewTotal }}</div>
                            <div class="stat-label">收到评价</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">{{ avgRating }}</div>
                            <div class="stat-label">平均评分</div>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <!-- 右侧：收到的评价 -->
            <el-col :span="16">
                <el-card class="green-card">
                    <template #header><span class="card-label">收到的评价 ({{ reviewTotal }})</span></template>

                    <div v-loading="reviewLoading">
                        <div v-for="r in reviews" :key="r.id" class="review-item">
                            <div class="review-header">
                                <el-avatar :size="36" style="background: #81c784;">
                                    {{ (r.reviewerNickname || '?').charAt(0) }}
                                </el-avatar>
                                <div>
                                    <span class="review-user">{{ r.reviewerNickname || '匿名用户' }}</span>
                                    <span class="review-role">{{ { 0: '租客', 1: '房东' }[r.reviewerRole] || '' }}</span>
                                </div>
                                <el-rate v-model="r.rating" disabled :size="'small'" :colors="['#81c784', '#66bb6a', '#4caf50']" style="margin-left: 8px;" />
                                <span class="review-time">{{ r.createTime }}</span>
                            </div>
                            <div class="review-content">{{ r.content }}</div>
                        </div>
                        <el-empty v-if="!reviewLoading && reviews.length === 0" description="暂无评价" :image-size="80" />
                    </div>

                    <el-pagination
                        v-if="reviewTotal > 10"
                        style="margin-top: 16px; justify-content: flex-end;"
                        background layout="total, prev, pager, next"
                        :total="reviewTotal" :page-size="10"
                        v-model:current-page="currentPage"
                        @current-change="loadReviews"
                    />
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getUserProfile } from '@/api/user'
import { getReviewsByTarget } from '@/api/review'

const route = useRoute()
const loading = ref(false)
const userInfo = ref(null)
const reviews = ref([])
const reviewTotal = ref(0)
const reviewLoading = ref(false)
const currentPage = ref(1)

const avgRating = computed(() => {
    if (reviews.value.length === 0) return '-'
    const sum = reviews.value.reduce((acc, r) => acc + (r.rating || 0), 0)
    return (sum / reviews.value.length).toFixed(1)
})

const loadProfile = async () => {
    const userId = route.params.id
    loading.value = true
    try {
        const res = await getUserProfile(userId)
        userInfo.value = res.data
    } catch (e) {
        console.log('加载用户信息失败', e)
    } finally {
        loading.value = false
    }
    loadReviews()
}

const loadReviews = async () => {
    reviewLoading.value = true
    try {
        const res = await getReviewsByTarget(route.params.id, currentPage.value, 10)
        reviews.value = res.data?.records || []
        reviewTotal.value = res.data?.total || 0
    } catch (e) {
        console.log('加载评价失败', e)
    } finally {
        reviewLoading.value = false
    }
}

onMounted(() => loadProfile())

watch(() => route.params.id, (newId) => {
    if (newId) {
        currentPage.value = 1
        loadProfile()
    }
})
</script>

<style scoped>
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.green-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.card-label { color: #2e5e3a; font-weight: 500; }

.profile-header { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 16px 0; }
.profile-name { margin: 0; color: #2e5e3a; font-size: 20px; }
.profile-tags { display: flex; gap: 8px; }
.profile-meta { font-size: 13px; color: #88a58b; }

.stats-row { display: flex; justify-content: space-around; }
.stat-item { text-align: center; }
.stat-value { font-size: 24px; font-weight: bold; color: #2e5e3a; }
.stat-label { font-size: 13px; color: #88a58b; margin-top: 4px; }

.review-item { padding: 16px 0; border-bottom: 1px solid #f0f0f0; }
.review-item:last-child { border-bottom: none; }
.review-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.review-user { font-size: 14px; font-weight: 500; color: #2e5e3a; }
.review-role { font-size: 12px; color: #88a58b; margin-left: 4px; }
.review-time { margin-left: auto; font-size: 12px; color: #aaa; }
.review-content { font-size: 14px; color: #606266; line-height: 1.6; padding-left: 44px; }

:deep(.el-pagination.is-background .el-pager li.is-active) { background: #66bb6a !important; }
</style>
