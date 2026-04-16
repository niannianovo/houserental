<template>
    <div>
        <el-card class="blue-card">
            <template #header><span class="section-title">订单管理</span></template>

            <el-tabs v-model="activeTab" @tab-change="handleTabChange">
                <el-tab-pane label="当前订单" name="current" />
                <el-tab-pane label="历史订单" name="history" />
            </el-tabs>

            <el-table :data="orders" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="订单号" width="80" />
                <el-table-column label="房源" min-width="220">
                    <template #default="{ row }">
                        <div class="house-cell" @click="goHouse(row.houseId)">
                            <img v-if="row.houseImages" :src="row.houseImages.split(',')[0]" class="house-thumb" />
                            <div v-else class="house-thumb house-thumb-empty">无图</div>
                            <span class="house-title">{{ row.houseTitle || ('房源#' + row.houseId) }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="租客" width="90">
                    <template #default="{ row }">
                        <el-link type="primary" :underline="false" @click="$router.push(`/landlord/user/${row.tenantId}`)">#{{ row.tenantId }}</el-link>
                    </template>
                </el-table-column>
                <el-table-column prop="monthlyRent" label="月租(元)" width="100" />
                <el-table-column label="本月应缴" width="110">
                    <template #default="{ row }">
                        <span v-if="row.currentMonthRent != null" :style="{ color: row.currentMonthRent < row.monthlyRent ? '#E6A23C' : '#1a3a5c', fontWeight: 500 }">
                            {{ row.currentMonthRent }}元
                        </span>
                        <span v-else style="color:#c0c4cc;">--</span>
                    </template>
                </el-table-column>
                <el-table-column label="押金" width="120">
                    <template #default="{ row }">
                        <span>{{ row.depositAmount || 0 }}元</span>
                        <el-tag v-if="row.depositStatus === 1" type="success" size="small" style="margin-left:4px;">已付</el-tag>
                        <el-tag v-else-if="row.depositStatus === 2" type="info" size="small" style="margin-left:4px;">已退</el-tag>
                        <el-tag v-else-if="row.status >= 6" type="danger" size="small" style="margin-left:4px;">未付</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="startDate" label="起租日" width="120" />
                <el-table-column prop="endDate" label="到期日" width="120" />
                <el-table-column label="签约时长" width="120">
                    <template #default="{ row }">{{ calcDuration(row.startDate, row.endDate) }}</template>
                </el-table-column>
                <el-table-column label="状态" width="120">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="280" fixed="right">
                    <template #default="{ row }">
                        <!-- 待签约：房东可确认、拒绝或取消 -->
                        <el-button v-if="row.status === 0" type="success" link size="small" @click="handleSign(row)">确认签约</el-button>
                        <el-popconfirm v-if="row.status === 0" title="确定拒绝该签约申请？" @confirm="handleReject(row)">
                            <template #reference>
                                <el-button type="danger" link size="small">拒绝</el-button>
                            </template>
                        </el-popconfirm>
                        <el-popconfirm v-if="row.status === 0" title="确定取消该订单？" @confirm="handleCancel(row)">
                            <template #reference>
                                <el-button type="info" link size="small">取消</el-button>
                            </template>
                        </el-popconfirm>
                        <!-- 待付押金：等待租客缴纳 -->
                        <span v-if="row.status === 6" style="color:#E6A23C;font-size:12px;">等待租客付押金</span>
                        <!-- 履行中：可退租 -->
                        <template v-if="row.status === 1">
                            <el-popconfirm title="确定申请退租？" @confirm="handleQuit(row)">
                                <template #reference>
                                    <el-button type="warning" link size="small">退租</el-button>
                                </template>
                            </el-popconfirm>
                        </template>
                        <!-- 续租申请中：房东可确认续租 -->
                        <el-button v-if="row.status === 3" type="success" link size="small" @click="handleRenewConfirm(row)">确认续租</el-button>
                        <!-- 申请退租中：对方申请的可确认，自己申请的可撤回 -->
                        <template v-if="row.status === 4">
                            <el-popconfirm v-if="row.quitApplicant !== userStore.userInfo.id" title="确认同意退租？" @confirm="handleQuitConfirm(row)">
                                <template #reference>
                                    <el-button type="danger" link size="small">确认退租</el-button>
                                </template>
                            </el-popconfirm>
                            <el-popconfirm v-else title="确定撤回退租申请？" @confirm="handleQuitCancel(row)">
                                <template #reference>
                                    <el-button type="info" link size="small">撤回退租</el-button>
                                </template>
                            </el-popconfirm>
                        </template>
                        <!-- 已退租且押金已付未退：可退押金 -->
                        <el-popconfirm v-if="row.status === 5 && row.depositStatus === 1" title="确认退还押金？" @confirm="handleRefundDeposit(row)">
                            <template #reference>
                                <el-button type="primary" link size="small">退押金</el-button>
                            </template>
                        </el-popconfirm>
                        <el-button type="primary" link size="small" @click="contactTenant(row)">联系租客</el-button>
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

    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyOrders, getOrderHistory, signOrder, rejectOrder, renewConfirm, quitApply, quitConfirm, quitCancel, refundDeposit, cancelOrder } from '@/api/order'
