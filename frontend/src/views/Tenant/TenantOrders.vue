<template>
    <div>
        <el-card class="green-card">
            <template #header><span class="section-title">我的订单</span></template>

            <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="green-tabs">
                <el-tab-pane label="当前订单" name="current" />
                <el-tab-pane label="历史订单" name="history" />
            </el-tabs>

            <el-table :data="orders" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="订单号" width="80" />
                <el-table-column prop="houseId" label="房源ID" width="80" />
                <el-table-column prop="monthlyRent" label="月租(元)" width="100" />
                <el-table-column prop="startDate" label="起租日" width="120" />
                <el-table-column prop="endDate" label="到期日" width="120" />
                <el-table-column label="状态" width="120">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <template v-if="row.status === 1">
                            <el-button type="primary" link size="small" @click="openPay(row)">去缴费</el-button>
                            <el-popconfirm title="确定申请退租？" @confirm="handleQuit(row)">
                                <template #reference>
                                    <el-button type="warning" link size="small">申请退租</el-button>
                                </template>
                            </el-popconfirm>
                        </template>
                        <span v-if="row.status === 4" style="color:#E6A23C;font-size:12px;">退租审批中</span>
                    </template>
                </el-table-column>
            </el-table>

            <el-pagination
                v-if="total > 0"
                style="margin-top: 16px; justify-content: flex-end;"
                background layout="total, prev, pager, next"
                :total="total" :page-size="pageSize"
                v-model:current-page="currentPage"
                @current-change="loadOrders"
            />
        </el-card>

        <!-- 缴费弹窗 -->
        <el-dialog title="租金缴费" v-model="payVisible" width="500px">
            <el-table :data="payments" size="small" v-loading="loadingPay">
                <el-table-column prop="payMonth" label="月份" width="100" />
                <el-table-column prop="amount" label="金额(元)" width="90" />
                <el-table-column label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                            {{ row.status === 1 ? '已支付' : '待支付' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                    <template #default="{ row }">
                        <el-button v-if="row.status === 0" type="primary" link size="small" @click="handlePay(row)">支付</el-button>
                        <span v-else style="color:#c0c4cc;font-size:12px;">--</span>
                    </template>
                </el-table-column>
            </el-table>
        </el-dialog>

        <!-- 支付方式 -->
        <el-dialog title="选择支付方式" v-model="payMethodVisible" width="360px">
            <el-radio-group v-model="payMethod" style="display: flex; flex-direction: column; gap: 12px;">
                <el-radio :label="0">微信支付</el-radio>
                <el-radio :label="1">支付宝</el-radio>
                <el-radio :label="2">银行转账</el-radio>
            </el-radio-group>
            <template #footer>
                <el-button @click="payMethodVisible = false">取消</el-button>
                <el-button type="primary" class="green-btn" @click="confirmPay" :loading="paying">确认支付</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyOrders, getOrderHistory, getPaymentList, quitApply } from '@/api/order'
import { ElMessage } from 'element-plus'
import httpInstance from '@/utils/http'

const userStore = useUserStore()
const loading = ref(false)
const loadingPay = ref(false)
const paying = ref(false)
const activeTab = ref('current')
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const payVisible = ref(false)
const payMethodVisible = ref(false)
const payments = ref([])
const payMethod = ref(0)
const currentPayId = ref(null)

const statusText = (s) => ({ 0: '待签约', 1: '履行中', 2: '已到期', 3: '已退租', 4: '申请退租中' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: '', 4: 'danger' }[s] || 'info')

const loadOrders = async () => {
    loading.value = true
    try {
        const fn = activeTab.value === 'current' ? getMyOrders : getOrderHistory
        const res = await fn(userStore.userInfo.id, currentPage.value, pageSize)
        orders.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载失败', e) } finally { loading.value = false }
}
const handleTabChange = () => { currentPage.value = 1; loadOrders() }

const openPay = async (row) => {
    payVisible.value = true; loadingPay.value = true
    try { const res = await getPaymentList(row.id); payments.value = res.data || [] }
    catch (e) { console.log('加载租金失败', e) } finally { loadingPay.value = false }
}
const handlePay = (row) => { currentPayId.value = row.id; payMethod.value = 0; payMethodVisible.value = true }
const confirmPay = async () => {
    paying.value = true
    try {
        await httpInstance({ url: `/rent-payment/pay/${currentPayId.value}`, method: 'post', params: { payMethod: payMethod.value } })
        ElMessage.success({ message: '支付成功', duration: 2000 })
        payMethodVisible.value = false
        const idx = payments.value.findIndex(p => p.id === currentPayId.value)
        if (idx >= 0) payments.value[idx].status = 1
    } catch (e) { console.log('支付失败', e) } finally { paying.value = false }
}
const handleQuit = async (row) => {
    try { await quitApply(row.id, userStore.userInfo.id); ElMessage.success({ message: '已提交退租申请', duration: 2000 }); loadOrders() }
    catch (e) { console.log('申请失败', e) }
}
onMounted(() => loadOrders())
</script>

<style scoped>
.green-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.green-btn { background: #81c784 !important; border: none !important; }
.green-btn:hover { background: #66bb6a !important; }
.green-tabs :deep(.el-tabs__active-bar) { background: #81c784; }
.green-tabs :deep(.el-tabs__item.is-active) { color: #2e7d32; }
:deep(.el-pagination.is-background .el-pager li.is-active) { background: #66bb6a !important; }
</style>
