<template>
    <div>
        <el-card>
            <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>用户管理</span>
                    <el-input v-model="keyword" placeholder="搜索账号/昵称/手机" clearable style="width: 240px;" @keyup.enter="handleSearch">
                        <template #append><el-button @click="handleSearch">搜索</el-button></template>
                    </el-input>
                </div>
            </template>

            <el-table :data="users" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column prop="account" label="账号" width="120" />
                <el-table-column prop="nickname" label="昵称" width="120" />
                <el-table-column prop="phone" label="手机号" width="130" />
                <el-table-column prop="email" label="邮箱" show-overflow-tooltip min-width="160" />
                <el-table-column label="角色" width="80">
                    <template #default="{ row }">{{ row.currentRole === 1 ? '房东' : '租客' }}</template>
                </el-table-column>
                <el-table-column label="状态" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'danger' : 'success'" size="small">
                            {{ row.status === 1 ? '禁用' : '正常' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="注册时间" width="160" />
                <el-table-column label="操作" width="160" fixed="right">
                    <template #default="{ row }">
                        <el-button v-if="row.status !== 1" type="warning" link size="small" @click="handleDisable(row.id)">禁用</el-button>
                        <el-button v-else type="success" link size="small" @click="handleEnable(row.id)">启用</el-button>
                        <el-popconfirm title="确定删除该用户？" @confirm="handleDelete(row.id)">
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
                @current-change="loadUsers"
            />
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, disableUser, enableUser, deleteUser } from '@/api/admin'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const keyword = ref('')

const loadUsers = async () => {
    loading.value = true
    try {
        const res = await getUserList(currentPage.value, pageSize, keyword.value)
        users.value = res.data?.records || []
        total.value = res.data?.total || 0
    } catch (e) { console.log('加载用户失败', e) }
    finally { loading.value = false }
}
const handleSearch = () => { currentPage.value = 1; loadUsers() }

const handleDisable = async (id) => {
    try { await disableUser(id); ElMessage.success({ message: '已禁用', duration: 2000 }); loadUsers() }
    catch (e) { console.log('操作失败', e) }
}
const handleEnable = async (id) => {
    try { await enableUser(id); ElMessage.success({ message: '已启用', duration: 2000 }); loadUsers() }
    catch (e) { console.log('操作失败', e) }
}
const handleDelete = async (id) => {
    try { await deleteUser(id); ElMessage.success({ message: '已删除', duration: 2000 }); loadUsers() }
    catch (e) { console.log('删除失败', e) }
}

onMounted(() => loadUsers())
</script>
