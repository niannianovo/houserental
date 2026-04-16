<template>
    <div class="login-wrapper">
        <el-card class="login-card">
            <template #header>
                <div class="card-header">
                    <h2>欢迎登录</h2>
                    <p class="subtitle">请选择本次登录身份</p>
                </div>
            </template>

            <!-- 身份切换 Tabs - name改成数字 -->
            <el-tabs v-model="activeRole" class="role-tabs" @tab-click="handleTabClick">
                <el-tab-pane label="租户" :name="0">
                    <template #label>
                        <span><el-icon>
                                <User />
                            </el-icon> 租户</span>
                    </template>
                </el-tab-pane>
                <el-tab-pane label="房东" :name="1">
                    <template #label>
                        <span><el-icon>
                                <HomeFilled />
                            </el-icon> 房东</span>
                    </template>
                </el-tab-pane>
            </el-tabs>

            <!-- 登录表单 -->
            <el-form ref="formRef" :model="loginForm" :rules="rules">
                <el-form-item prop="account">
                    <el-input v-model="loginForm.account" :placeholder="accountPlaceholder" size="large"
                        :prefix-icon="User" />
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="loginForm.password" placeholder="密码" type="password" size="large" :prefix-icon="Lock"
                        show-password />
                </el-form-item>

                <el-form-item>
                    <el-button class="btn" type="primary" @click="doLogin" size="large" :loading="loading">
                        {{ activeRole === 0 ? '租户登录' : '房东登录' }} <!-- ⭐ 判断条件改成数字 -->
                    </el-button>
                </el-form-item>

                <div class="form-footer">
                    <el-checkbox v-model="remember">记住密码</el-checkbox>
                    <el-link type="primary">忘记密码？</el-link>
                </div>
            </el-form>

            <!-- 提示信息 -->
            <div class="role-tip">
                <el-alert :title="roleTip" type="info" :closable="false" show-icon />
            </div>

            <div class="register-link">
                还没有账号？<el-link type="primary" @click="handleRegister">立即注册</el-link>
            </div>
        </el-card>
    </div>
</template>
  
<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, HomeFilled, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 当前选中的角色：0租客，1房东（和后端 currentRole 一致）
const activeRole = ref(0) // 默认租客（0）
const remember = ref(false)
const loading = ref(false)

// 登录表单
const loginForm = ref({
    account: '',
    password: ''
})

// 账号输入框提示
const accountPlaceholder = computed(() => {
    return activeRole.value === 0
        ? '请输入账号'
        : '请输入账号'
})

// 角色提示
const roleTip = computed(() => {
    return activeRole.value === 0  // ⭐ 判断条件改成数字
        ? '租户身份：可浏览房源、预约看房、在线租房'
        : '房东身份：可发布房源、管理订单、查看收益'
})

// 表单校验规则
const rules = {
    account: [
        { required: true, message: '账号不能为空', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '密码不能为空', trigger: 'blur' }
    ]
}

const formRef = ref(null)

// 切换角色时
const handleTabClick = () => {
    // 切换角色
}

// 登录
const doLogin = async () => {
    formRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                const loginData = {
                    account: loginForm.value.account,
                    password: loginForm.value.password,
                    currentRole: activeRole.value  // 0租客 1房东
                }

                await userStore.getUserInfo(loginData)
                ElMessage.success({ message: `${activeRole.value === 0 ? '租户' : '房东'}登录成功`, duration: 2000 })

                // 根据角色跳转不同首页
                router.replace(activeRole.value === 0 ? '/tenant' : '/landlord')
            } catch (error) {
                console.log('登录失败', error)
            } finally {
                loading.value = false
            }
        }
    })
}

// 注册
const handleRegister = () => {
    router.push('/register')
}
</script>

