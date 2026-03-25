<template>
    <div>
        <el-card>
            <template #header>举报管理</template>

            <el-table :data="reports" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column prop="reporterId" label="举报人ID" width="90" />
                <el-table-column label="目标类型" width="90">
                    <template #default="{ row }">
                        {{ { 0: '房源', 1: '用户', 2: '评论' }[row.targetType] || '其他' }}
                    </template>
                </el-table-column>
                <el-table-column prop="targetId" label="目标ID" width="80" />
                <el-table-column prop="reason" label="举报原因" show-overflow-tooltip min-width="200" />
                <el-table-column prop="createTime" label="举报时间" width="160" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button type="success" link size="small" @click="openHandle(row.id, 1)">处理</el-button>
                        <el-button type="warning" link size="small" @click="openHandle(row.id, 2)">驳回</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <el-pagination
                v-if="total > 0"
                style="margin-top: 16px; justify-content: flex-end;"
                background layout="total, prev, pager, next"
                :total="total" :page-size="pageSize"
                v-model:current-page="currentPage"
                @current-change="loadList"
            />
        </el-card>

        <!-- 处理弹窗 -->
        <el-dialog title="处理举报" v-model="handleVisible" width="400px">
            <el-input v-model="handleResult" type="textarea" :rows="3" placeholder="请输入处理结果说明" />
            <template #footer>
                <el-button @click="handleVisible = false">取消</el-button>
                <el-button type="primary" @click="doHandle">确认</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAdminStore } from '@/stores/user'
import { getPendingReports, handleReport } from '@/api/admin'
import { ElMessage } from 'element-plus'

const adminStore = useAdminStore()
const loading = ref(false)
const reports = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const handleVisible = ref(false)
const handleResult = ref('')
const handleId = ref(null)
const handleStatus = ref(1)

const loadList = async () => {
    loading.value = true
    try {
        const res = await getPendingReports(currentPage.value, pageSize)
        reports.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载失败', e) }
    finally { loading.value = false }
}

const openHandle = (id, status) => {
    handleId.value = id
    handleStatus.value = status
    handleResult.value = ''
    handleVisible.value = true
}

const doHandle = async () => {
    try {
        await handleReport(handleId.value, adminStore.adminInfo.id, handleStatus.value, handleResult.value)
        ElMessage.success({ message: '处理完成', duration: 2000 })
        handleVisible.value = false
        loadList()
    } catch (e) { console.log('处理失败', e) }
}

onMounted(() => loadList())
</script>
