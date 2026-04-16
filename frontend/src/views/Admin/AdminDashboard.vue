<template>
    <div>
        <el-row :gutter="16" style="margin-bottom: 20px;">
            <el-col :span="4" v-for="item in statCards" :key="item.label">
                <el-card shadow="hover">
                    <div class="stat-card">
                        <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
                        <div class="stat-label">{{ item.label }}</div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <el-card>
            <template #header>订单趋势（近12个月）</template>
            <div v-if="trendData.length > 0" class="trend-chart">
                <div v-for="item in trendData" :key="item.month" class="trend-bar-wrapper">
                    <div class="trend-bar" :style="{ height: getBarHeight(item.value) + 'px' }">
                        <span class="trend-value">{{ item.value }}</span>
                    </div>
                    <span class="trend-label">{{ item.month.slice(5) }}月</span>
                </div>
            </div>
            <el-empty v-else description="暂无趋势数据" />
        </el-card>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboardOverview, getOrderTrend } from '@/api/admin'

const overview = ref({})
const trendData = ref([])

const statCards = computed(() => [
    { label: '用户总数', value: overview.value.userCount || 0, color: '#409EFF' },
    { label: '房源总数', value: overview.value.houseCount || 0, color: '#67C23A' },
    { label: '本月订单', value: overview.value.monthlyOrderCount || 0, color: '#E6A23C' },
    { label: '待审核房源', value: overview.value.pendingVerifyCount || 0, color: '#F56C6C' },
    { label: '本月收入', value: overview.value.monthlyPaymentTotal || 0, color: '#409EFF' }
])

const maxTrend = computed(() => Math.max(...trendData.value.map(i => Number(i.value) || 0), 1))
const getBarHeight = (val) => Math.max((Number(val) / maxTrend.value) * 160, 4)

onMounted(async () => {
    try {
        const [overviewRes, trendRes] = await Promise.all([getDashboardOverview(), getOrderTrend(12)])
        overview.value = overviewRes.data || {}
        trendData.value = trendRes.data || []
    } catch (e) { console.log('加载失败', e) }
})
</script>

<style scoped>
.stat-card { text-align: center; padding: 8px 0; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }

.trend-chart {
    display: flex; align-items: flex-end; gap: 12px; height: 200px; padding: 16px 0;
}
.trend-bar-wrapper {
    flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: flex-end;
}
.trend-bar {
    width: 32px; background: linear-gradient(180deg, #409EFF, #79bbff); border-radius: 4px 4px 0 0;
    position: relative; min-height: 4px; transition: height 0.3s;
}
.trend-value {
    position: absolute; top: -20px; left: 50%; transform: translateX(-50%);
    font-size: 11px; color: #606266; white-space: nowrap;
}
.trend-label { font-size: 12px; color: #909399; margin-top: 6px; }
</style>
