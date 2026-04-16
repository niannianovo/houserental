<template>
    <div>
        <el-card class="search-card">
            <template #header><span class="section-title">找房</span></template>

            <!-- 筛选条件 -->
            <div class="filter-bar">
                <el-input v-model="filters.keyword" placeholder="关键词" clearable style="width: 180px;" />
                <el-cascader
                    v-model="filters.areaValue"
                    :options="areaData"
                    :props="{ expandTrigger: 'hover', checkStrictly: true }"
                    placeholder="选择区域"
                    clearable
                    style="width: 220px;"
                    @change="handleAreaChange"
                />
                <el-select v-model="filters.houseType" placeholder="类型" clearable style="width: 110px;">
                    <el-option label="整租" :value="0" />
                    <el-option label="合租" :value="1" />
                    <el-option label="公寓" :value="2" />
                </el-select>
                <el-input-number v-model="filters.minPrice" placeholder="最低价" :min="0" :step="500" controls-position="right" style="width: 120px;" />
                <span style="color: #88a58b;">—</span>
                <el-input-number v-model="filters.maxPrice" placeholder="最高价" :min="0" :step="500" controls-position="right" style="width: 120px;" />
                <el-button type="primary" class="green-btn" @click="handleSearch">搜索</el-button>
                <el-button @click="handleReset">重置</el-button>
            </div>

            <!-- 房源列表 -->
            <div v-loading="loading" style="margin-top: 20px;">
                <el-alert v-if="searchHint" :title="searchHint" type="info" show-icon :closable="false"
                          style="margin-bottom: 16px; border-radius: 8px;" />
                <el-row :gutter="16">
                    <el-col :span="6" v-for="house in houses" :key="house.id">
                        <div class="house-card" @click="$router.push(`/tenant/house/${house.id}`)">
                            <div class="house-img">
                                <img v-if="house.images" :src="house.images.split(',')[0]" alt="" />
                                <div v-else class="house-img-placeholder">暂无图片</div>
                                <div class="house-tag">
                                    {{ { 0: '整租', 1: '合租', 2: '公寓' }[house.houseType] || '' }}
                                </div>
                            </div>
                            <div class="house-info">
                                <div class="house-title">{{ house.title }}</div>
                                <div class="house-address">{{ [house.district, house.address].filter(Boolean).join(' ') }}</div>
                                <div class="house-meta">
                                    {{ house.roomCount }}室{{ house.hallCount }}厅 · {{ house.area }}m² · {{ house.floor }}楼
                                </div>
                                <div class="house-bottom">
                                    <span class="house-price"><span class="price">{{ house.price }}</span> 元/月</span>
                                    <span class="house-views">{{ house.viewCount || 0 }}浏览</span>
                                </div>
                            </div>
                        </div>
                    </el-col>
                </el-row>
                <el-empty v-if="!loading && houses.length === 0" description="没有找到符合条件的房源" />
            </div>

            <el-pagination
                v-if="total > 0"
                style="margin-top: 16px; justify-content: flex-end;"
                background layout="total, prev, pager, next"
                :total="total" :page-size="pageSize"
                v-model:current-page="currentPage"
                @current-change="loadHouses"
            />
        </el-card>

        <!-- AI 助手悬浮按钮 -->
        <div class="ai-fab" @click="aiOpen = !aiOpen" :class="{ active: aiOpen }">
            <el-icon :size="24"><MagicStick /></el-icon>
        </div>

        <!-- AI 助手面板 -->
        <transition name="ai-slide">
            <div v-if="aiOpen" class="ai-panel">
                <div class="ai-header">
                    <span>AI 找房助手</span>
                    <el-icon class="ai-close" @click="aiOpen = false"><Close /></el-icon>
                </div>
                <div class="ai-chat" ref="aiChatRef">
                    <!-- 欢迎语 -->
                    <div v-if="aiMessages.length === 0" class="ai-welcome">
                        <div style="font-size: 28px; margin-bottom: 8px;">🏠</div>
                        <div style="font-size: 13px; color: #2e5e3a; font-weight: 500;">告诉我你的需求</div>
                        <div class="ai-examples">
                            <div v-for="ex in aiExamples" :key="ex" class="ai-example" @click="aiSendExample(ex)">{{ ex }}</div>
                        </div>
                    </div>
                    <!-- 消息列表 -->
                    <div v-for="(msg, idx) in aiMessages" :key="idx" class="ai-msg" :class="msg.role">
                        <div class="ai-bubble">
                            <div v-html="formatReply(msg.content)"></div>
                            <div v-if="msg.houses && msg.houses.length > 0" class="ai-houses">
                                <div v-for="h in msg.houses" :key="h.id" class="ai-house-item"
                                     @click="$router.push(`/tenant/house/${h.id}`)">
                                    <div class="ai-house-title">{{ h.title }}</div>
                                    <div class="ai-house-meta">{{ h.roomCount }}室{{ h.hallCount }}厅 · {{ h.area }}m² · <span class="ai-house-price">{{ h.price }}元/月</span></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div v-if="aiLoading" class="ai-msg ai">
                        <div class="ai-bubble">思考中...</div>
                    </div>
                </div>
                <div class="ai-input">
                    <el-input v-model="aiText" placeholder="描述你的需求..." size="small"
                              :disabled="aiLoading" @keyup.enter="aiSend" />
                    <el-button type="primary" size="small" class="green-btn" @click="aiSend" :loading="aiLoading">发送</el-button>
                </div>
            </div>
        </transition>
    </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { recommendSearch } from '@/api/recommend'
