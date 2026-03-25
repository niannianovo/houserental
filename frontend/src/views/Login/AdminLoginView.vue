<template>
    <div class="login-wrapper admin-login">
        <el-card class="login-card">
            <template #header>
                <div class="card-header">
                    <h2>管理后台</h2>
                    <p class="subtitle">管理员登录</p>
                </div>
            </template>

            <el-form ref="formRef" :model="loginForm" :rules="rules">
                <el-form-item prop="account">
                    <el-input v-model="loginForm.account" placeholder="管理员账号" size="large" :prefix-icon="User" />
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="loginForm.password" placeholder="密码" type="password" size="large"
                        :prefix-icon="Lock" show-password />
                </el-form-item>

                <el-form-item>
                    <el-button class="btn" type="primary" @click="doLogin" size="large" :loading="loading">
                        登 录
                    </el-button>
                </el-form-item>
            </el-form>

            <div class="back-link">
                <el-link type="primary" @click="$router.push('/login')">返回用户登录</el-link>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/user'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const adminStore = useAdminStore()
const loading = ref(false)

const loginForm = ref({
    account: '',
    password: ''
})

const rules = {
    account: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
    password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
}

const formRef = ref(null)

const doLogin = async () => {
    formRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                await adminStore.getAdminInfo({
                    account: loginForm.value.account,
                    password: loginForm.value.password
                })
                ElMessage.success({ message: '管理员登录成功', duration: 2000 })
                router.replace('/admin')
            } catch (error) {
                console.log('登录失败', error)
            } finally {
                loading.value = false
            }
        }
    })
}
</script>

<style scoped>
.login-wrapper {
    height: 100vh;
    width: 100vw;
    display: flex;
    justify-content: center;
    align-items: center;
    background: linear-gradient(145deg, #e3f2fd 0%, #bbdefb 100%);
    position: fixed;
    top: 0;
    left: 0;
    overflow: hidden;
}

.login-card {
    width: 420px;
    max-width: 90%;
    border-radius: 32px;
    background: #ffffff;
    box-shadow: 0 20px 40px -20px rgba(25, 118, 210, 0.3);
}

:deep(.el-card__header) {
    border-bottom: none;
    padding: 32px 32px 0 32px;
}

.card-header {
    text-align: center;
}

.card-header h2 {
    font-size: 28px;
    font-weight: 500;
    color: #1565c0;
    margin: 0 0 4px 0;
}

.subtitle {
    font-size: 14px;
    color: #90a4ae;
    margin: 0;
}

:deep(.el-card__body) {
    padding: 24px 32px 32px 32px;
}

:deep(.el-input__wrapper) {
    border-radius: 20px;
    padding: 2px 16px;
}

.btn {
    width: 100%;
    height: 44px;
    font-size: 15px;
    border-radius: 22px;
    background: #42a5f5;
    border: none;
    margin-top: 8px;
}

.btn:hover {
    background: #1e88e5;
}

.back-link {
    text-align: center;
    margin-top: 16px;
}
</style>
