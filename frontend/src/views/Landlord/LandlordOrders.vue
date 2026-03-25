<template>
    <div>
        <el-card>
            <template #header>订单管理</template>

            <el-tabs v-model="activeTab" @tab-change="handleTabChange">
                <el-tab-pane label="当前订单" name="current" />
                <el-tab-pane label="历史订单" name="history" />
            </el-tabs>

            <el-table :data="orders" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="订单号" width="80" />
                <el-table-column prop="houseId" label="房源ID" width="80" />
                <el-table-column prop="tenantId" label="租客ID" width="80" />
                <el-table-column prop="monthlyRent" label="月租(元)" width="100" />
                <el-table-column prop="startDate" label="起租日" width="120" />
                <el-table-column prop="endDate" label="到期日" width="120" />
                <el-table-column label="状态" width="120">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="220" fixed="right">
                    <template #default="{ row }">
                        <!-- 待签约：房东可确认 -->
                        <el-button v-if="row.status === 0" type="success" link size="small" @click="handleSign(row)">确认签约</el-button>
                        <!-- 履行中：可续租/退租 -->
                        <template v-if="row.status === 1">
                            <el-button type="primary" link size="small" @click="openRenew(row)">续租</el-button>
                            <el-popconfirm title="确定申请退租？" @confirm="handleQuit(row)">
                                <template #reference>
                                    <el-button type="warning" link size="small">退租</el-button>
                                </template>
                            </el-popconfirm>
                        </template>
                        <!-- 申请退租中：可确认退租 -->
                        <el-popconfirm v-if="row.status === 4" title="确认退租？" @confirm="handleQuitConfirm(row)">
                            <template #reference>
                                <el-button type="danger" link size="small">确认退租</el-button>
                            </template>
                        </el-popconfirm>
                        <el-button type="primary" link size="small" @click="$router.push({ path: '/landlord/payments', query: { orderId: row.id } })">查看租金</el-button>
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

        <!-- 续租弹窗 -->
        <el-dialog title="续租" v-model="renewVisible" width="400px">
            <el-form label-width="80px">
                <el-form-item label="新到期日">
                    <el-date-picker v-model="renewDate" type="date" value-format="YYYY-MM-DD" placeholder="选择新到期日" style="width: 100%;" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="renewVisible = false">取消</el-button>
                <el-button type="primary" @click="handleRenew" :loading="submitting">确认续租</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyOrders, getOrderHistory, signOrder, renewOrder, quitApply, quitConfirm } from '@/api/order'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('current')
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const renewVisible = ref(false)
const renewDate = ref('')
const renewOrderId = ref(null)

const statusText = (s) => ({ 0: '待签约', 1: '履行中', 2: '已到期', 3: '已退租', 4: '申请退租中' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: '', 4: 'danger' }[s] || 'info')

const loadOrders = async () => {
    loading.value = true
    try {
        const fn = activeTab.value === 'current' ? getMyOrders : getOrderHistory
        const res = await fn(userStore.userInfo.id, currentPage.value, pageSize)
        orders.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) {
        console.log('加载订单失败', e)
    } finally {
        loading.value = false
    }
}

const handleTabChange = () => {
    currentPage.value = 1
    loadOrders()
}

const handleSign = async (row) => {
    try {
        await signOrder(row.id, row.tenantId)
        ElMessage.success({ message: '签约成功', duration: 2000 })
        loadOrders()
    } catch (e) { console.log('签约失败', e) }
}

const openRenew = (row) => {
    renewOrderId.value = row.id
    renewDate.value = ''
    renewVisible.value = true
}

const handleRenew = async () => {
    if (!renewDate.value) {
        ElMessage.warning('请选择新到期日')
        return
    }
    submitting.value = true
    try {
        await renewOrder(renewOrderId.value, renewDate.value)
        ElMessage.success({ message: '续租成功', duration: 2000 })
        renewVisible.value = false
        loadOrders()
    } catch (e) { console.log('续租失败', e) } finally { submitting.value = false }
}

const handleQuit = async (row) => {
    try {
        await quitApply(row.id, userStore.userInfo.id)
        ElMessage.success({ message: '已申请退租', duration: 2000 })
        loadOrders()
    } catch (e) { console.log('退租申请失败', e) }
}

const handleQuitConfirm = async (row) => {
    try {
        await quitConfirm(row.id, userStore.userInfo.id)
        ElMessage.success({ message: '已确认退租', duration: 2000 })
        loadOrders()
    } catch (e) { console.log('确认退租失败', e) }
}

onMounted(() => loadOrders())
</script>