import { aiSmartSearch } from '@/api/ai'
import { MagicStick, Close } from '@element-plus/icons-vue'
import areaData from '@/utils/areaData'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const houses = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 12
const searchHint = ref('')
const filters = ref({ keyword: '', areaValue: [], province: '', city: '', district: '', houseType: null, minPrice: null, maxPrice: null })

// ========== 搜索逻辑 ==========
const handleAreaChange = (val) => {
    filters.value.province = val && val.length >= 1 ? val[0] : ''
    filters.value.city = val && val.length >= 2 ? val[1] : ''
    filters.value.district = val && val.length >= 3 ? val[2] : ''
}

const loadHouses = async () => {
    loading.value = true
    try {
        const params = { userId: userStore.userInfo.id, page: currentPage.value, size: pageSize }
        if (filters.value.keyword) params.keyword = filters.value.keyword
        if (filters.value.province) params.province = filters.value.province
        if (filters.value.city) params.city = filters.value.city
        if (filters.value.district) params.district = filters.value.district
        if (filters.value.houseType !== null && filters.value.houseType !== undefined) params.houseType = filters.value.houseType
        if (filters.value.minPrice) params.minPrice = filters.value.minPrice
        if (filters.value.maxPrice) params.maxPrice = filters.value.maxPrice
        const res = await recommendSearch(params)
        houses.value = res.data?.page?.records || []
        total.value = res.data?.page?.total || 0
        searchHint.value = res.data?.hint || ''
    } catch (e) { console.log('搜索失败', e) }
    finally { loading.value = false }
}

const handleSearch = () => {
    currentPage.value = 1
    loadHouses()
}

const handleReset = () => {
    filters.value = { keyword: '', areaValue: [], province: '', city: '', district: '', houseType: null, minPrice: null, maxPrice: null }
    handleSearch()
}

// ========== AI 助手 ==========
const aiOpen = ref(false)
const aiText = ref('')
const aiLoading = ref(false)
const aiMessages = ref([])
const aiChatRef = ref(null)
const aiExamples = [
    '长沙岳麓区2000左右两居室',
    '深圳南山合租3000以内',
    '长沙天心区地铁口单间'
]

const formatReply = (text) => text ? text.replace(/\n/g, '<br>') : ''

const aiScrollBottom = () => {
    nextTick(() => {
        if (aiChatRef.value) aiChatRef.value.scrollTop = aiChatRef.value.scrollHeight
    })
}

const aiSendExample = (text) => {
    aiText.value = text
    aiSend()
}

const aiSend = async () => {
    const text = aiText.value.trim()
    if (!text || aiLoading.value) return

    aiMessages.value.push({ role: 'user', content: text })
    aiText.value = ''
    aiScrollBottom()

    // 构建历史（只发 role + content 文字，不发 houses）
    const history = aiMessages.value.map(m => ({ role: m.role, content: m.content }))

    aiLoading.value = true
    try {
        const res = await aiSmartSearch(history, userStore.userInfo.id)
        aiMessages.value.push({ role: 'ai', content: res.data.reply, houses: res.data.houses || [] })
    } catch (e) {
        aiMessages.value.push({ role: 'ai', content: '抱歉，AI助手暂时无法响应，请稍后再试。' })
    } finally {
        aiLoading.value = false
        aiScrollBottom()
    }
}

onMounted(() => {
    if (route.query.keyword) filters.value.keyword = route.query.keyword
    loadHouses()
})
</script>

