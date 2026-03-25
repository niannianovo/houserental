<template>
    <div class="login-wrapper">
        <el-card class="login-card">
            <template #header>
                <div class="card-header">
                    <h2>注册账号</h2>
                    <p class="subtitle">请填写以下信息</p>
                </div>
            </template>

            <el-form ref="formRef" :model="registerForm" :rules="rules">
                <el-form-item prop="account">
                    <el-input v-model="registerForm.account" placeholder="账号（4-20位，字母开头，字母数字下划线）" size="large"
                        :prefix-icon="User" />
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="registerForm.password" placeholder="密码（8-20位，含字母+数字+特殊字符）" type="password"
                        size="large" :prefix-icon="Lock" show-password />
                </el-form-item>
                <el-form-item prop="confirmPassword">
                    <el-input v-model="registerForm.confirmPassword" placeholder="请确认密码" type="password" size="large"
                        :prefix-icon="Lock" show-password />
                </el-form-item>
                <el-form-item prop="email">
                    <el-input v-model="registerForm.email" placeholder="请输入邮箱" size="large"
                        :prefix-icon="Message" />
                </el-form-item>
                <el-form-item prop="verifyCode">
                    <div class="verify-code-row">
                        <el-input v-model="registerForm.verifyCode" placeholder="请输入验证码" size="large" />
                        <el-button type="success" size="large" :disabled="countdown > 0 || sendingCode"
                            :loading="sendingCode" @click="handleSendCode" class="send-code-btn">
                            {{ countdown > 0 ? `${countdown}s后重发` : '发送验证码' }}
                        </el-button>
                    </div>
                </el-form-item>

                <el-form-item>
                    <el-button class="btn" type="primary" @click="doRegister" size="large" :loading="loading">
                        注 册
                    </el-button>
                </el-form-item>
            </el-form>

            <div class="login-link">
                已有账号？<el-link type="primary" @click="$router.push('/login')">去登录</el-link>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { register, sendRegisterCode } from '@/api/user'
import { User, Lock, Message } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer = null

const registerForm = ref({
    account: '',
    password: '',
    confirmPassword: '',
    email: '',
    verifyCode: ''
})

const validateAccount = (rule, value, callback) => {
    if (!value) {
        callback(new Error('账号不能为空'))
    } else if (value.length < 4 || value.length > 20) {
        callback(new Error('账号长度必须在4-20个字符之间'))
    } else if (!/^[a-zA-Z][a-zA-Z0-9_]{3,19}$/.test(value)) {
        callback(new Error('账号必须以字母开头，只能包含字母、数字和下划线'))
    } else {
        callback()
    }
}

const validatePassword = (rule, value, callback) => {
    if (!value) {
        callback(new Error('密码不能为空'))
    } else if (value.length < 8 || value.length > 20) {
        callback(new Error('密码长度必须在8-20个字符之间'))
    } else if (!/[a-zA-Z]/.test(value)) {
        callback(new Error('密码必须包含至少一个字母'))
    } else if (!/\d/.test(value)) {
        callback(new Error('密码必须包含至少一个数字'))
    } else if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value)) {
        callback(new Error('密码必须包含至少一个特殊字符（如 !@#$%^&*）'))
    } else {
        callback()
    }
}

const validateConfirmPassword = (rule, value, callback) => {
    if (value !== registerForm.value.password) {
        callback(new Error('两次密码不一致'))
    } else {
        callback()
    }
}

const rules = {
    account: [{ validator: validateAccount, trigger: 'blur' }],
    password: [{ validator: validatePassword, trigger: 'blur' }],
    confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
    ],
    email: [
        { required: true, message: '邮箱不能为空', trigger: 'blur' },
        { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
    ],
    verifyCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' }
    ]
}

const formRef = ref(null)

const startCountdown = () => {
    countdown.value = 60
    countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
            clearInterval(countdownTimer)
            countdownTimer = null
        }
    }, 1000)
}

onBeforeUnmount(() => {
    if (countdownTimer) clearInterval(countdownTimer)
})

const handleSendCode = async () => {
    // 先单独校验邮箱字段
    try {
        await formRef.value.validateField('email')
    } catch {
        return
    }

    sendingCode.value = true
    try {
        await sendRegisterCode(registerForm.value.email)
        ElMessage.success('验证码已发送到您的邮箱')
        startCountdown()
    } catch (error) {
        console.log('发送验证码失败', error)
    } finally {
        sendingCode.value = false
    }
}

const doRegister = async () => {
    formRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                await register({
                    account: registerForm.value.account,
                    password: registerForm.value.password,
                    email: registerForm.value.email,
                    verifyCode: registerForm.value.verifyCode
                })
                ElMessage.success('注册成功，请登录')
                router.push('/login')
            } catch (error) {
                console.log('注册失败', error)
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
    background: linear-gradient(145deg, #e8f5e9 0%, #c8e6c9 100%);
    position: fixed;
    top: 0;
    left: 0;
    overflow: hidden;
}

.login-card {
    width: 440px;
    max-width: 90%;
    border-radius: 32px;
    background: #ffffff;
    box-shadow: 0 20px 40px -20px rgba(46, 125, 50, 0.3);
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
    color: #2e5e3a;
    margin: 0 0 4px 0;
}

.subtitle {
    font-size: 14px;
    color: #88a58b;
    margin: 0;
}

:deep(.el-card__body) {
    padding: 24px 32px 32px 32px;
}

:deep(.el-input__wrapper) {
    border-radius: 20px;
    padding: 2px 16px;
}

.verify-code-row {
    display: flex;
    width: 100%;
    gap: 10px;
}

.verify-code-row .el-input {
    flex: 1;
}

.send-code-btn {
    border-radius: 20px;
    white-space: nowrap;
    min-width: 120px;
}

.btn {
    width: 100%;
    height: 44px;
    font-size: 15px;
    border-radius: 22px;
    background: #81c784;
    border: none;
    margin-top: 8px;
}

.btn:hover {
    background: #66bb6a;
}

.login-link {
    text-align: center;
    margin-top: 16px;
    font-size: 13px;
    color: #b8d4ba;
}
</style>
