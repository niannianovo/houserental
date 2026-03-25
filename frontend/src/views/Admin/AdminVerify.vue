<template>
    <div>
        <el-card>
            <template #header>房源审核</template>

            <el-table :data="houses" style="width: 100%" v-loading="loading">
                <el-table-column label="图片" width="100">
                    <template #default="{ row }">
                        <el-image v-if="row.images" :src="row.images.split(',')[0]" style="width: 60px; height: 45px; border-radius: 4px;" fit="cover" :preview-src-list="row.images.split(',')" />
                        <span v-else style="color: #c0c4cc; font-size: 12px;">无图</span>
                    </template>
                </el-table-column>
                <el-table-column prop="title" label="房源名称" show-overflow-tooltip min-width="160" />
                <el-table-column prop="address" label="地址" show-overflow-tooltip min-width="140" />
                <el-table-column label="户型" width="90">
                    <template #default="{ row }">{{ row.roomCount }}室{{ row.hallCount }}厅</template>
                </el-table-column>
                <el-table-column prop="price" label="月租(元)" width="90" />
                <el-table-column prop="ownerId" label="房东ID" width="80" />
                <el-table-column prop="createTime" label="提交时间" width="160" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button type="success" link size="small" @click="handleAudit(row.id, 1)">通过</el-button>
                        <el-button type="danger" link size="small" @click="openReject(row.id)">驳回</el-button>
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

        <!-- 驳回原因弹窗 -->
        <el-dialog title="驳回原因" v-model="rejectVisible" width="400px">
            <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因" />
            <template #footer>
                <el-button @click="rejectVisible = false">取消</el-button>
                <el-button type="danger" @click="handleReject">确认驳回</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAdminStore } from '@/stores/user'
import { getPendingHouses, auditHouse } from '@/api/admin'
import { ElMessage } from 'element-plus'

const adminStore = useAdminStore()
const loading = ref(false)
const houses = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectHouseId = ref(null)

const loadList = async () => {
    loading.value = true
    try {
        const res = await getPendingHouses(currentPage.value, pageSize)
        houses.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载失败', e) }
    finally { loading.value = false }
}

const handleAudit = async (houseId, action) => {
    try {
        await auditHouse(houseId, adminStore.adminInfo.id, action)
        ElMessage.success({ message: '审核通过', duration: 2000 })
        loadList()
    } catch (e) { console.log('审核失败', e) }
}

const openReject = (id) => {
    rejectHouseId.value = id
    rejectReason.value = ''
    rejectVisible.value = true
}

const handleReject = async () => {
    try {
        await auditHouse(rejectHouseId.value, adminStore.adminInfo.id, 2, rejectReason.value)
        ElMessage.success({ message: '已驳回', duration: 2000 })
        rejectVisible.value = false
        loadList()
    } catch (e) { console.log('驳回失败', e) }
}

onMounted(() => loadList())
</script>
