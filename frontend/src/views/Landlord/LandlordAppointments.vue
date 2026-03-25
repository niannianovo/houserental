<template>
    <div>
        <el-card>
            <template #header>预约看房</template>

            <el-table :data="appointments" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column prop="houseId" label="房源ID" width="80" />
                <el-table-column prop="userId" label="预约人ID" width="90" />
                <el-table-column prop="appointmentTime" label="预约时间" min-width="160" />
                <el-table-column prop="remark" label="备注" show-overflow-tooltip min-width="160" />
                <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="180" fixed="right">
                    <template #default="{ row }">
                        <template v-if="row.status === 0">
                            <el-button type="success" link size="small" @click="handleStatus(row.id, 1)">同意</el-button>
                            <el-button type="danger" link size="small" @click="handleStatus(row.id, 2)">拒绝</el-button>
                        </template>
                        <el-button v-if="row.status === 1" type="primary" link size="small" @click="handleStatus(row.id, 3)">已完成</el-button>
                        <span v-if="row.status >= 2" style="color: #c0c4cc; font-size: 12px;">--</span>
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
    try {
        const res = await getMyAppointments(userStore.userInfo.id, currentPage.value, pageSize)
        appointments.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载预约失败', e) }
    finally { loading.value = false }
}

const handleStatus = async (id, status) => {
    try {
        await updateAppointmentStatus(id, status)
        ElMessage.success({ message: '操作成功', duration: 2000 })
        loadAppointments()
    } catch (e) { console.log('操作失败', e) }
}

onMounted(() => loadAppointments())
</script>
