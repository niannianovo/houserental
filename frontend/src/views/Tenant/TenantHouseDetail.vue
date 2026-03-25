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
                    <div class="house-address">{{ house.address }}</div>

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
            </el-col>

            <!-- 右侧：操作 -->
            <el-col :span="8">
                <el-card class="detail-card" style="margin-bottom: 20px;">
                    <div style="display: flex; flex-direction: column; gap: 12px;">
                        <el-button type="primary" size="large" class="green-btn-lg" @click="openAppointment">预约看房</el-button>
                        <el-button :class="isFav ? 'fav-active' : 'fav-btn'" size="large" @click="handleFavorite">
                            {{ isFav ? '已收藏' : '收藏房源' }}
                        </el-button>
                    </div>
                </el-card>

                <el-card class="detail-card" style="margin-bottom: 20px;">
                    <template #header><span class="card-label">联系信息</span></template>
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
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getHouseDetail } from '@/api/house'
import { toggleFavorite } from '@/api/favorite'
import { ElMessage } from 'element-plus'
import httpInstance from '@/utils/http'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const house = ref(null)
const isFav = ref(false)
const appointmentVisible = ref(false)
const appointmentTime = ref('')
const appointmentRemark = ref('')

const imageList = computed(() => house.value?.images ? house.value.images.split(',').filter(Boolean) : [])

onMounted(async () => {
    loading.value = true
    try { const res = await getHouseDetail(route.params.id); house.value = res.data }
    catch (e) { console.log('加载失败', e) }
    finally { loading.value = false }
})

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

.green-btn-lg { background: #81c784 !important; border: none !important; border-radius: 12px !important; font-weight: 500; width: 100%; }
.green-btn-lg:hover { background: #66bb6a !important; }
.green-btn { background: #81c784 !important; border: none !important; }
.green-btn:hover { background: #66bb6a !important; }
.fav-btn { width: 100%; border-radius: 12px !important; border: 1px solid #c8e6c9 !important; color: #2e5e3a !important; }
.fav-btn:hover { border-color: #81c784 !important; color: #4caf50 !important; }
.fav-active { width: 100%; border-radius: 12px !important; background: #e8f5e9 !important; border: 1px solid #81c784 !important; color: #2e7d32 !important; }

.card-label { color: #2e5e3a; font-weight: 500; }
.contact-list { display: flex; flex-direction: column; gap: 12px; color: #2e5e3a; }
.contact-label { color: #88a58b; margin-right: 8px; }
.data-list { display: flex; flex-direction: column; gap: 12px; }
.data-row { display: flex; justify-content: space-between; color: #2e5e3a; }
.data-row span:first-child { color: #88a58b; }
</style>
