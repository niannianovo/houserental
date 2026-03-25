<template>
    <div>
        <el-row :gutter="20">
            <!-- 个人信息 -->
            <el-col :span="14">
                <el-card>
                    <template #header>个人信息</template>
                    <el-form :model="profileForm" label-width="80px" ref="formRef">
                        <el-form-item label="头像">
                            <el-avatar :size="64" :src="profileForm.avatar">
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
                        <el-form-item label="真实姓名">
                            <span v-if="profileForm.realName">{{ profileForm.realName }}</span>
                            <span v-else style="color: #c0c4cc;">未实名认证</span>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
                        </el-form-item>
                    </el-form>
                </el-card>
            </el-col>

            <!-- 侧边信息 -->
            <el-col :span="10">
                <!-- 修改密码 -->
                <el-card style="margin-bottom: 20px;">
                    <template #header>修改密码</template>
                    <el-form :model="pwdForm" label-width="80px" ref="pwdFormRef">
                        <el-form-item label="旧密码">
                            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
                        </el-form-item>
                        <el-form-item label="新密码">
                            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="handleChangePwd" :loading="changingPwd">修改密码</el-button>
                        </el-form-item>
                    </el-form>
                </el-card>

                <!-- 认证状态 -->
                <el-card>
                    <template #header>认证状态</template>
                    <div style="display: flex; flex-direction: column; gap: 16px;">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <span>身份认证</span>
                            <el-tag :type="profileForm.isVerified ? 'success' : 'warning'" size="small">
                                {{ profileForm.isVerified ? '已认证' : '未认证' }}
                            </el-tag>
                        </div>
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <span>邮箱认证</span>
                            <el-tag :type="profileForm.isEmailVerified ? 'success' : 'warning'" size="small">
                                {{ profileForm.isEmailVerified ? '已认证' : '未认证' }}
                            </el-tag>
                        </div>
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <span>当前身份</span>
                            <el-tag type="primary" size="small">房东</el-tag>
                        </div>
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <span>注册时间</span>
                            <span style="color: #909399; font-size: 13px;">{{ profileForm.createTime }}</span>
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
    try {
        const res = await getUserProfile(userStore.userInfo.id)
        profileForm.value = res.data || {}
    } catch (e) { console.log('加载资料失败', e) }
})

const handleSave = async () => {
    saving.value = true
    try {
        await httpInstance({ url: '/user/info', method: 'put', data: profileForm.value })
        userStore.setUserInfo({ ...userStore.userInfo, nickname: profileForm.value.nickname, phone: profileForm.value.phone })
        ElMessage.success({ message: '保存成功', duration: 2000 })
    } catch (e) { console.log('保存失败', e) }
    finally { saving.value = false }
}

const handleChangePwd = async () => {
    if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
        ElMessage.warning('请填写完整')
        return
    }
    changingPwd.value = true
    try {
        await httpInstance({
            url: '/user/password', method: 'put',
            params: { userId: userStore.userInfo.id, oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword }
        })
        ElMessage.success({ message: '密码修改成功', duration: 2000 })
        pwdForm.value = { oldPassword: '', newPassword: '' }
    } catch (e) { console.log('修改失败', e) }
    finally { changingPwd.value = false }
}
</script>