<style scoped>
.login-wrapper {
    height: 100vh;
    /* 固定为视口高度 */
    width: 100vw;
    /* 固定为视口宽度 */
    display: flex;
    justify-content: center;
    align-items: center;
    background: linear-gradient(145deg, #e8f5e9 0%, #c8e6c9 100%);
    position: fixed;
    /* 固定定位，确保覆盖整个视口 */
    top: 0;
    left: 0;
    overflow: hidden;
    /* 隐藏溢出，禁止滚动 */
    padding: 0;
    /* 移除内边距 */
}

/* 背景装饰 */
.login-wrapper::before {
    content: '';
    position: absolute;
    width: 600px;
    height: 600px;
    background: radial-gradient(circle at 30% 50%, rgba(129, 199, 132, 0.15) 0%, transparent 60%);
    top: -200px;
    right: -200px;
    border-radius: 50%;
    pointer-events: none;
    /* 确保不影响点击 */
}

.login-wrapper::after {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle at 70% 80%, rgba(102, 187, 106, 0.12) 0%, transparent 60%);
    bottom: -100px;
    left: -100px;
    border-radius: 50%;
    pointer-events: none;
}

.login-card {
    width: 440px;
    max-width: 90%;
    /* 移动端适配 */
    border-radius: 32px;
    background: #ffffff;
    box-shadow:
        0 20px 40px -20px rgba(46, 125, 50, 0.3),
        0 0 0 1px rgba(200, 230, 201, 0.5);
    position: relative;
    z-index: 10;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    margin: 0;
    /* 移除可能的外边距 */
}

/* 卡片头部 */
:deep(.el-card__header) {
    border-bottom: none;
    padding: 32px 32px 0 32px;
}

.card-header {
    text-align: center;
    margin-bottom: 8px;
}

.card-header h2 {
    font-size: 28px;
    font-weight: 500;
    color: #2e5e3a;
    margin: 0 0 4px 0;
    letter-spacing: 0.5px;
}

.subtitle {
    font-size: 14px;
    color: #88a58b;
    margin: 0;
    font-weight: 400;
}

/* 书签形式的 Tabs - 清新风格 */
.role-tabs {
    margin: 16px 32px 20px 32px;
}

:deep(.el-tabs__header) {
    margin: 0;
}

:deep(.el-tabs__nav-wrap) {
    display: flex;
    justify-content: center;
}

:deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background: linear-gradient(90deg, transparent, #c8e6c9, #a5d6a7, #c8e6c9, transparent);
}

:deep(.el-tabs__item) {
    font-size: 16px;
    font-weight: 400;
    padding: 0 28px;
    height: 42px;
    line-height: 42px;
    color: #95b897;
    transition: all 0.2s ease;
    position: relative;
}

:deep(.el-tabs__item:hover) {
    color: #2e7d32;
    background: rgba(129, 199, 132, 0.05);
    border-radius: 21px 21px 0 0;
}

:deep(.el-tabs__item.is-active) {
    color: #2e7d32;
    font-weight: 500;
}

:deep(.el-tabs__active-bar) {
    height: 2px;
    background: #81c784;
    border-radius: 2px 2px 0 0;
    bottom: 0;
    transition: width 0.2s ease, transform 0.2s ease;
}

/* 标签内图标和文字样式 */
:deep(.el-tabs__item .el-icon) {
    margin-right: 8px;
    font-size: 18px;
    vertical-align: middle;
    color: inherit;
}

/* 卡片内容区域 */
:deep(.el-card__body) {
    padding: 0 32px 32px 32px;
}

/* 表单样式 */
.el-form {
    margin-top: 8px;
}

:deep(.el-form-item) {
    margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #e3f0e3 inset;
    border-radius: 20px;
    transition: all 0.2s ease;
    background: #f9fcf9;
    padding: 2px 16px;
}

:deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px #a5d6a7 inset;
    background: #ffffff;
}

:deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 2px #81c784 inset !important;
    background: #ffffff;
}

:deep(.el-input__inner) {
    height: 44px;
    font-size: 14px;
    color: #2e5e3a;
}

:deep(.el-input__inner::placeholder) {
    color: #b8d4ba;
    font-weight: 300;
}

:deep(.el-input__prefix-inner .el-icon) {
    font-size: 18px;
    color: #a5d6a7;
    transition: color 0.2s ease;
}

:deep(.el-input__wrapper:hover .el-input__prefix-inner .el-icon) {
    color: #81c784;
}

:deep(.el-input__wrapper.is-focus .el-input__prefix-inner .el-icon) {
    color: #2e7d32;
}

/* 登录按钮 */
.btn {
    width: 100%;
    height: 44px;
    font-size: 15px;
    font-weight: 500;
    letter-spacing: 1px;
    border-radius: 22px;
    background: #81c784;
    border: none;
    color: white;
    transition: all 0.2s ease;
    margin-top: 8px;
}

