<template>
    <div>
        <el-card class="search-card">
            <template #header><span class="section-title">找房</span></template>

            <!-- 筛选条件 -->
            <div class="filter-bar">
                <el-input v-model="filters.keyword" placeholder="关键词" clearable style="width: 180px;" />
                <el-input v-model="filters.address" placeholder="地址" clearable style="width: 140px;" />
                <el-select v-model="filters.houseType" placeholder="类型" clearable style="width: 110px;">
                    <el-option label="整租" :value="0" />
                    <el-option label="合租" :value="1" />
                    <el-option label="公寓" :value="2" />
                </el-select>
                <el-input-number v-model="filters.minPrice" placeholder="最低价" :min="0" :step="500" controls-position="right" style="width: 120px;" />
                <span style="color: #88a58b;">—</span>
                <el-input-number v-model="filters.maxPrice" placeholder="最高价" :min="0" :step="500" controls-position="right" style="width: 120px;" />
                <el-button type="primary" class="green-btn" @click="handleSearch">搜索</el-button>
                <el-button @click="handleReset">重置</el-button>
            </div>

            <!-- 房源列表 -->
            <div v-loading="loading" style="margin-top: 20px;">
                <el-row :gutter="16">
                    <el-col :span="6" v-for="house in houses" :key="house.id">
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
                                <div class="house-address">{{ house.address }}</div>
                                <div class="house-meta">
                                    {{ house.roomCount }}室{{ house.hallCount }}厅 · {{ house.area }}m² · {{ house.floor }}楼
                                </div>
                                <div class="house-bottom">
                                    <span class="house-price"><span class="price">{{ house.price }}</span> 元/月</span>
                                    <span class="house-views">{{ house.viewCount || 0 }}浏览</span>
                                </div>
                            </div>
                        </div>
                    </el-col>
                </el-row>
                <el-empty v-if="!loading && houses.length === 0" description="没有找到符合条件的房源" />
            </div>

            <el-pagination
                v-if="total > 0"
                style="margin-top: 16px; justify-content: flex-end;"
                background layout="total, prev, pager, next"
                :total="total" :page-size="pageSize"
                v-model:current-page="currentPage"
                @current-change="loadHouses"
            />
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { searchHouses } from '@/api/house'

const route = useRoute()
const loading = ref(false)
const houses = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 12
const filters = ref({ keyword: '', address: '', houseType: null, minPrice: null, maxPrice: null })

const loadHouses = async () => {
    loading.value = true
    try {
        const params = { page: currentPage.value, size: pageSize }
        if (filters.value.keyword) params.keyword = filters.value.keyword
        if (filters.value.address) params.address = filters.value.address
        if (filters.value.houseType !== null && filters.value.houseType !== undefined) params.houseType = filters.value.houseType
        if (filters.value.minPrice) params.minPrice = filters.value.minPrice
        if (filters.value.maxPrice) params.maxPrice = filters.value.maxPrice
        const res = await searchHouses(params)
        houses.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('搜索失败', e) }
    finally { loading.value = false }
}

const handleSearch = () => { currentPage.value = 1; loadHouses() }
const handleReset = () => { filters.value = { keyword: '', address: '', houseType: null, minPrice: null, maxPrice: null }; handleSearch() }

onMounted(() => {
    if (route.query.keyword) filters.value.keyword = route.query.keyword
    loadHouses()
})
</script>

<style scoped>
.search-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.filter-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.green-btn { background: #81c784 !important; border: none !important; }
.green-btn:hover { background: #66bb6a !important; }

.house-card {
    cursor: pointer; border-radius: 12px; overflow: hidden;
    background: #fff; border: 1px solid #e8f5e9; margin-bottom: 16px; transition: all 0.3s;
}
.house-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(76, 175, 80, 0.12);
    border-color: #a5d6a7;
}
.house-img { height: 150px; overflow: hidden; background: #e8f5e9; position: relative; }
.house-img img { width: 100%; height: 100%; object-fit: cover; }
.house-img-placeholder { height: 100%; display: flex; align-items: center; justify-content: center; color: #81c784; font-size: 14px; }
.house-tag { position: absolute; top: 8px; left: 8px; background: rgba(46,94,58,0.8); color: #fff; font-size: 11px; padding: 2px 8px; border-radius: 4px; }
.house-info { padding: 12px; }
.house-title { font-size: 14px; font-weight: 500; color: #2e5e3a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.house-address { font-size: 12px; color: #88a58b; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.house-meta { font-size: 12px; color: #88a58b; margin-top: 4px; }
.house-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.house-price { color: #88a58b; font-size: 12px; }
.house-price .price { font-size: 18px; font-weight: bold; color: #e65100; }
.house-views { font-size: 12px; color: #b8d4ba; }

:deep(.el-pagination.is-background .el-pager li.is-active) { background: #66bb6a !important; }
</style>
