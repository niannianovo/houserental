<template>
    <div>
        <el-row :gutter="20">
            <!-- 个人信息 -->
            <el-col :span="14">
                <el-card class="green-card">
                    <template #header><span class="section-title">个人信息</span></template>
                    <el-form :model="profileForm" label-width="80px">
                        <el-form-item label="头像">
                            <el-avatar :size="64" :src="profileForm.avatar" style="background: #81c784;">
                                {{ profileForm.nickname?.charAt(0) }}
                            </el-avatar>
                        </el-form-item>
                        <el-form-item label="账号">
                            <el-input :value="profileForm.account" disabled />
                        </el-form-item>
                        <el-form-item label="昵称">
                            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
                        </el-form-item>
                        <el-form-item label="手机号">
                            <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                        </el-form-item>
                        <el-form-item label="邮箱">
                            <div style="display: flex; align-items: center; gap: 8px;">
                                <el-input :value="profileForm.email || '未绑定'" disabled style="flex: 1;" />
                                <el-tag v-if="profileForm.isEmailVerified" type="success" size="small">已验证</el-tag>
                                <el-tag v-else type="info" size="small">未验证</el-tag>
                            </div>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" class="green-btn" @click="handleSave" :loading="saving">保存修改</el-button>
                        </el-form-item>
                    </el-form>
                </el-card>
            </el-col>

            <!-- 侧边 -->
            <el-col :span="10">
                <!-- 修改密码 -->
                <el-card class="green-card" style="margin-bottom: 20px;">
                    <template #header><span class="section-title">修改密码</span></template>
                    <el-form :model="pwdForm" label-width="80px">
                        <el-form-item label="旧密码">
                            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
                        </el-form-item>
                        <el-form-item label="新密码">
                            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" class="green-btn" @click="handleChangePwd" :loading="changingPwd">修改密码</el-button>
                        </el-form-item>
                    </el-form>
                </el-card>

                <!-- 账号信息 -->
                <el-card class="green-card">
                    <template #header><span class="section-title">账号信息</span></template>
                    <div class="info-list">
                        <div class="info-row">
                            <span class="info-label">邮箱状态</span>
                            <el-tag :type="profileForm.isEmailVerified ? 'success' : 'warning'" size="small">
                                {{ profileForm.isEmailVerified ? '已验证' : '未验证' }}
                            </el-tag>
                        </div>
                        <div class="info-row">
                            <span class="info-label">当前身份</span>
                            <el-tag size="small" style="background: #e8f5e9; color: #2e7d32; border: none;">租客</el-tag>
                        </div>
                        <div class="info-row">
                            <span class="info-label">注册时间</span>
                            <span class="info-value">{{ profileForm.createTime }}</span>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUserProfile } from '@/api/user'
import { ElMessage } from 'element-plus'
import httpInstance from '@/utils/http'

const userStore = useUserStore()
const saving = ref(false)
const changingPwd = ref(false)
const profileForm = ref({})
const pwdForm = ref({ oldPassword: '', newPassword: '' })

onMounted(async () => {
    try { const res = await getUserProfile(userStore.userInfo.id); profileForm.value = res.data || {} }
    catch (e) { console.log('加载资料失败', e) }
})

const handleSave = async () => {
    saving.value = true
    try {
        await httpInstance({ url: '/user/info', method: 'put', data: profileForm.value })
        userStore.setUserInfo({ ...userStore.userInfo, nickname: profileForm.value.nickname, phone: profileForm.value.phone })
        ElMessage.success({ message: '保存成功', duration: 2000 })
    } catch (e) { console.log('保存失败', e) } finally { saving.value = false }
}

const handleChangePwd = async () => {
    if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) { ElMessage.warning('请填写完整'); return }
    changingPwd.value = true
    try {
        await httpInstance({ url: '/user/password', method: 'put', params: { userId: userStore.userInfo.id, oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword } })
        ElMessage.success({ message: '密码修改成功', duration: 2000 })
        pwdForm.value = { oldPassword: '', newPassword: '' }
    } catch (e) { console.log('修改失败', e) } finally { changingPwd.value = false }
}
</script>

<style scoped>
.green-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.green-btn { background: #81c784 !important; border: none !important; border-radius: 10px !important; }
.green-btn:hover { background: #66bb6a !important; }

:deep(.el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px #e3f0e3 inset;
    background: #f9fcf9;
}
:deep(.el-input__wrapper:focus-within) {
    box-shadow: 0 0 0 2px #81c784 inset;
    background: #fff;
}

.info-list { display: flex; flex-direction: column; gap: 16px; }
.info-row { display: flex; justify-content: space-between; align-items: center; }
.info-label { color: #88a58b; font-size: 14px; }
.info-value { color: #2e5e3a; font-size: 13px; }
</style>