import { createConversation } from '@/api/chat'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('current')
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const calcDuration = (start, end) => {
    if (!start || !end) return '--'
    const s = new Date(start), e = new Date(end)
    if (e <= s) return '--'
    let years = e.getFullYear() - s.getFullYear()
    let months = e.getMonth() - s.getMonth()
    let days = e.getDate() - s.getDate()
    if (days < 0) {
        months--
        const prevMonth = new Date(e.getFullYear(), e.getMonth(), 0)
        days += prevMonth.getDate()
    }
    if (months < 0) { years--; months += 12 }
    const parts = []
    if (years > 0) parts.push(years + '年')
    if (months > 0) parts.push(months + '个月')
    if (days > 0) parts.push(days + '天')
    return parts.length > 0 ? parts.join('') : '--'
}

const statusText = (s) => ({ 0: '待签约', 1: '履行中', 2: '已到期', 3: '续租申请中', 4: '申请退租中', 5: '已退租', 6: '待付押金', 7: '已拒绝', 8: '已取消' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: '', 4: 'danger', 5: 'info', 6: 'warning', 7: 'danger', 8: 'info' }[s] || 'info')

const goHouse = (houseId) => {
    if (houseId) router.push({ path: `/landlord/house/${houseId}` })
}

const loadOrders = async () => {
    loading.value = true
    try {
        const fn = activeTab.value === 'current' ? getMyOrders : getOrderHistory
        const res = await fn(userStore.userInfo.id, currentPage.value, pageSize, 1)
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
        await signOrder(row.id, userStore.userInfo.id)
        ElMessage.success({ message: '签约成功，等待租客缴纳押金', duration: 2000 })
        loadOrders()
    } catch (e) { console.log('签约失败', e) }
}

const contactTenant = async (row) => {
    try {
        const res = await createConversation(userStore.userInfo.id, row.tenantId)
        router.push({ path: '/landlord/chat', query: { conversationId: res.data.id } })
    } catch (e) { console.log('创建会话失败', e) }
}

const handleReject = async (row) => {
    try {
        await rejectOrder(row.id, userStore.userInfo.id)
        ElMessage.success({ message: '已拒绝签约', duration: 2000 })
        loadOrders()
    } catch (e) { console.log('拒绝失败', e) }
}

const handleRefundDeposit = async (row) => {
    try {
        await refundDeposit(row.id, userStore.userInfo.id)
        ElMessage.success({ message: '押金已退还', duration: 2000 })
        loadOrders()
    } catch (e) { console.log('退押金失败', e) }
}

const handleRenewConfirm = async (row) => {
    try {
        await renewConfirm(row.id, userStore.userInfo.id)
        ElMessage.success({ message: '已确认续租', duration: 2000 })
        loadOrders()
    } catch (e) { console.log('确认续租失败', e) }
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

const handleQuitCancel = async (row) => {
    try {
        await quitCancel(row.id, userStore.userInfo.id)
        ElMessage.success({ message: '退租申请已撤回', duration: 2000 })
        loadOrders()
    } catch (e) { console.log('撤回失败', e) }
}

const handleCancel = async (row) => {
    try {
        await cancelOrder(row.id, userStore.userInfo.id)
        ElMessage.success({ message: '订单已取消', duration: 2000 })
        loadOrders()
    } catch (e) { console.log('取消订单失败', e) }
}

onMounted(() => loadOrders())
</script>

<style scoped>
.blue-card { border-radius: 16px; border: 1px solid #e8f0f8; }
.section-title { font-size: 16px; font-weight: 600; color: #1a3a5c; }
:deep(.el-tabs__active-bar) { background: #5b9bd5; }
:deep(.el-tabs__item.is-active) { color: #1a3a5c; }
:deep(.el-pagination.is-background .el-pager li.is-active) { background: #5b9bd5 !important; }
.house-cell { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.house-cell:hover .house-title { color: #1a3a5c; text-decoration: underline; }
.house-thumb { width: 56px; height: 42px; object-fit: cover; border-radius: 4px; flex-shrink: 0; }
.house-thumb-empty { display: flex; align-items: center; justify-content: center; background: #f0f0f0; color: #bbb; font-size: 12px; }
.house-title { font-size: 13px; color: #333; line-height: 1.3; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
</style>
