<template>
    <div>
        <el-card>
            <template #header>房源审核</template>

            <el-table :data="houses" style="width: 100%" v-loading="loading">
                <el-table-column label="图片" width="100">
                    <template #default="{ row }">
                        <el-image v-if="row.images" :src="row.images.split(',')[0]" style="width: 60px; height: 45px; border-radius: 4px;" fit="cover" :preview-src-list="row.images.split(',')" :z-index="3000" preview-teleported @click.stop />
                        <span v-else style="color: #c0c4cc; font-size: 12px;">无图</span>
                    </template>
                </el-table-column>
                <el-table-column prop="title" label="房源名称" show-overflow-tooltip min-width="160" />
                <el-table-column prop="address" label="地址" show-overflow-tooltip min-width="140" />
                <el-table-column label="户型" width="90">
                    <template #default="{ row }">{{ row.roomCount }}室{{ row.hallCount }}厅</template>
                </el-table-column>
                <el-table-column prop="price" label="月租(元)" width="100" />
                <el-table-column prop="ownerId" label="房东ID" width="80" />
                <el-table-column label="相似房源" min-width="200">
                    <template #default="{ row }">
                        <div v-if="row.similarHouseId" class="similar-warn">
                            <el-tag type="danger" size="small">疑似重复</el-tag>
                            <span class="similar-info">
                                与房源【{{ getSimilarTitle(row.similarHouseId) }}】(ID:{{ row.similarHouseId }})图片相似
                            </span>
                            <el-button type="primary" link size="small" @click="showCompare(row)">对比查看</el-button>
                        </div>
                        <el-tag v-else type="success" size="small">图片无重复</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="提交时间" width="160" />
                <el-table-column label="操作" width="160" fixed="right">
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

        <!-- 房源对比弹窗 -->
        <el-dialog title="房源对比" v-model="compareVisible" width="1000px" top="5vh">
            <el-row :gutter="24">
                <el-col :span="12">
                    <div class="compare-section">
                        <div class="compare-label">待审核房源</div>
                        <el-descriptions :column="1" border size="small">
                            <el-descriptions-item label="标题">{{ compareData.current?.title }}</el-descriptions-item>
                            <el-descriptions-item label="区域">{{ [compareData.current?.province, compareData.current?.city, compareData.current?.district].filter(Boolean).join(' ') }}</el-descriptions-item>
                            <el-descriptions-item label="地址">{{ compareData.current?.address }}</el-descriptions-item>
                            <el-descriptions-item label="户型">{{ compareData.current?.roomCount }}室{{ compareData.current?.hallCount }}厅</el-descriptions-item>
                            <el-descriptions-item label="月租">{{ compareData.current?.price }} 元</el-descriptions-item>
                            <el-descriptions-item label="面积">{{ compareData.current?.area }} m²</el-descriptions-item>
                            <el-descriptions-item label="房东ID">{{ compareData.current?.ownerId }}</el-descriptions-item>
                        </el-descriptions>
                        <div class="compare-img-title">图片</div>
                        <div class="compare-images">
                            <el-image v-for="(img, idx) in (compareData.current?.images || '').split(',').filter(Boolean)" :key="'c'+idx"
                                :src="img" fit="cover" class="compare-img"
                                :preview-src-list="(compareData.current?.images || '').split(',').filter(Boolean)" />
                        </div>
                    </div>
                </el-col>
                <el-col :span="12">
                    <div class="compare-section">
                        <div class="compare-label">相似房源 (ID:{{ compareData.similar?.id }})</div>
                        <el-descriptions :column="1" border size="small">
                            <el-descriptions-item label="标题">{{ compareData.similar?.title }}</el-descriptions-item>
                            <el-descriptions-item label="区域">{{ [compareData.similar?.province, compareData.similar?.city, compareData.similar?.district].filter(Boolean).join(' ') }}</el-descriptions-item>
                            <el-descriptions-item label="地址">{{ compareData.similar?.address }}</el-descriptions-item>
                            <el-descriptions-item label="户型">{{ compareData.similar?.roomCount }}室{{ compareData.similar?.hallCount }}厅</el-descriptions-item>
                            <el-descriptions-item label="月租">{{ compareData.similar?.price }} 元</el-descriptions-item>
                            <el-descriptions-item label="面积">{{ compareData.similar?.area }} m²</el-descriptions-item>
                            <el-descriptions-item label="房东ID">{{ compareData.similar?.ownerId }}</el-descriptions-item>
                        </el-descriptions>
                        <div class="compare-img-title">图片</div>
                        <div class="compare-images">
                            <el-image v-for="(img, idx) in (compareData.similar?.images || '').split(',').filter(Boolean)" :key="'s'+idx"
                                :src="img" fit="cover" class="compare-img"
                                :preview-src-list="(compareData.similar?.images || '').split(',').filter(Boolean)" />
                        </div>
                    </div>
                </el-col>
            </el-row>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAdminStore } from '@/stores/user'
import { getPendingHouses, auditHouse } from '@/api/admin'
import { ElMessage } from 'element-plus'
import httpInstance from '@/utils/http'

const adminStore = useAdminStore()
const loading = ref(false)
const houses = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectHouseId = ref(null)

// 相似房源信息缓存
const similarHouseMap = ref({})
const compareVisible = ref(false)
const compareData = ref({ current: null, similar: null })

const loadList = async () => {
    loading.value = true
    try {
        const res = await getPendingHouses(currentPage.value, pageSize)
        houses.value = res.data?.records || []
        total.value = res.data?.total || 0
        // 加载相似房源信息
        for (const h of houses.value) {
            if (h.similarHouseId && !similarHouseMap.value[h.similarHouseId]) {
                loadSimilarHouse(h.similarHouseId)
            }
        }
    } catch (e) { console.log('加载失败', e) }
    finally { loading.value = false }
}

const loadSimilarHouse = async (houseId) => {
    try {
        const res = await httpInstance({ url: `/house/${houseId}`, method: 'get' })
        similarHouseMap.value[houseId] = res.data
    } catch (e) { /* ignore */ }
}

const getSimilarTitle = (houseId) => {
    return similarHouseMap.value[houseId]?.title || '加载中...'
}

const showCompare = (row) => {
    compareData.value = {
        current: row,
        similar: similarHouseMap.value[row.similarHouseId] || null
    }
    compareVisible.value = true
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

<style scoped>
.similar-warn { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.similar-info { font-size: 12px; color: #f56c6c; }
.compare-section { padding: 8px; }
.compare-label { font-size: 15px; font-weight: 600; margin-bottom: 12px; color: #303133; }
.compare-img-title { font-size: 13px; font-weight: 500; margin: 12px 0 8px; color: #606266; }
.compare-images { display: flex; flex-wrap: wrap; gap: 8px; }
.compare-img { width: 110px; height: 80px; border-radius: 4px; border: 1px solid #eee; }
</style>
