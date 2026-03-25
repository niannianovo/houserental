<template>
    <div>
        <el-card>
            <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>公告管理</span>
                    <el-button type="primary" @click="openAdd">发布公告</el-button>
                </div>
            </template>

            <el-table :data="list" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column prop="title" label="标题" show-overflow-tooltip min-width="200" />
                <el-table-column prop="content" label="内容" show-overflow-tooltip min-width="300" />
                <el-table-column label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                            {{ row.status === 1 ? '已发布' : '已下架' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="发布时间" width="160" />
                <el-table-column label="操作" width="140" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
                        <el-popconfirm title="确定删除该公告？" @confirm="handleDelete(row.id)">
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
                @current-change="loadList"
            />
        </el-card>

        <!-- 发布/编辑弹窗 -->
        <el-dialog :title="editForm.id ? '编辑公告' : '发布公告'" v-model="dialogVisible" width="500px">
            <el-form :model="editForm" label-width="60px">
                <el-form-item label="标题">
                    <el-input v-model="editForm.title" placeholder="请输入公告标题" />
                </el-form-item>
                <el-form-item label="内容">
                    <el-input v-model="editForm.content" type="textarea" :rows="5" placeholder="请输入公告内容" />
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
import { useAdminStore } from '@/stores/user'
import { getAnnouncementList, publishAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/admin'
import { ElMessage } from 'element-plus'

const adminStore = useAdminStore()
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const editForm = ref({ title: '', content: '' })

const loadList = async () => {
    loading.value = true
    try {
        const res = await getAnnouncementList(currentPage.value, pageSize)
        list.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载失败', e) }
    finally { loading.value = false }
}

const openAdd = () => { editForm.value = { title: '', content: '' }; dialogVisible.value = true }
const openEdit = (row) => { editForm.value = { ...row }; dialogVisible.value = true }

const handleSubmit = async () => {
    if (!editForm.value.title) { ElMessage.warning('请输入标题'); return }
    submitting.value = true
    try {
        if (editForm.value.id) {
            await updateAnnouncement(editForm.value.id, editForm.value)
            ElMessage.success({ message: '更新成功', duration: 2000 })
        } else {
            await publishAnnouncement({ ...editForm.value, adminId: adminStore.adminInfo.id })
            ElMessage.success({ message: '发布成功', duration: 2000 })
        }
        dialogVisible.value = false
        loadList()
    } catch (e) { console.log('操作失败', e) }
    finally { submitting.value = false }
}

const handleDelete = async (id) => {
    try { await deleteAnnouncement(id); ElMessage.success({ message: '已删除', duration: 2000 }); loadList() }
    catch (e) { console.log('删除失败', e) }
}

onMounted(() => loadList())
</script>
