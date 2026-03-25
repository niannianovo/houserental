<template>
    <div>
        <!-- 搜索栏 -->
        <el-card class="search-bar">
            <div style="display: flex; gap: 12px;">
                <el-input v-model="keyword" placeholder="搜索房源名称、地址..." size="large" clearable style="flex: 1;" @keyup.enter="goSearch">
                    <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-button type="primary" size="large" class="search-btn" @click="goSearch">搜索</el-button>
            </div>
        </el-card>

        <!-- 快捷入口 -->
        <el-row :gutter="16" style="margin-bottom: 20px;">
            <el-col :span="6" v-for="item in quickEntries" :key="item.path">
                <el-card shadow="hover" class="quick-entry" @click="$router.push(item.path)">
                    <el-icon :size="32" :color="item.color"><component :is="item.icon" /></el-icon>
                    <span>{{ item.label }}</span>
                </el-card>
            </el-col>
        </el-row>

        <!-- 推荐房源 -->
        <el-card class="recommend-card">
            <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span class="section-title">推荐房源</span>
                    <el-button type="primary" link class="more-btn" @click="$router.push('/tenant/search')">查看更多</el-button>
                </div>
            </template>
            <div v-loading="loading">
                <el-row :gutter="16">
                    <el-col :span="6" v-for="house in recommendList" :key="house.id">
                        <div class="house-card" @click="$router.push(`/tenant/house/${house.id}`)">
                            <div class="house-img">
                                <img v-if="house.images" :src="house.images.split(',')[0]" alt="" />
                                <div v-else class="house-img-placeholder">暂无图片</div>
                                <div class="house-tag">
                                    {{ { 0: '整租', 1: '合租', 2: '公寓' }[house.houseType] || '' }}
                                </div>
                            </div>
                            <div class="house-info">
                                <div class="house-title">{{ house.title }}</div>
                                <div class="house-meta">
                                    {{ house.roomCount }}室{{ house.hallCount }}厅 · {{ house.area }}m²
                                </div>
                                <div class="house-price">
                                    <span class="price">{{ house.price }}</span> 元/月
                                </div>
                            </div>
                        </div>
                    </el-col>
                </el-row>
                <el-empty v-if="!loading && recommendList.length === 0" description="暂无推荐房源" />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getRecommendList } from '@/api/recommend'
import { Search, Document, Star, Calendar } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const keyword = ref('')
const recommendList = ref([])

const quickEntries = [
    { path: '/tenant/search', label: '找房', icon: Search, color: '#4caf50' },
    { path: '/tenant/orders', label: '我的订单', icon: Document, color: '#66bb6a' },
    { path: '/tenant/favorites', label: '我的收藏', icon: Star, color: '#81c784' },
    { path: '/tenant/appointments', label: '预约记录', icon: Calendar, color: '#a5d6a7' }
]

const goSearch = () => {
    router.push({ path: '/tenant/search', query: keyword.value ? { keyword: keyword.value } : {} })
}

onMounted(async () => {
    loading.value = true
    try {
        const res = await getRecommendList(userStore.userInfo.id, 8)
        recommendList.value = res.data || []
    } catch (e) { console.log('加载推荐失败', e) }
    finally { loading.value = false }
})
</script>

<style scoped>
.search-bar {
    margin-bottom: 20px;
    border-radius: 16px;
    border: 1px solid #c8e6c9;
}
.search-bar :deep(.el-input__wrapper) {
    border-radius: 12px;
    box-shadow: 0 0 0 1px #e3f0e3 inset;
    background: #f9fcf9;
}
.search-bar :deep(.el-input__wrapper:focus-within) {
    box-shadow: 0 0 0 2px #81c784 inset;
    background: #fff;
}
.search-btn {
    background: #81c784 !important;
    border: none !important;
    border-radius: 12px !important;
    font-weight: 500;
}
.search-btn:hover { background: #66bb6a !important; }

.quick-entry {
    cursor: pointer;
    border-radius: 16px;
    border: 1px solid #e8f5e9;
    transition: all 0.3s;
}
.quick-entry:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(76, 175, 80, 0.15);
    border-color: #a5d6a7;
}
.quick-entry :deep(.el-card__body) {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    padding: 24px;
}
.quick-entry span {
    font-size: 14px;
    color: #2e5e3a;
    font-weight: 500;
}

.recommend-card {
    border-radius: 16px;
    border: 1px solid #e8f5e9;
}
.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #2e5e3a;
}
.more-btn { color: #66bb6a !important; }
.more-btn:hover { color: #2e7d32 !important; }

.house-card {
    cursor: pointer;
    border-radius: 12px;
    overflow: hidden;
    background: #fff;
    border: 1px solid #e8f5e9;
    margin-bottom: 16px;
    transition: all 0.3s;
}
.house-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(76, 175, 80, 0.12);
    border-color: #a5d6a7;
}
.house-img {
    height: 140px;
    overflow: hidden;
    background: #e8f5e9;
    position: relative;
}
.house-img img { width: 100%; height: 100%; object-fit: cover; }
.house-img-placeholder {
    height: 100%; display: flex; align-items: center; justify-content: center;
    color: #81c784; font-size: 14px;
}
.house-tag {
    position: absolute;
    top: 8px;
    left: 8px;
    background: rgba(46, 94, 58, 0.8);
    color: #fff;
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 4px;
}
.house-info { padding: 12px; }
.house-title {
    font-size: 14px;
    font-weight: 500;
    color: #2e5e3a;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.house-meta { font-size: 12px; color: #88a58b; margin-top: 4px; }
.house-price { margin-top: 8px; color: #88a58b; font-size: 12px; }
.house-price .price { font-size: 20px; font-weight: bold; color: #e65100; }
</style>