<style scoped>
.search-card { border-radius: 16px; border: 1px solid #e8f5e9; }
.section-title { font-size: 16px; font-weight: 600; color: #2e5e3a; }
.filter-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.green-btn { background: #81c784 !important; border: none !important; }
.green-btn:hover { background: #66bb6a !important; }

.house-card {
    cursor: pointer; border-radius: 12px; overflow: hidden;
    background: #fff; border: 1px solid #e8f5e9; margin-bottom: 16px; transition: all 0.3s;
}
.house-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(76, 175, 80, 0.12);
    border-color: #a5d6a7;
}
.house-img { height: 150px; overflow: hidden; background: #e8f5e9; position: relative; }
.house-img img { width: 100%; height: 100%; object-fit: cover; }
.house-img-placeholder { height: 100%; display: flex; align-items: center; justify-content: center; color: #81c784; font-size: 14px; }
.house-tag { position: absolute; top: 8px; left: 8px; background: rgba(46,94,58,0.8); color: #fff; font-size: 11px; padding: 2px 8px; border-radius: 4px; }
.house-info { padding: 12px; }
.house-title { font-size: 14px; font-weight: 500; color: #2e5e3a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.house-address { font-size: 12px; color: #88a58b; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.house-meta { font-size: 12px; color: #88a58b; margin-top: 4px; }
.house-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.house-price { color: #88a58b; font-size: 12px; }
.house-price .price { font-size: 18px; font-weight: bold; color: #e65100; }
.house-views { font-size: 12px; color: #b8d4ba; }
:deep(.el-pagination.is-background .el-pager li.is-active) { background: #66bb6a !important; }

/* ========== AI 悬浮按钮 ========== */
.ai-fab {
    position: fixed; right: 28px; bottom: 28px; width: 50px; height: 50px;
    border-radius: 50%; background: #81c784; color: #fff;
    display: flex; align-items: center; justify-content: center;
    cursor: pointer; box-shadow: 0 4px 16px rgba(76,175,80,0.3);
    transition: all 0.3s; z-index: 100;
}
.ai-fab:hover { background: #66bb6a; transform: scale(1.1); }
.ai-fab.active { background: #4caf50; }

/* ========== AI 面板 ========== */
.ai-panel {
    position: fixed; right: 28px; bottom: 90px; width: 360px; height: 480px;
    background: #fff; border-radius: 16px; box-shadow: 0 8px 32px rgba(0,0,0,0.12);
    border: 1px solid #e8f5e9; display: flex; flex-direction: column;
    z-index: 100; overflow: hidden;
}
.ai-header {
    display: flex; justify-content: space-between; align-items: center;
    padding: 14px 16px; border-bottom: 1px solid #e8f5e9;
    font-size: 15px; font-weight: 600; color: #2e5e3a;
}
.ai-close { cursor: pointer; color: #88a58b; font-size: 18px; }
.ai-close:hover { color: #333; }

.ai-chat { flex: 1; overflow-y: auto; padding: 12px; }

/* 欢迎语 */
.ai-welcome { text-align: center; padding: 20px 0; }
.ai-examples { display: flex; flex-direction: column; gap: 6px; margin-top: 10px; }
.ai-example {
    padding: 8px 14px; border-radius: 16px; border: 1px solid #c8e6c9;
    color: #2e7d32; font-size: 12px; cursor: pointer; transition: all 0.2s;
}
.ai-example:hover { background: #e8f5e9; }

/* 消息 */
.ai-msg { display: flex; margin-bottom: 10px; }
.ai-msg.user { justify-content: flex-end; }
.ai-msg.ai { justify-content: flex-start; }

.ai-bubble { max-width: 88%; padding: 8px 12px; border-radius: 12px; font-size: 13px; line-height: 1.5; }
.ai-msg.user .ai-bubble { background: #81c784; color: #fff; border-bottom-right-radius: 4px; }
.ai-msg.ai .ai-bubble { background: #f3faf3; color: #333; border-bottom-left-radius: 4px; }

/* AI 返回的房源列表 */
.ai-houses { margin-top: 8px; display: flex; flex-direction: column; gap: 6px; }
.ai-house-item {
    padding: 8px 10px; border-radius: 8px; background: #fff; border: 1px solid #e8f5e9;
    cursor: pointer; transition: all 0.2s;
}
.ai-house-item:hover { border-color: #81c784; box-shadow: 0 2px 8px rgba(76,175,80,0.1); }
.ai-house-title { font-size: 13px; font-weight: 500; color: #2e5e3a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ai-house-meta { font-size: 11px; color: #88a58b; margin-top: 2px; }
.ai-house-price { color: #e65100; font-weight: 600; }

/* 输入区域 */
.ai-input {
    display: flex; gap: 8px; padding: 10px 12px; border-top: 1px solid #e8f5e9;
}
.ai-input .el-input { flex: 1; }

/* 动画 */
.ai-slide-enter-active, .ai-slide-leave-active { transition: all 0.3s ease; }
.ai-slide-enter-from, .ai-slide-leave-to { opacity: 0; transform: translateY(20px) scale(0.95); }
</style>
