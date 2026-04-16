<template>
    <div>
        <el-card class="green-card">
            <template #header>
                <div style="display: flex; align-items: center; gap: 16px;">
                    <span class="section-title">用户互评</span>
                    <el-radio-group v-model="activeTab" @change="handleTabChange">
                        <el-radio-button value="received">收到的评价</el-radio-button>
                        <el-radio-button value="sent">发出的评价</el-radio-button>
                    </el-radio-group>
                </div>
            </template>

            <div v-loading="loading">
                <div v-for="r in reviews" :key="r.id" class="review-item">
                    <div class="review-header">
                        <el-avatar :size="36" style="background: #81c784;">
                            {{ (activeTab === 'received' ? r.reviewerNickname : r.targetNickname || '?').charAt(0) }}
                        </el-avatar>
                        <div>
                            <span class="review-user">
                                {{ activeTab === 'received' ? r.reviewerNickname : r.targetNickname }}
                            </span>
                            <span class="review-role">{{ { 0: '租客', 1: '房东' }[r.reviewerRole] || '' }}</span>
                        </div>
                        <el-rate v-model="r.rating" disabled :size="'small'" :colors="['#81c784', '#66bb6a', '#4caf50']" style="margin-left: 8px;" />
                        <span class="review-time">{{ r.createTime }}</span>
                    </div>
                    <div class="review-content">{{ r.content }}</div>
                    <div class="review-order">订单 #{{ r.orderId }}</div>
                </div>
                <el-empty v-if="!loading && reviews.length === 0" :description="activeTab === 'received' ? '暂无收到的评价' : '暂无发出的评价'" />
            </div>

            <el-pagination
                v-if="total > 10"
                style="margin-top: 16px; justify-content: flex-end;"
                background layout="total, prev, pager, next"
                :total="total" :page-size="10"
                v-model:current-page="currentPage"
                @current-change="loadReviews"
            />
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyReceivedReviews, getMySentReviews } from '@/api/review'

const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('received')
const reviews = ref([])
const total = ref(0)
const currentPage = ref(1)

const loadReviews = async () => {
    loading.value = true
    try {
        const userId = userStore.userInfo.id
        const res = activeTab.value === 'received'
            ? await getMyReceivedReviews(userId, currentPage.value, 10)
            : await getMySentReviews(userId, currentPage.value, 10)
        reviews.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载评价失败', e) }
    finally { loading.value = false }
}

const handleTabChange = () => { currentPage.value = 1; loadReviews() }

onMounted(() => loadReviews())
</script>

<style scoped>
.green-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.review-item { padding: 16px 0; border-bottom: 1px solid #f0f0f0; }
.review-item:last-child { border-bottom: none; }
.review-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.review-user { font-size: 14px; font-weight: 500; color: #2e5e3a; }
.review-role { font-size: 12px; color: #88a58b; margin-left: 4px; }
.review-time { margin-left: auto; font-size: 12px; color: #aaa; }
.review-content { font-size: 14px; color: #606266; line-height: 1.6; padding-left: 44px; }
.review-order { font-size: 12px; color: #aaa; padding-left: 44px; margin-top: 4px; }
:deep(.el-pagination.is-background .el-pager li.is-active) { background: #66bb6a !important; }
:deep(.el-radio-button__inner) { border-color: #c8e6c9 !important; }
:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) { background: #81c784 !important; border-color: #81c784 !important; }
</style>
