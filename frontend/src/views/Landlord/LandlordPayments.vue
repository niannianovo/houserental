<template>
    <div>
        <el-card>
            <template #header>收租管理</template>

            <!-- 先选择订单 -->
            <div v-if="!selectedOrderId" v-loading="loadingOrders">
                <el-alert title="请选择一个订单查看租金记录" type="info" :closable="false" style="margin-bottom: 16px;" />
                <el-table :data="orders" style="width: 100%">
                    <el-table-column prop="id" label="订单号" width="80" />
                    <el-table-column prop="houseId" label="房源ID" width="80" />
                    <el-table-column prop="tenantId" label="租客ID" width="80" />
                    <el-table-column prop="monthlyRent" label="月租(元)" width="100" />
                    <el-table-column prop="startDate" label="起租日" width="120" />
                    <el-table-column prop="endDate" label="到期日" width="120" />
                    <el-table-column label="操作" width="120">
                        <template #default="{ row }">
                            <el-button type="primary" link size="small" @click="selectOrder(row.id)">查看租金</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>

            <!-- 租金详情 -->
            <div v-else>
                <div style="margin-bottom: 16px;">
                    <el-button @click="backToList">返回订单列表</el-button>
                    <span style="margin-left: 12px; color: #909399;">订单号：{{ selectedOrderId }}</span>
                </div>

                <el-table :data="payments" style="width: 100%" v-loading="loadingPayments">
                    <el-table-column prop="id" label="ID" width="60" />
                    <el-table-column prop="payMonth" label="缴费月份" width="120" />
                    <el-table-column prop="amount" label="金额(元)" width="100" />
                    <el-table-column label="支付状态" width="100">
                        <template #default="{ row }">
                            <el-tag :type="payStatusType(row.status)" size="small">{{ payStatusText(row.status) }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="确认状态" width="100">
                        <template #default="{ row }">
                            <el-tag :type="confirmStatusType(row.confirmStatus)" size="small">
                                {{ confirmStatusText(row.confirmStatus) }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="payMethod" label="支付方式" width="100">
                        <template #default="{ row }">
                            {{ payMethodText(row.payMethod) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="payTime" label="支付时间" min-width="160" />
                    <el-table-column label="操作" width="120" fixed="right">
                        <template #default="{ row }">
                            <el-popconfirm
                                v-if="row.status === 1 && row.confirmStatus === 0"
                                title="确认已收到该笔租金？"
                                @confirm="handleConfirm(row.id)"
                            >
                                <template #reference>
                                    <el-button type="success" link size="small">确认收款</el-button>
                                </template>
                            </el-popconfirm>
                            <span v-else style="color: #c0c4cc; font-size: 12px;">--</span>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyOrders, getPaymentList, confirmPayment } from '@/api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const loadingOrders = ref(false)
const loadingPayments = ref(false)
const orders = ref([])
const payments = ref([])
const selectedOrderId = ref(null)

const payStatusText = (s) => ({ 0: '待支付', 1: '已支付' }[s] || '未知')
const payStatusType = (s) => ({ 0: 'danger', 1: 'success' }[s] || 'info')
const confirmStatusText = (s) => ({ 0: '待确认', 1: '已确认' }[s] || '未知')
const confirmStatusType = (s) => ({ 0: 'warning', 1: 'success' }[s] || 'info')
const payMethodText = (m) => ({ 0: '微信', 1: '支付宝', 2: '银行转账' }[m] || '--')

const loadOrders = async () => {
    loadingOrders.value = true
    try {
        const res = await getMyOrders(userStore.userInfo.id, 1, 50, 1)
        orders.value = res.data?.records || []
    } catch (e) { console.log('加载订单失败', e) }
    finally { loadingOrders.value = false }
}

const selectOrder = async (orderId) => {
    selectedOrderId.value = orderId
    loadingPayments.value = true
    try {
        const res = await getPaymentList(orderId)
        payments.value = res.data || []
    } catch (e) { console.log('加载租金失败', e) }
    finally { loadingPayments.value = false }
}

const backToList = () => {
    selectedOrderId.value = null
    if (orders.value.length === 0) loadOrders()
}

const handleConfirm = async (id) => {
    try {
        await confirmPayment(id, userStore.userInfo.id)
        ElMessage.success({ message: '确认收款成功', duration: 2000 })
        selectOrder(selectedOrderId.value)
    } catch (e) { console.log('确认失败', e) }
}

onMounted(() => {
    if (route.query.orderId) {
        selectOrder(Number(route.query.orderId))
    } else {
        loadOrders()
    }
})
</script>
