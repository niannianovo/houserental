<template>
    <div>
        <el-card>
            <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>房源管理</span>
                    <el-button type="primary" @click="openPublish">发布房源</el-button>
                </div>
            </template>

            <el-table :data="houses" style="width: 100%" v-loading="loading">
                <el-table-column label="图片" width="100">
                    <template #default="{ row }">
                        <el-image
                            v-if="getFirstImage(row.images)"
                            :src="getFirstImage(row.images)"
                            style="width: 60px; height: 45px; border-radius: 4px;"
                            fit="cover"
                            :preview-src-list="getImageList(row.images)"
                            :z-index="3000"
                            preview-teleported
                            @click.stop
                        />
                        <span v-else style="color: #c0c4cc; font-size: 12px;">无图</span>
                    </template>
                </el-table-column>
                <el-table-column prop="title" label="房源名称" show-overflow-tooltip min-width="160" />
                <el-table-column label="地址" show-overflow-tooltip min-width="140">
                    <template #default="{ row }">{{ [row.province, row.city, row.district, row.address].filter(Boolean).join(' ') }}</template>
                </el-table-column>
                <el-table-column label="户型" width="90">
                    <template #default="{ row }">{{ row.roomCount }}室{{ row.hallCount }}厅</template>
                </el-table-column>
                <el-table-column prop="area" label="面积(m²)" width="100" />
                <el-table-column prop="price" label="月租(元)" width="110" />
                <el-table-column label="审核" width="85">
                    <template #default="{ row }">
                        <el-tag :type="verifyType(row.verifyStatus)" size="small">{{ verifyText(row.verifyStatus) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="85">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="viewCount" label="浏览" width="65" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button v-if="row.status === 1" type="warning" link size="small" @click="handleOffline(row)">下架</el-button>
                        <el-button v-if="row.status !== 1 && row.status !== 3" type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
                        <el-popconfirm v-if="row.status !== 3" title="确定删除该房源？" @confirm="handleDelete(row.id)">
                            <template #reference>
                                <el-button type="danger" link size="small">删除</el-button>
                            </template>
                        </el-popconfirm>
                    </template>
                </el-table-column>
            </el-table>

            <el-pagination
                v-if="total > 0"
                style="margin-top: 16px; justify-content: flex-end;"
                background layout="total, prev, pager, next"
                :total="total" :page-size="pageSize"
                v-model:current-page="currentPage"
                @current-change="loadHouses"
            />
        </el-card>

        <!-- 发布/编辑弹窗 -->
        <el-dialog :title="editForm.id ? '编辑房源' : '发布房源'" v-model="dialogVisible" width="750px" :close-on-click-modal="false">
            <el-form :model="editForm" :rules="rules" ref="formRef" label-width="90px">
                <el-form-item label="房源名称" prop="title">
                    <el-input v-model="editForm.title" placeholder="请输入房源标题" />
                </el-form-item>
                <el-form-item label="所在区域" prop="areaValue">
                    <el-cascader
                        v-model="editForm.areaValue"
                        :options="areaData"
                        :props="{ expandTrigger: 'hover' }"
                        placeholder="请选择省/市/区"
                        clearable
                        style="width: 100%;"
                        @change="handleAreaChange"
                    />
                </el-form-item>
                <el-form-item label="详细地址" prop="address">
                    <el-input v-model="editForm.address" placeholder="请输入小区名称、门牌号等" />
                </el-form-item>
                <el-row :gutter="16">
                    <el-col :span="8">
                        <el-form-item label="几室" prop="roomCount">
                            <el-input-number v-model="editForm.roomCount" :min="1" :max="10" style="width: 100%;" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="几厅" prop="hallCount">
                            <el-input-number v-model="editForm.hallCount" :min="0" :max="5" style="width: 100%;" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="楼层" prop="floor">
                            <el-input v-model="editForm.floor" placeholder="如: 3/18" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="16">
                    <el-col :span="8">
                        <el-form-item label="面积(m²)" prop="area">
                            <el-input-number v-model="editForm.area" :min="1" controls-position="right" style="width: 100%;" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="月租(元)" prop="price">
                            <el-input-number v-model="editForm.price" :min="0" :step="100" controls-position="right" style="width: 100%;" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="押金(元)">
                            <el-input-number v-model="editForm.deposit" :min="0" :step="100" controls-position="right" style="width: 100%;" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="房屋类型" prop="houseType">
                    <el-select v-model="editForm.houseType" placeholder="请选择" style="width: 100%;">
                        <el-option label="整租" :value="0" />
                        <el-option label="合租" :value="1" />
                        <el-option label="公寓" :value="2" />
                    </el-select>
                </el-form-item>
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="联系人" prop="contactName">
                            <el-input v-model="editForm.contactName" placeholder="联系人姓名" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="联系电话" prop="contactPhone">
                            <el-input v-model="editForm.contactPhone" placeholder="联系电话" />
                        </el-form-item>
                    </el-col>
                </el-row>

                <!-- 图片上传 -->
                <el-form-item label="房源图片">
                    <div class="image-upload-area">
                        <div v-for="(img, idx) in imageFileList" :key="idx" class="image-item">
                            <el-image :src="img" fit="cover" style="width: 100px; height: 80px; border-radius: 4px;" />
                            <el-icon class="image-remove" @click="removeImage(idx)"><CircleClose /></el-icon>
                        </div>
                        <el-upload
                            v-if="imageFileList.length < 9"
                            action=""
                            :show-file-list="false"
                            :http-request="handleUpload"
                            accept=".jpg,.jpeg,.png,.gif,.webp"
                        >
                            <div class="upload-trigger">
                                <el-icon :size="24"><Plus /></el-icon>
                                <span>上传图片</span>
                            </div>
                        </el-upload>
                    </div>
                    <div style="font-size: 12px; color: #909399; margin-top: 4px;">最多上传9张，支持 jpg/png/gif/webp，单张不超过5MB</div>
                </el-form-item>

                <el-form-item label="房源描述">
                    <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="描述一下您的房源..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ editForm.id ? '保存' : '发布' }}</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyHouses, publishHouse, updateHouse, deleteHouse, uploadImage } from '@/api/house'
import { ElMessage } from 'element-plus'
import { CircleClose, Plus } from '@element-plus/icons-vue'
import areaData from '@/utils/areaData'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const houses = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const formRef = ref(null)
const imageFileList = ref([])

const defaultForm = {
    title: '', province: '', city: '', district: '', address: '', areaValue: [],
    roomCount: 1, hallCount: 1, floor: '',
    area: null, price: null, deposit: null, houseType: 0,
    contactName: '', contactPhone: '', description: '', images: ''
}
const editForm = ref({ ...defaultForm })

const handleAreaChange = (val) => {
    if (val && val.length === 3) {
        editForm.value.province = val[0]
        editForm.value.city = val[1]
        editForm.value.district = val[2]
    } else {
        editForm.value.province = ''
        editForm.value.city = ''
        editForm.value.district = ''
    }
}

const rules = {
    title: [{ required: true, message: '请输入房源名称', trigger: 'blur' }],
    areaValue: [{ required: true, message: '请选择所在区域', trigger: 'change', type: 'array' }],
    address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
    price: [{ required: true, message: '请输入月租', trigger: 'blur' }],
    area: [{ required: true, message: '请输入面积', trigger: 'blur' }],
    contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
    contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const statusText = (s) => ({ 0: '待审核', 1: '已上架', 2: '已下架', 3: '已出租' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'primary' }[s] || 'info')
const verifyText = (s) => ({ 0: '待审核', 1: '已通过', 2: '已驳回' }[s] || '未知')
const verifyType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')

const getFirstImage = (images) => images ? images.split(',')[0] : ''
const getImageList = (images) => images ? images.split(',').filter(Boolean) : []

const loadHouses = async () => {
    loading.value = true
    try {
        const res = await getMyHouses(userStore.userInfo.id, currentPage.value, pageSize)
        houses.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载房源失败', e) }
    finally { loading.value = false }
}

const openPublish = () => {
    editForm.value = { ...defaultForm }
    imageFileList.value = []
    dialogVisible.value = true
}

const openEdit = (row) => {
    editForm.value = { ...row }
    // 回填省市区级联
    if (row.province && row.city && row.district) {
        editForm.value.areaValue = [row.province, row.city, row.district]
    } else {
        editForm.value.areaValue = []
    }
    imageFileList.value = row.images ? row.images.split(',').filter(Boolean) : []
    dialogVisible.value = true
}

// 图片上传
const handleUpload = async ({ file }) => {
    // 校验
    const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    if (!validTypes.includes(file.type)) {
        ElMessage.error('仅支持 jpg/png/gif/webp 格式')
        return
    }
    if (file.size > 5 * 1024 * 1024) {
        ElMessage.error('图片不能超过5MB')
        return
    }

    try {
        const res = await uploadImage(file)
        imageFileList.value.push(res.data.url)
        ElMessage.success({ message: '上传成功', duration: 1500 })
    } catch (e) {
        console.log('上传失败', e)
    }
}

const removeImage = (idx) => {
    imageFileList.value.splice(idx, 1)
}

const handleSubmit = async () => {
    try { await formRef.value.validate() } catch { return }

    // 拼接图片
    editForm.value.images = imageFileList.value.join(',')

    submitting.value = true
    try {
        if (editForm.value.id) {
            await updateHouse(editForm.value.id, { ...editForm.value, ownerId: userStore.userInfo.id })
            ElMessage.success({ message: '房源更新成功', duration: 2000 })
        } else {
            await publishHouse({ ...editForm.value, ownerId: userStore.userInfo.id })
            ElMessage.success({ message: '房源发布成功', duration: 2000 })
        }
        dialogVisible.value = false
        loadHouses()
    } catch (e) { console.log('操作失败', e) }
    finally { submitting.value = false }
}

const handleOffline = async (row) => {
    try {
        await updateHouse(row.id, { ownerId: userStore.userInfo.id, status: 2 })
        ElMessage.success({ message: '已下架，可以编辑了', duration: 2000 })
        loadHouses()
    } catch (e) { console.log('下架失败', e) }
}

const handleDelete = async (id) => {
    try {
        await deleteHouse(id, userStore.userInfo.id)
        ElMessage.success({ message: '删除成功', duration: 2000 })
        loadHouses()
    } catch (e) { console.log('删除失败', e) }
}

onMounted(() => loadHouses())
</script>

<style scoped>
.image-upload-area {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}
.image-item {
    position: relative;
    width: 100px;
    height: 80px;
}
.image-remove {
    position: absolute;
    top: -6px;
    right: -6px;
    font-size: 18px;
    color: #F56C6C;
    cursor: pointer;
    background: #fff;
    border-radius: 50%;
}
.image-remove:hover { color: #e63e3e; }
.upload-trigger {
    width: 100px;
    height: 80px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #909399;
    font-size: 12px;
    gap: 4px;
    transition: all 0.2s;
}
.upload-trigger:hover {
    border-color: #5b9bd5;
    color: #5b9bd5;
}
:deep(.el-card) { border-radius: 16px; border: 1px solid #e8f0f8; }
:deep(.el-pagination.is-background .el-pager li.is-active) { background: #5b9bd5 !important; }
</style>
