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
                <el-table-column label="房源" min-width="220">
                    <template #default="{ row }">
                        <div class="house-cell" @click="goHouse(row.houseId)">
                            <img v-if="row.houseImages" :src="row.houseImages.split(',')[0]" class="house-thumb" />
                            <div v-else class="house-thumb house-thumb-empty">无图</div>
                            <span class="house-title">{{ row.houseTitle || ('房源#' + row.houseId) }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="monthlyRent" label="月租(元)" width="100" />
                <el-table-column label="本月应缴" width="110">
                    <template #default="{ row }">
                        <span v-if="row.currentMonthRent != null" :style="{ color: row.currentMonthRent < row.monthlyRent ? '#E6A23C' : '#2e5e3a', fontWeight: 500 }">
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
                <el-table-column label="操作" width="260" fixed="right">
                    <template #default="{ row }">
                        <!-- 待签约：租客可取消 -->
                        <el-popconfirm v-if="row.status === 0" title="确定取消该签约订单？" @confirm="handleCancel(row)">
                            <template #reference>
                                <el-button type="danger" link size="small">取消订单</el-button>
                            </template>
                        </el-popconfirm>
                        <!-- 待付押金：租客付押金 -->
                        <el-popconfirm v-if="row.status === 6" :title="'确认缴纳押金 ' + (row.depositAmount || 0) + ' 元？'" @confirm="handlePayDeposit(row)">
                            <template #reference>
                                <el-button type="danger" link size="small">缴纳押金</el-button>
                            </template>
                        </el-popconfirm>
                        <template v-if="row.status === 1">
                            <el-button type="primary" link size="small" @click="openPay(row)">去缴费</el-button>
                            <el-button type="success" link size="small" @click="openRenew(row)">申请续租</el-button>
                            <el-popconfirm title="确定申请退租？" @confirm="handleQuit(row)">
                                <template #reference>
                                    <el-button type="warning" link size="small">申请退租</el-button>
                                </template>
                            </el-popconfirm>
                        </template>
                        <span v-if="row.status === 3" style="color:#409EFF;font-size:12px;">续租审批中</span>
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
                        <el-button type="primary" link size="small" @click="contactOwner(row)">联系房东</el-button>
                        <el-button type="primary" link size="small" @click="$router.push(`/tenant/user/${row.ownerId}`)">房东主页</el-button>
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

        <!-- 续租弹窗 -->
        <el-dialog title="申请续租" v-model="renewVisible" width="400px">
            <el-form label-width="80px">
                <el-form-item label="新到期日">
                    <el-date-picker v-model="renewDate" type="date" value-format="YYYY-MM-DD" placeholder="选择新到期日" style="width: 100%;" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="renewVisible = false">取消</el-button>
                <el-button type="primary" class="green-btn" @click="handleRenew" :loading="submitting">提交申请</el-button>
            </template>
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
import { getMyOrders, getOrderHistory, getPaymentList, quitApply, quitConfirm, quitCancel, renewApply, payDeposit, cancelOrder } from '@/api/order'
import { createConversation } from '@/api/chat'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import httpInstance from '@/utils/http'

const router = useRouter()
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

const submitting = ref(false)
const renewVisible = ref(false)
const renewDate = ref('')
const renewOrderId = ref(null)

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
    if (houseId) router.push({ path: `/tenant/house/${houseId}` })
}

const loadOrders = async () => {
    loading.value = true
    try {
        const fn = activeTab.value === 'current' ? getMyOrders : getOrderHistory
        const res = await fn(userStore.userInfo.id, currentPage.value, pageSize, 0)
        orders.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载失败', e) } finally { loading.value = false }
}
const handleTabChange = () => { currentPage.value = 1; loadOrders() }

const handlePayDeposit = async (row) => {
    try {
        await payDeposit(row.id, userStore.userInfo.id)
        ElMessage.success({ message: '押金缴纳成功，租约正式生效', duration: 2000 })
        loadOrders()
    } catch (e) { console.log('缴纳押金失败', e) }
}

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
const openRenew = (row) => { renewOrderId.value = row.id; renewDate.value = ''; renewVisible.value = true }
const handleRenew = async () => {
    if (!renewDate.value) { ElMessage.warning('请选择新到期日'); return }
    submitting.value = true
    try {
        await renewApply(renewOrderId.value, userStore.userInfo.id, renewDate.value)
        ElMessage.success({ message: '续租申请已提交，等待房东确认', duration: 2000 })
        renewVisible.value = false
        loadOrders()
    } catch (e) { console.log('续租申请失败', e) } finally { submitting.value = false }
}

const contactOwner = async (row) => {
    try {
        const res = await createConversation(userStore.userInfo.id, row.ownerId, row.houseId)
        router.push({ path: '/tenant/chat', query: { conversationId: res.data.id } })
    } catch (e) { console.log('创建会话失败', e) }
}

const handleQuit = async (row) => {
    try {
        await quitApply(row.id, userStore.userInfo.id)
        ElMessage.success({ message: '已提交退租申请', duration: 2000 })
        loadOrders()
    } catch (e) { console.log('申请失败', e) }
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
.green-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.green-btn { background: #81c784 !important; border: none !important; }
.green-btn:hover { background: #66bb6a !important; }
.green-tabs :deep(.el-tabs__active-bar) { background: #81c784; }
.green-tabs :deep(.el-tabs__item.is-active) { color: #2e7d32; }
:deep(.el-pagination.is-background .el-pager li.is-active) { background: #66bb6a !important; }
.house-cell { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.house-cell:hover .house-title { color: #2e7d32; text-decoration: underline; }
.house-thumb { width: 56px; height: 42px; object-fit: cover; border-radius: 4px; flex-shrink: 0; }
.house-thumb-empty { display: flex; align-items: center; justify-content: center; background: #f0f0f0; color: #bbb; font-size: 12px; }
.house-title { font-size: 13px; color: #333; line-height: 1.3; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
</style>
