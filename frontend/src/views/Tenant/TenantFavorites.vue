<template>
    <div>
        <el-card class="green-card">
            <template #header><span class="section-title">我的收藏</span></template>

            <div v-loading="loading">
                <el-row :gutter="16">
                    <el-col :span="6" v-for="item in favorites" :key="item.id">
                        <div class="house-card" @click="$router.push(`/tenant/house/${item.houseId}`)">
                            <div class="house-img">
                                <img v-if="item.houseImages" :src="item.houseImages.split(',')[0]" alt="" />
                                <div v-else class="house-img-placeholder">暂无图片</div>
                                <div v-if="item.houseType != null" class="house-tag">
                                    {{ { 0: '整租', 1: '合租', 2: '公寓' }[item.houseType] || '' }}
                                </div>
                            </div>
                            <div class="house-info">
                                <div class="house-title">{{ item.houseTitle || '房源 #' + item.houseId }}</div>
                                <div class="house-address">{{ [item.houseDistrict, item.houseAddress].filter(Boolean).join(' ') }}</div>
                                <div class="house-meta">
                                    <span v-if="item.roomCount">{{ item.roomCount }}室{{ item.hallCount }}厅</span>
                                    <span v-if="item.houseArea"> · {{ item.houseArea }}m²</span>
                                </div>
                                <div class="house-bottom">
                                    <span class="house-price" v-if="item.housePrice"><span class="price">{{ item.housePrice }}</span> 元/月</span>
                                    <el-button type="danger" link size="small" @click.stop="handleCancel(item)">取消收藏</el-button>
                                </div>
                            </div>
                        </div>
                    </el-col>
                </el-row>
                <el-empty v-if="!loading && favorites.length === 0" description="暂无收藏" />
            </div>

            <el-pagination
                v-if="total > 0"
                style="margin-top: 16px; justify-content: flex-end;"
                background layout="total, prev, pager, next"
                :total="total" :page-size="pageSize"
                v-model:current-page="currentPage"
                @current-change="loadFavorites"
            />
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyFavorites, toggleFavorite } from '@/api/favorite'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const favorites = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 12

const loadFavorites = async () => {
    loading.value = true
    try { const res = await getMyFavorites(userStore.userInfo.id, currentPage.value, pageSize); favorites.value = res.data?.records || []; total.value = res.data?.total || 0 }
    catch (e) { console.log('加载收藏失败', e) } finally { loading.value = false }
}
const handleCancel = async (item) => {
    try { await toggleFavorite(item.houseId, userStore.userInfo.id); ElMessage.success({ message: '已取消收藏', duration: 2000 }); loadFavorites() }
    catch (e) { console.log('取消失败', e) }
}
onMounted(() => loadFavorites())
</script>

<style scoped>
.green-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.house-card {
    cursor: pointer; border-radius: 12px; overflow: hidden;
    background: #fff; border: 1px solid #e8f5e9; margin-bottom: 16px; transition: all 0.3s;
}
.house-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(76,175,80,0.12); border-color: #a5d6a7; }
.house-img { height: 140px; overflow: hidden; background: #e8f5e9; position: relative; }
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
:deep(.el-pagination.is-background .el-pager li.is-active) { background: #66bb6a !important; }
</style>
