<template>
    <div>
        <!-- 统计卡片 -->
        <el-row :gutter="20" style="margin-bottom: 20px;">
            <el-col :span="6">
                <el-card shadow="hover">
                    <div class="stat-card">
                        <div class="stat-icon" style="background: #5b9bd5;"><el-icon :size="28"><House /></el-icon></div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.houseCount }}</div>
                            <div class="stat-label">我的房源</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover">
                    <div class="stat-card">
                        <div class="stat-icon" style="background: #4a8bc2;"><el-icon :size="28"><Document /></el-icon></div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.orderCount }}</div>
                            <div class="stat-label">租赁订单</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover">
                    <div class="stat-card">
                        <div class="stat-icon" style="background: #7ab3e0;"><el-icon :size="28"><Money /></el-icon></div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.pendingPayment }}</div>
                            <div class="stat-label">待确认收款</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover">
                    <div class="stat-card">
                        <div class="stat-icon" style="background: #3a7bbf;"><el-icon :size="28"><Calendar /></el-icon></div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.appointmentCount }}</div>
                            <div class="stat-label">待处理预约</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <el-row :gutter="20">
            <!-- 最近房源 -->
            <el-col :span="12">
                <el-card>
                    <template #header>
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <span>我的房源</span>
                            <el-button type="primary" link @click="$router.push('/landlord/houses')">查看全部</el-button>
                        </div>
                    </template>
                    <el-table :data="recentHouses" style="width: 100%" size="small" v-loading="loading">
                        <el-table-column prop="title" label="房源名称" show-overflow-tooltip />
                        <el-table-column prop="price" label="月租(元)" width="100" />
                        <el-table-column label="状态" width="100">
                            <template #default="{ row }">
                                <el-tag :type="houseStatusType(row.status)" size="small">{{ houseStatusText(row.status) }}</el-tag>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>
            </el-col>

            <!-- 最近订单 -->
            <el-col :span="12">
                <el-card>
                    <template #header>
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <span>最近订单</span>
                            <el-button type="primary" link @click="$router.push('/landlord/orders')">查看全部</el-button>
                        </div>
                    </template>
                    <el-table :data="recentOrders" style="width: 100%" size="small" v-loading="loading">
                        <el-table-column prop="houseId" label="房源ID" width="80" />
                        <el-table-column prop="monthlyRent" label="月租(元)" width="100" />
                        <el-table-column label="状态" width="100">
                            <template #default="{ row }">
                                <el-tag :type="orderStatusType(row.status)" size="small">{{ orderStatusText(row.status) }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="startDate" label="起租日" width="110" />
                    </el-table>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyHouses } from '@/api/house'
import { getMyOrders } from '@/api/order'
import { House, Document, Money, Calendar } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const recentHouses = ref([])
const recentOrders = ref([])
const stats = ref({ houseCount: 0, orderCount: 0, pendingPayment: 0, appointmentCount: 0 })

const houseStatusText = (s) => ({ 0: '待审核', 1: '已上架', 2: '已下架', 3: '已出租' }[s] || '未知')
const houseStatusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'primary' }[s] || 'info')
const orderStatusText = (s) => ({ 0: '待签约', 1: '履行中', 2: '已到期', 3: '已退租', 4: '申请退租中' }[s] || '未知')
const orderStatusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: '', 4: 'danger' }[s] || 'info')

onMounted(async () => {
    loading.value = true
    try {
        const [housesRes, ordersRes] = await Promise.all([
            getMyHouses(userStore.userInfo.id, 1, 5),
            getMyOrders(userStore.userInfo.id, 1, 5)
        ])
        recentHouses.value = housesRes.data?.records || []
        recentOrders.value = ordersRes.data?.records || []
        stats.value.houseCount = housesRes.data?.total || 0
        stats.value.orderCount = ordersRes.data?.total || 0
    } catch (e) {
        console.log('加载数据失败', e)
    } finally {
        loading.value = false
    }
})
</script>

<style scoped>
.stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
}
.stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
}
.stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #1a3a5c;
}
.stat-label {
    font-size: 14px;
    color: #7a94a8;
    margin-top: 4px;
}
:deep(.el-card) { border-radius: 16px; border: 1px solid #e8f0f8; }
</style>