.btn:hover {
    background: #66bb6a;
    transform: translateY(-2px);
    box-shadow: 0 8px 16px -8px rgba(102, 187, 106, 0.6);
}

.btn:active {
    transform: translateY(0);
    background: #4caf50;
    box-shadow: none;
}

.btn.is-loading {
    background: #b8d4ba;
    opacity: 0.8;
}

/* 表单底部 */
.form-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 12px 0 0 0;
}

:deep(.el-checkbox) {
    height: 20px;
}

:deep(.el-checkbox__label) {
    color: #88a58b;
    font-size: 13px;
    transition: color 0.2s ease;
}

:deep(.el-checkbox:hover .el-checkbox__label) {
    color: #2e7d32;
}

:deep(.el-checkbox.is-checked .el-checkbox__label) {
    color: #2e7d32;
}

:deep(.el-checkbox__inner) {
    width: 16px;
    height: 16px;
    border-radius: 4px;
    border: 1px solid #c8e6c9;
    transition: all 0.2s ease;
}

:deep(.el-checkbox.is-checked .el-checkbox__inner) {
    background-color: #81c784;
    border-color: #81c784;
}

:deep(.el-checkbox__inner:hover) {
    border-color: #81c784;
}

:deep(.el-link) {
    font-size: 13px;
    font-weight: 400;
    color: #88a58b;
    transition: color 0.2s ease;
}

:deep(.el-link:hover) {
    color: #2e7d32;
}

/* 角色提示区域 */
.role-tip {
    margin: 20px 0 16px 0;
}

:deep(.el-alert) {
    border-radius: 16px;
    background: #f3faf3;
    border: 1px solid #e3f0e3;
    padding: 10px 14px;
}

:deep(.el-alert__title) {
    color: #2e5e3a;
    font-size: 13px;
    font-weight: 400;
}

:deep(.el-alert__icon) {
    color: #81c784;
    font-size: 16px;
}

/* 注册链接 */
.register-link {
    text-align: center;
    margin-top: 16px;
    font-size: 13px;
    color: #b8d4ba;
}

.register-link .el-link {
    font-size: 13px;
    font-weight: 500;
    margin-left: 4px;
    color: #81c784;
}

.register-link .el-link:hover {
    color: #2e7d32;
}

/* 移动端适配 - 仍然保持无滚动 */
@media screen and (max-width: 768px) {
    .login-wrapper {
        padding: 0;
        /* 确保移动端也无内边距 */
    }

    .login-card {
        max-width: 90%;
        margin: 0;
        /* 移除外边距 */
    }

    :deep(.el-tabs__item) {
        padding: 0 20px;
        font-size: 15px;
    }

    :deep(.el-card__header),
    :deep(.el-card__body) {
        padding-left: 24px;
        padding-right: 24px;
    }

    .card-header h2 {
        font-size: 26px;
    }
}

/* 清新风格的加载动画 */
:deep(.el-loading-spinner .path) {
    stroke: #81c784 !important;
}

/* 密码可见性图标 */
:deep(.el-input__suffix .el-icon) {
    color: #b8d4ba;
    font-size: 16px;
    transition: color 0.2s ease;
}

:deep(.el-input__suffix .el-icon:hover) {
    color: #81c784;
}

/* 移除所有粗体和重阴影，保持轻盈感 */
:deep(.el-tabs__item) {
    font-weight: 400 !important;
}

:deep(.el-tabs__item.is-active) {
    font-weight: 500 !important;
}

/* 添加微妙的动效 */
.el-form-item {
    animation: fadeInUp 0.4s ease backwards;
}

.el-form-item:nth-child(1) {
    animation-delay: 0.1s;
}

.el-form-item:nth-child(2) {
    animation-delay: 0.2s;
}

.btn {
    animation: fadeInUp 0.4s 0.3s ease backwards;
}

.role-tip {
    animation: fadeIn 0.5s 0.4s ease backwards;
}

.register-link {
    animation: fadeIn 0.5s 0.5s ease backwards;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
    }
}

/* 确保body和html也没有滚动 */
:global(html),
:global(body) {
    margin: 0;
    padding: 0;
    overflow: hidden;
    height: 100%;
    width: 100%;
}
</style>