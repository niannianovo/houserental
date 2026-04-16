<template>
    <div>
        <el-card>
            <template #header>房源管理</template>

            <div style="display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap;">
                <el-input v-model="idFilter" placeholder="房源ID" clearable style="width: 100px;" @keyup.enter="handleSearch" />
                <el-input v-model="keyword" placeholder="搜索标题/地址" clearable style="width: 200px;" @keyup.enter="handleSearch" />
                <el-select v-model="statusFilter" placeholder="房源状态" clearable style="width: 120px;" @change="handleSearch">
                    <el-option label="待审核" :value="0" />
                    <el-option label="已上架" :value="1" />
                    <el-option label="已下架" :value="2" />
                    <el-option label="已出租" :value="3" />
                </el-select>
                <el-select v-model="verifyFilter" placeholder="审核状态" clearable style="width: 120px;" @change="handleSearch">
                    <el-option label="未审核" :value="0" />
                    <el-option label="已通过" :value="1" />
                    <el-option label="已驳回" :value="2" />
                </el-select>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="idFilter = ''; keyword = ''; statusFilter = null; verifyFilter = null; handleSearch()">重置</el-button>
            </div>

            <el-table :data="houses" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column label="图片" width="80">
                    <template #default="{ row }">
                        <el-image v-if="row.images" :src="row.images.split(',')[0]" style="width: 50px; height: 40px; border-radius: 4px;" fit="cover" :preview-src-list="row.images.split(',')" :z-index="3000" preview-teleported @click.stop />
                        <span v-else style="color: #c0c4cc; font-size: 12px;">无图</span>
                    </template>
                </el-table-column>
                <el-table-column prop="title" label="标题" show-overflow-tooltip min-width="160" />
                <el-table-column label="区域" show-overflow-tooltip min-width="120">
                    <template #default="{ row }">{{ [row.province, row.city, row.district].filter(Boolean).join(' ') }}</template>
                </el-table-column>
                <el-table-column label="户型" width="80">
                    <template #default="{ row }">{{ row.roomCount }}室{{ row.hallCount }}厅</template>
                </el-table-column>
                <el-table-column prop="price" label="月租" width="90" />
                <el-table-column prop="ownerId" label="房东ID" width="70" />
                <el-table-column label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="审核" width="80">
                    <template #default="{ row }">
                        <el-tag :type="verifyType(row.verifyStatus)" size="small">{{ verifyText(row.verifyStatus) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="viewCount" label="浏览" width="60" />
                <el-table-column prop="favoriteCount" label="收藏" width="60" />
                <el-table-column prop="createTime" label="创建时间" width="160" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
                        <el-button v-if="row.status === 1 || row.status === 3" type="warning" link size="small" @click="handleTakedown(row)">下架</el-button>
                        <el-button v-if="row.status !== 3" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

        <!-- 详情弹窗 -->
        <el-dialog title="房源详情" v-model="detailVisible" width="700px">
            <template v-if="detailHouse">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="ID">{{ detailHouse.id }}</el-descriptions-item>
                    <el-descriptions-item label="房东ID">{{ detailHouse.ownerId }}</el-descriptions-item>
                    <el-descriptions-item label="标题" :span="2">{{ detailHouse.title }}</el-descriptions-item>
                    <el-descriptions-item label="区域" :span="2">{{ [detailHouse.province, detailHouse.city, detailHouse.district, detailHouse.address].filter(Boolean).join(' ') }}</el-descriptions-item>
                    <el-descriptions-item label="户型">{{ detailHouse.roomCount }}室{{ detailHouse.hallCount }}厅</el-descriptions-item>
                    <el-descriptions-item label="面积">{{ detailHouse.area }} m²</el-descriptions-item>
                    <el-descriptions-item label="月租">{{ detailHouse.price }} 元</el-descriptions-item>
                    <el-descriptions-item label="押金">{{ detailHouse.deposit }} 元</el-descriptions-item>
                    <el-descriptions-item label="楼层">{{ detailHouse.floor }}</el-descriptions-item>
                    <el-descriptions-item label="类型">{{ { 0: '整租', 1: '合租', 2: '公寓' }[detailHouse.houseType] || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="联系人">{{ detailHouse.contactName }}</el-descriptions-item>
                    <el-descriptions-item label="电话">{{ detailHouse.contactPhone }}</el-descriptions-item>
                    <el-descriptions-item label="状态">
                        <el-tag :type="statusType(detailHouse.status)" size="small">{{ statusText(detailHouse.status) }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="审核">
                        <el-tag :type="verifyType(detailHouse.verifyStatus)" size="small">{{ verifyText(detailHouse.verifyStatus) }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="描述" :span="2">{{ detailHouse.description || '-' }}</el-descriptions-item>
                </el-descriptions>
                <div v-if="detailHouse.images" style="margin-top: 16px;">
                    <div style="font-weight: 500; margin-bottom: 8px;">房源图片</div>
                    <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                        <el-image v-for="(img, idx) in detailHouse.images.split(',').filter(Boolean)" :key="idx"
                            :src="img" fit="cover" style="width: 120px; height: 90px; border-radius: 4px;"
                            :preview-src-list="detailHouse.images.split(',').filter(Boolean)" />
                    </div>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { searchHouses, adminTakedownHouse, adminDeleteHouse } from '@/api/house'

const loading = ref(false)
const houses = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const idFilter = ref('')
const keyword = ref('')
const statusFilter = ref(null)
const verifyFilter = ref(null)
const detailVisible = ref(false)
const detailHouse = ref(null)

const statusText = (s) => ({ 0: '待审核', 1: '已上架', 2: '已下架', 3: '已出租' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'primary' }[s] || 'info')
const verifyText = (s) => ({ 0: '未审核', 1: '已通过', 2: '已驳回' }[s] || '未知')
const verifyType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')

const loadList = async () => {
    loading.value = true
    try {
        const params = { page: currentPage.value, size: pageSize, status: statusFilter.value !== null ? statusFilter.value : -1 }
        if (idFilter.value) params.id = idFilter.value
        if (keyword.value) params.keyword = keyword.value
        if (verifyFilter.value !== null) params.verifyStatus = verifyFilter.value
        const res = await searchHouses(params)
        houses.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载失败', e) }
    finally { loading.value = false }
}

const handleSearch = () => {
    currentPage.value = 1
    loadList()
}

const showDetail = (row) => {
    detailHouse.value = row
    detailVisible.value = true
}

const handleTakedown = async (row) => {
    try {
        const { value: reason } = await ElMessageBox.prompt('请输入下架原因（可选）', '强制下架房源', {
            confirmButtonText: '确认下架',
            cancelButtonText: '取消',
            inputPlaceholder: '如：房源信息不实、被举报等',
            type: 'warning'
        })
        await adminTakedownHouse(row.id, reason || '')
        ElMessage.success('已下架')
        loadList()
    } catch (e) {
        if (e !== 'cancel' && e?.message !== 'cancel') {
            ElMessage.error(e?.response?.data?.message || '操作失败')
        }
    }
}

const handleDelete = async (row) => {
    try {
        const { value: reason } = await ElMessageBox.prompt('请输入删除原因（可选）', '删除房源', {
            confirmButtonText: '确认删除',
            cancelButtonText: '取消',
            inputPlaceholder: '如：违规房源、虚假信息等',
            type: 'warning'
        })
        await adminDeleteHouse(row.id, reason || '')
        ElMessage.success('已删除')
        loadList()
    } catch (e) {
        if (e !== 'cancel' && e?.message !== 'cancel') {
            ElMessage.error(e?.response?.data?.message || '操作失败')
        }
    }
}

onMounted(() => loadList())
</script>
