<template>
    <div>
        <el-card class="green-card">
            <template #header><span class="section-title">预约记录</span></template>

            <el-table :data="appointments" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column label="房源" min-width="220" show-overflow-tooltip>
                    <template #default="{ row }">
                        <template v-if="row.houseStatus === 1">
                            <el-link type="primary" :underline="false" @click="$router.push(`/tenant/house/${row.houseId}`)">
                                {{ row.houseTitle || `房源#${row.houseId}` }}
                            </el-link>
                        </template>
                        <template v-else>
                            <span class="offline-title">{{ row.houseTitle || `房源#${row.houseId}` }}</span>
                            <el-tag size="small" type="info" style="margin-left: 6px;">已下架</el-tag>
                        </template>
                    </template>
                </el-table-column>
                <el-table-column prop="appointmentTime" label="预约时间" min-width="160" />
                <el-table-column prop="remark" label="备注" show-overflow-tooltip min-width="160" />
                <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                        <el-popconfirm v-if="row.status === 0" title="确定取消该预约？" @confirm="handleCancel(row.id)">
                            <template #reference>
                                <el-button type="danger" link size="small">取消预约</el-button>
                            </template>
                        </el-popconfirm>
                        <el-button v-else-if="row.status === 1" type="primary" link size="small" @click="$router.push(`/tenant/house/${row.houseId}`)">查看房源</el-button>
                        <span v-else style="color: #b8d4ba; font-size: 12px;">--</span>
                    </template>
                </el-table-column>
            </el-table>

            <el-pagination
                v-if="total > 0"
                style="margin-top: 16px; justify-content: flex-end;"
                background layout="total, prev, pager, next"
                :total="total" :page-size="pageSize"
                v-model:current-page="currentPage"
                @current-change="loadAppointments"
            />
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyAppointments, updateAppointmentStatus } from '@/api/appointment'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const appointments = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const statusText = (s) => ({ 0: '待处理', 1: '已同意', 2: '已拒绝', 3: '已完成', 4: '已取消' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: '', 4: 'info' }[s] || 'info')

const loadAppointments = async () => {
    loading.value = true
    try { const res = await getMyAppointments(userStore.userInfo.id, currentPage.value, pageSize); appointments.value = res.data?.records || []; total.value = res.data?.total || 0 }
    catch (e) { console.log('加载预约失败', e) } finally { loading.value = false }
}
const handleCancel = async (id) => {
    try { await updateAppointmentStatus(id, 4); ElMessage.success({ message: '已取消预约', duration: 2000 }); loadAppointments() }
    catch (e) { console.log('取消失败', e) }
}
onMounted(() => loadAppointments())
</script>

<style scoped>
.green-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.offline-title { color: #909399; text-decoration: line-through; }
:deep(.el-pagination.is-background .el-pager li.is-active) { background: #66bb6a !important; }
</style>
