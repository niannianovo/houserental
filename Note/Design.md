# 房屋租赁系统 - 设计方案

## 一、项目概述

基于 Spring Boot 2.7 + MyBatis-Plus + MySQL 的房屋租赁平台，支持租客浏览租房、房东发布管理房源、管理员审核监管。核心亮点为**真假房源鉴别机制**（图片相似性鉴别 + 人工审核）。

技术栈：
- 后端：Spring Boot 2.7 + MyBatis-Plus 3.5 + MySQL + WebSocket
- 工具库：Lombok、Fastjson2、EasyExcel、Commons-Lang3
- 连接池：Druid
- 邮件：spring-boot-starter-mail（注册验证码、忘记密码验证码）

---

## 二、角色与登录设计

### 2.1 角色划分

| 角色 | 说明 |
|------|------|
| 租客 | 浏览/收藏/预约看房/签约/支付租金 |
| 房东 | 发布/管理房源/收租/签约 |
| 管理员 | 审核房源、管理用户、数据统计 |

- 租客和房东不是固定角色，用户每次登录时选择身份（current_role），可自由切换
- 管理员通过 is_admin 标记，独立登录入口，不参与身份切换

### 2.2 双登录入口

| 入口 | 路径 | 说明 |
|------|------|------|
| 用户登录 | `/login` | 租客/房东登录，登录后选择身份 |
| 管理员登录 | `/admin/login` | 管理员专用，独立页面 |

- 普通用户从管理员入口登录 → 提示"非管理员账号"
- 管理员从用户入口登录 → 提示"请从管理后台登录"

### 2.3 登录流程

```
用户登录：
  输入账号密码 → 校验通过 → 检查 is_admin=0
  → 检查邮箱是否已绑定
     ├── 未绑定 → 强制跳转绑定邮箱页（发验证码确认）
     └── 已绑定 → 选择身份（租客/房东）→ 写入 current_role → 进入系统

管理员登录：
  输入账号密码 → 校验通过 → 检查 is_admin=1 → 直接进入管理后台
```

### 2.4 权限控制

- current_role=0（租客身份）：屏蔽所有房源管理接口，不能操作房源
- current_role=1（房东身份）：可发布管理房源、收租等
- is_admin=1（管理员）：访问所有管理后台接口

---

## 三、功能模块

### 3.1 用户模块

| 功能 | 说明 |
|------|------|
| 修改个人信息 | 修改昵称、头像、手机号 |
| 修改密码 | 需验证旧密码 |
| 忘记密码 | 通过绑定邮箱发送6位验证码，5分钟过期，验证后重置密码 |
| 绑定邮箱 | 注册时通过邮箱验证码验证，注册即完成邮箱绑定 |
| 查看用户主页 | 展示用户公开信息（昵称、头像、邮箱验证状态、注册时间）及收到的评价 |

**忘记密码流程：**
```
用户点击「忘记密码」→ 输入注册邮箱
→ 后端生成6位验证码，存内存缓存（ConcurrentHashMap，5分钟过期）
→ 通过 spring-boot-starter-mail 发送到邮箱
→ 用户输入验证码 + 新密码 → 验证通过 → 重置密码
```

**用户公开主页：**
- 通过 `/user/profile/{id}` 获取用户公开信息，自动过滤敏感字段（密码、手机、邮箱）
- 展示基本信息：头像、昵称、角色、邮箱验证状态、注册时间
- 展示该用户收到的评价列表（来自 `user_review` 表）
- 房源详情页中房东信息区域可点击跳转至房东的公开主页

### 3.2 房源模块

| 功能 | 说明 |
|------|------|
| 发布房源 | 房东填写房源信息（含省市区三级联动+详细地址）、上传图片 |
| 搜索房源 | 按省/市/区级联筛选、价格区间、房屋类型、关键词搜索，分页展示 |
| 房源详情 | 展示完整信息（含省市区地址拼接）、房东信息卡片、用户备注、房源评论、相似推荐 |
| 修改/下架 | 房东管理自己的房源（修改后重新审核，回填省市区级联） |
| 访问统计 | 浏览次数、收藏数、沟通数（房东可见） |

**省市区三级联动机制：**
- 数据库：house 表使用 `province`、`city`、`district` 三个字段，`address` 字段存储小区名+门牌号等详细地址
- 前端：使用 Element Plus `el-cascader` 组件 + 静态省市区 JSON 数据
- 房东发布时：选择省市区 → 输入小区详细地址
- 租客搜索时：级联选择器支持只选省、选到市、或选到区（`checkStrictly: true`）
- 展示时：省+市+区+详细地址拼接展示

### 3.3 真假房源鉴别机制

核心亮点，基于**纯 Java 图片相似性鉴别 + 人工审核**，无需 Python 微服务或外部 AI API。

#### 3.3.1 核心算法 —— pHash 感知哈希

算法原理：
```
原图 → 缩放为 32×32 → 灰度化 → DCT(离散余弦变换) → 取左上角 8×8 低频区域
→ 计算均值 → 大于均值=1，小于=0 → 生成 64 位哈希值
```

两张图片的相似度用**汉明距离**衡量（两个哈希值逐位异或后 1 的个数，越小越相似）：

```java
int distance = Long.bitCount(hash1 ^ hash2); // 一行代码
```

数据库存储：每张图片入库时计算并存储 pHash 值（bigint），后续比对直接查表计算汉明距离。

#### 3.3.2 双重检测算法

采用 pHash 汉明距离 + 颜色直方图余弦相似度的双重检测：

| 算法 | 用途 | 阈值 |
|------|------|------|
| pHash 汉明距离 | 整体结构相似度 | ≤10 判定疑似盗图 |
| 颜色直方图余弦相似度 | 抗裁剪检测 | ≥0.90 判定疑似盗图 |

两种算法任一触发即标记风险，交由人工审核。

#### 3.3.3 鉴别流程

```
房东发布/修改房源
       │
       ▼
  保存房源基本信息
       │
       ▼
  触发图片鉴别
       │
       ├── 1. 读取图片文件，计算 pHash + 颜色直方图并存入 image_hash 表
       ├── 2. 全库比对（跨房源查重：pHash汉明距离 + 颜色余弦相似度）
       │
       ▼
  根据比对结果判定
       ├── 无相似图片 → verifyStatus=1（自动通过）
       └── 发现疑似盗图 → verifyStatus=0（标记 similarHouseId，通知管理员审核）
```

#### 3.3.4 管理员审核

管理员在审核页面可以：
- 查看待审核房源列表，标有"疑似重复"标记的重点关注
- 对比查看：并排展示待审核房源与相似房源的详细信息和图片
- 操作：通过（上架）/ 驳回（通知房东原因）

### 3.4 聊天模块

基于 WebSocket 的实时聊天，支持三种会话类型。

**会话类型：**

| type | 说明 |
|------|------|
| 0 | 租客 ↔ 房东（关联房源） |
| 1 | 租客 ↔ 客服 |
| 2 | 房东 ↔ 客服 |

**消息类型：**

| msg_type | 说明 | content 存储 |
|----------|------|-------------|
| 0 | 文本 | 纯文字 |
| 1 | 图片 | 图片URL |
| 2 | 表情 | 表情编码（如 emoji:smile 或 Unicode） |
| 3 | 文件 | JSON：`{"name":"合同.pdf","url":"xxx","size":1024}` |
| 9 | 系统消息 | 提示文字 |

**WebSocket 设计：**
```
连接：ws://localhost:8081/ws/chat?userId=xxx
发消息：客户端 → 服务端：{ conversationId, content, msgType }
推送：  服务端 → 接收方：{ conversationId, senderId, content, msgType, createTime }
离线：  消息存库，标记 is_read=0，上线后拉取未读消息数
心跳：  每30秒 ping/pong 保持连接
```

**客服分配策略：**
```
用户点击「联系客服」→ 取当前在线管理员中活跃会话数最少的 → 创建会话
无人在线时 → 分配给固定兜底客服账号
```

**快捷回复：** 房东可预设常用回复模板（标题+内容，可排序），聊天时通过弹出面板一键填入输入框。支持在聊天页内直接管理（增删）快捷回复模板。

### 3.5 收藏与预约

| 功能 | 说明 |
|------|------|
| 收藏房源 | toggle 切换收藏/取消收藏，自动维护 house 表 favorite_count 计数 |
| 收藏列表 | 分页展示收藏房源，关联查询房源标题、图片、价格、户型、区域等完整信息 |
| 降价通知 | 房东修改价格降低时，自动通知所有收藏该房源的用户 |
| 预约看房 | 租客选择时间预约，房东确认/拒绝 |

### 3.6 租约管理

**签合同流程：**
```
租客在房源详情页发起租房 → 填写租期 → 房东确认签约（或双方在待签约阶段任一方取消）
→ 进入待付押金状态 → 租客缴纳押金 → status=进行中 → 自动生成每月待缴记录
```

**续租流程：**
```
租客发起续租 → 填写新截止日期 → 房东同意 → 更新 end_date → 生成新的月度待缴
```

**退租流程：**
```
租客发起退租申请 → 房东确认 → status=已退租
房东也可发起（要求租客搬离）→ 租客确认
```

租约状态流转：`待签约 → 待付押金 → 进行中 → 已到期 / 续租中 / 退租申请中 → 已退租`

其他终态：
- 已拒绝：房东拒绝签约、或房源被管理员处置时 pending 订单自动置为此状态
- 已取消：待签约阶段任一方主动取消

**订单列表增强：** 后端 `getMyList` / `getHistory` 批量关联 house 表填充 `houseTitle` 和 `houseImages`（避免 N+1），前端列表项展示房源缩略图 + 标题, 点击可跳转房源详情页。

### 3.7 收租与支付

```
租客视角：
  进入「我的租约」→ 查看待缴月份 → 点击「支付」
  → 线下支付：上传转账截图作为凭证
  → 等待房东确认

房东视角：
  进入「收租管理」→ 查看每月缴费情况（待缴/已缴/逾期）
  → 收到支付通知 → 查看凭证 → 确认收款
```

支付方式：线下转账 + 上传凭证（简单实现，不接入真实支付SDK）。

### 3.8 评论与互评

**房源评论：**
- 登录用户可对房源评论（1-5星 + 文字）
- 评论列表按时间倒序分页，展示评论者昵称和头像（后端批量关联 user 表填充）
- 软删除机制：管理员删除评论时将 status 改为 1，查询时过滤
- 前端集成在房源详情页内嵌，无需跳转独立页面

**用户互评：**
- 必须存在租约关系才能评价对方
- 每个租约双方只能互评一次（后端校验 order_id + reviewer_id 唯一）
- 互评列表展示评价者和被评价者昵称（后端批量关联 user 表填充 reviewerNickname、targetNickname）
- 前端独立页面（租客/房东各一个），支持"收到的评价"和"发出的评价"切换
- 可通过用户公开主页查看某用户收到的所有评价


### 3.9 拉黑功能

租户和房东可以相互拉黑。

**拉黑效果：**
- 无法给对方发消息（发送时校验）
- 无法预约对方的房源
- 无法评价对方
- 会话列表中标记为「已屏蔽」
- 可在设置中查看黑名单并解除

### 3.10 房源备注

用户可对任意房源添加私密备注，仅自己可见。每个用户对每个房源只有一条备注（user_id + house_id 联合唯一，新增或覆盖更新）。前端集成在房源详情页内嵌，支持添加/编辑/删除。

### 3.11 房源推荐

采用**隐式反馈偏好建模 + 多级降级召回 + 热门兜底**的混合推荐架构。推荐流程分为两个阶段：先从用户的浏览、收藏、预约等隐式行为中动态生成用户偏好画像，再将画像喂给逐级降级召回算法产出最终结果。首页和找房页面共用同一套推荐机制，找房页面支持在推荐排序的基础上叠加筛选条件。

#### 3.11.1 用户偏好建模（隐式反馈）

由于要求用户手动填写偏好会带来较高的使用门槛且偏好数据容易过时，本系统采用**基于隐式反馈的用户兴趣画像建模（Implicit Feedback User Profiling）** 方法，从用户行为日志中自动提取偏好。核心算法分三步：

**第一步：行为加权（Behavior Weighting）**

针对不同的用户行为赋予差异化的初始权重，量化兴趣强度：

| 行为类型 | 权重 | 依据 |
|---------|------|------|
| 浏览房源（house_view_log） | 1 | 弱兴趣信号 |
| 收藏房源（house_favorite） | 5 | 中兴趣信号 |
| 预约看房（appointment）  | 8 | 强兴趣信号（有线下行动意图） |

**第二步：时间衰减（Exponential Time Decay）**

为了捕捉用户兴趣漂移、让近期行为主导推荐，对每条行为在初始权重之上叠加一个指数衰减因子：

```
w = behavior_weight × exp(-λ·Δt)
```

其中 `Δt` 为行为距今的天数，`λ = ln(2) / 30`，即**半衰期 30 天**——每过 30 天一条行为的影响力减半。该公式的理论依据为：
- **数学**：指数函数是唯一满足"衰减速率与当前值成正比"的连续函数（`dw/dt = -λ·w`）
- **心理学**：契合艾宾浩斯遗忘曲线（Ebbinghaus Forgetting Curve）对人类记忆衰减规律的实证
- **工程**：Ding & Li (CIKM 2005)、Koren (KDD 2009) 等经典推荐系统文献中时间动态建模的标准做法

**第三步：加权特征聚合（Weighted Feature Aggregation）**

将每条带权行为连接到对应的房源，按房源属性分维度聚合成偏好向量：

| 维度 | 类型 | 聚合方式 |
|------|------|---------|
| 城市 preferredCity | 类别 | 加权频次 Top-1（按 city 分组求 Σw，取最大） |
| 区县 preferredDistrict | 类别 | 加权频次 Top-1 |
| 类型 preferredHouseType | 类别 | 加权频次 Top-1 |
| 户型 preferredRoomCount | 类别 | 加权频次 Top-1 |
| 价格区间 [priceMin, priceMax] | 数值 | 加权中位数（Weighted Median）± 30% |

价格采用**加权中位数而非加权平均**，理由是中位数对离群值（outlier）具有鲁棒性——用户偶尔误点一套远超预算的房源不会导致价格偏好被严重拉偏。

**产出**：一个结构与 `user_preference` 表完全对齐的 `UserPreference` 对象，无需持久化，每次推荐时实时计算；下游召回逻辑无需任何改动即可复用。

```
用户行为日志（house_view_log + house_favorite + appointment，最近 90 天）
      │
      ▼
  行为加权：w₀ = 浏览1 / 收藏5 / 预约8
      │
      ▼
  时间衰减：w = w₀ × exp(-ln2/30 × Δt天)
      │
      ▼
  JOIN house 获取房源特征
      │
      ▼
  分维度聚合：
    类别维度（城市/区/类型/户型） → 加权频次 Top-1
    价格维度 → 加权中位数 ± 30%
      │
      ▼
  动态 UserPreference 对象
```

#### 3.11.2 核心推荐算法（多级降级召回）

将上一步产出的 `UserPreference` 作为查询条件，按约束严格度从强到弱依次查询，每轮排除已选房源并追加至结果集，直到凑齐 `limit` 条或走到兜底：

```
用户请求推荐
      │
      ▼
  获取用户偏好（优先读 user_preference 表；若为空则调用隐式建模动态生成）
      │
      ▼
  多级降级召回（每轮排除已选出的房源ID）：
      │
      ├── 第1轮：精确匹配（省+市+区+价格+类型+户型）
      ├── 第2轮：放宽区 → 同城（省+市+价格+类型+户型）
      ├── 第3轮：放宽市 → 同省（省+价格+类型+户型）
      ├── 第4轮：放宽价格和类型 → 同城热门（省+市，无偏好约束）
      └── 第5轮：全站热门兜底（按收藏量+浏览量排序）
      │
      ▼
  返回推荐列表（第 1 轮结果排最前，逐轮靠后；每轮内部按收藏量/浏览量降序）
```

**算法特性**：
- **层级化约束松弛（Hierarchical Constraint Relaxation）**：模拟用户"找不到完美的就退而求其次"的决策过程
- **增量去重合并（Incremental Deduplication）**：`HashSet` 跟踪已命中 ID，保证结果无重复且天然按匹配度降序
- **热度二次排序（Popularity Re-ranking）**：每轮内部按 `favorite_count DESC, view_count DESC`，引入群体智慧作为质量先验
- **冷启动兜底**：无偏好且无行为的新用户直接走第 5 轮全站热门，保证接口永远有返回

#### 3.11.3 应用场景

**（一）首页推荐**

租客首页直接调用推荐接口，展示 8 条个性化推荐房源。

```
GET /recommend/list?userId=xxx&limit=8
```

**（二）找房页面（推荐排序 + 筛选 + 分页）**

找房页面调用推荐搜索接口。所有结果按推荐优先级排序，推荐列表中的房源排在前面，其余房源按热度（收藏数×3 + 浏览数）排序。

```
GET /recommend/search?userId=xxx&keyword=&province=&city=&houseType=&minPrice=&maxPrice=&page=1&size=12
```

推荐搜索流程：
```
用户打开找房页面（可选：输入筛选条件）
      │
      ▼
  1. 调用推荐算法获取推荐列表（按偏好逐级降级）
      │
      ▼
  2. 查询所有符合筛选条件的已上架房源
      │
      ▼
  3. 排序合并：
      ├── 推荐列表中的房源 → 按推荐顺序排前面
      └── 其余房源 → 按热度（收藏数×3 + 浏览数）排后面
      │
      ▼
  4. 分页返回
```

#### 3.11.4 相似房源推荐（详情页）

独立于个性化推荐，展示在房源详情页底部的"相似房源"：

```
输入：当前房源 A
      │
      ├── 第1轮：同城+同区+同类型+价格±30%
      ├── 第2轮：放宽到同城（去掉区限制）
      └── 第3轮：全站热门兜底
      │
      ▼
  返回相似房源列表（按浏览量排序）
```

#### 3.11.5 冷启动策略

| 场景 | 策略 |
|------|------|
| 全新用户（无行为日志，user_preference 也为空） | 隐式建模返回 null → 逐级降级全部跳过 → 退化为全站热门推荐（按收藏+浏览排序） |
| 用户有少量行为后 | 隐式偏好建模立即生效，推荐开始反映用户兴趣；随着行为增多，权重统计更稳定 |
| 用户长期不活跃后再回访 | 老行为在时间衰减下影响趋近于零，新行为主导画像，自动适应兴趣漂移 |
| 新房源（无浏览量） | 通过搜索可发现，随着浏览和收藏增长逐渐进入热门推荐 |

### 3.12 通知系统

统一通知，覆盖以下场景：

| type | 触发场景 |
|------|---------|
| 0 | 收藏房源降价 |
| 1 | 系统公告 |
| 2 | 审核结果（房源审核） |
| 4 | 支付提醒（租金待缴/已确认） |
| 5 | 合同提醒（签约/续租/退租/取消/强制下架） |

### 3.13 管理员 - 仪表盘

**数据概览卡片：**

| 指标 | 来源 |
|------|------|
| 用户总数 | user count |
| 房源总数 | house count |
| 待审核房源数 | house verify_status=0 count |
| 本月成交租约数 | rental_order 按 create_time 过滤 |
| 本月租金总额 | rent_payment status=已缴 sum(amount) |

**图表：**

| 图表 | 类型 |
|------|------|
| 月度成交趋势 | 折线图（近12个月） |
| 月度收租金额趋势 | 折线图 |

### 3.14 管理员 - 用户管理

- 查看所有用户列表（分页、搜索）
- 禁用账号（级联处理）：
  - 前置校验：该用户如存在 status∈{1,3,4}（履行中/续租中/退租申请中）的活跃租约, 直接拒绝禁用, 提示管理员先协调退租。避免产生"被禁用户持有活契约却无法登录操作"的僵尸状态
  - 级联 1：取消该用户 status∈{0,6}（待签约/待付押金）的订单, 置为已拒绝, 并通知对方
  - 级联 2：下架该用户名下所有 status=1（已上架）的房源
  - 最终置 user.status=1, 阻止登录
  - 返回消息汇总清理数量给管理员确认
- 启用账号：仅恢复登录, 房源不自动重新上架（房东需自行进入管理后台重新发布, 避免被禁期间违规内容"偷偷"恢复）
- 删除用户

### 3.15 管理员 - 房源审核

- 待审核房源列表，展示房源信息及相似房源标记
- 通过/驳回操作（驳回需填写原因）
- 疑似重复房源可对比查看（并排展示两套房源的详细信息和图片）

### 3.16 管理员 - 系统公告

- 发布/编辑/下架公告
- 发布时自动给所有用户推送通知

### 3.17 管理员 - 操作日志

使用 AOP 自动记录管理员所有操作：
- 审核房源、删除评论、禁用用户、发布公告等
- 可按管理员、操作类型、时间筛选查看

### 3.18 AI 大语言模型集成

集成通义千问大语言模型（Qwen），为平台增加智能找房能力。通过 RestTemplate 直接对接阿里云 DashScope API。

#### 3.18.1 智能找房助手

**交互流程：**
```
用户输入自然语言描述（支持多轮对话）
       │
       ▼
  AiService.smartSearch()
       │
       ├── 1. 组装系统提示词 + 用户对话历史
       ├── 2. 调用通义千问 API
       ├── 3. 大模型返回结构化 JSON（搜索条件）
       ├── 4. 解析条件调用房源搜索
       │
       ▼
  返回给前端：{ hint: "为您找到...", houses: [...] }
```

**提取的搜索条件：**

| 字段 | 说明 |
|------|------|
| province | 省份 |
| city | 城市 |
| district | 区县 |
| keyword | 关键词（小区名、地铁站等） |
| minPrice / maxPrice | 价格区间 |
| houseType | 类型（整租/合租） |
| roomCount | 户型（几居） |

**智能放宽策略：** 当搜索无结果时，自动逐步放宽条件（扩大价格范围±50% → 去除类型限制 → 扩大到市级范围 → 推荐热门房源）。

**多轮对话：** 前端维护对话历史（history数组），每次请求携带完整对话上下文，支持用户逐步细化需求。

#### 3.18.2 容错与降级

| 场景 | 处理方式 |
|------|----------|
| API 超时 | 返回友好提示，不阻塞主流程 |
| API 返回格式异常 | JSON 解析失败时降级处理 |
| API Key 余额不足 | 日志告警，搜索走传统筛选 |
| 模型输出不合理 | 对解析出的数值做边界校验 |

**核心原则：AI 功能是增强型的，关闭后系统所有功能正常运行。**

#### 3.18.3 配置项

```yaml
ai:
  api-key: sk-xxxx           # 通义千问 API Key
  model: qwen-turbo           # 模型
  base-url: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
```

---

## 四、数据库设计（共21张表）

### 4.1 user（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| account | varchar(50) | 账号 |
| password | varchar(100) | 密码（MD5加密） |
| nickname | varchar(50) | 昵称（唯一） |
| avatar | varchar(255) | 头像URL |
| email | varchar(100) | 邮箱 |
| phone | varchar(20) | 手机号 |
| is_admin | tinyint | 是否管理员（0否 1是） |
| current_role | tinyint | 当前身份（0租客 1房东） |
| is_email_verified | tinyint | 邮箱是否已验证（0/1） |
| status | tinyint | 账号状态（0正常 1禁用） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

### 4.2 house（房源表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| owner_id | int | 房东ID |
| title | varchar(100) | 标题 |
| description | text | 描述 |
| province | varchar(50) | 省份 |
| city | varchar(50) | 城市 |
| district | varchar(50) | 区县 |
| address | varchar(255) | 详细地址（小区名+门牌号） |
| area | decimal(10,2) | 面积（平方米） |
| price | decimal(10,2) | 月租金 |
| deposit | decimal(10,2) | 押金 |
| room_count | int | 室 |
| hall_count | int | 厅 |
| floor | varchar(20) | 楼层 |
| house_type | tinyint | 类型（0整租 1合租） |
| status | tinyint | 状态（0待审核 1已上架 2已下架 3已出租） |
| verify_status | tinyint | 审核状态（0未审核 1已通过 2已驳回） |
| images | text | 图片URL（逗号分隔） |
| contact_phone | varchar(20) | 联系电话 |
| contact_name | varchar(50) | 联系人 |
| view_count | int | 浏览次数（默认0） |
| favorite_count | int | 收藏数（默认0） |
| contact_count | int | 沟通数（默认0） |
| similar_house_id | int | 相似房源ID（图片鉴别标记） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

### 4.3 image_hash（图片哈希表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| house_id | int | 所属房源ID |
| image_url | varchar(255) | 图片路径 |
| phash | bigint | 感知哈希值（64位） |
| color_hist | text | 颜色直方图（序列化数组） |
| create_time | datetime | 入库时间 |

### 4.4 house_view_log（浏览记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| house_id | int | 房源ID |
| user_id | int | 用户ID（可为空） |
| create_time | datetime | 浏览时间 |

### 4.5 house_favorite（收藏表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| user_id | int | 用户ID |
| house_id | int | 房源ID |
| create_time | datetime | 收藏时间 |

### 4.6 house_comment（房源评论表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| house_id | int | 房源ID |
| user_id | int | 评论人ID |
| content | text | 评论内容 |
| rating | tinyint | 评分（1-5星） |
| status | tinyint | 状态（0正常 1已删除） |
| create_time | datetime | 创建时间 |

### 4.7 house_note（房源备注表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| user_id | int | 用户ID |
| house_id | int | 房源ID |
| content | text | 备注内容 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

> user_id + house_id 联合唯一

### 4.8 user_review（用户互评表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| order_id | int | 关联租约ID |
| reviewer_id | int | 评价人ID |
| target_id | int | 被评价人ID |
| reviewer_role | tinyint | 评价方向（0租客评房东 1房东评租客） |
| content | text | 评价内容 |
| rating | tinyint | 评分（1-5星） |
| status | tinyint | 状态（0正常 1已删除） |
| create_time | datetime | 创建时间 |

### 4.9 user_block（拉黑表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| user_id | int | 拉黑发起人 |
| blocked_user_id | int | 被拉黑的人 |
| create_time | datetime | 创建时间 |

### 4.10 user_preference（用户偏好表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| user_id | int | 用户ID |
| preferred_province | varchar(50) | 偏好省份 |
| preferred_city | varchar(50) | 偏好城市 |
| preferred_district | varchar(50) | 偏好区县 |
| preferred_areas | varchar(255) | 偏好区域（兼容旧数据） |
| price_min | decimal(10,2) | 偏好最低价 |
| price_max | decimal(10,2) | 偏好最高价 |
| preferred_house_type | tinyint | 偏好类型（0整租 1合租） |
| preferred_room_count | int | 偏好几居 |
| update_time | datetime | 更新时间 |

### 4.11 search_history（搜索历史表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| user_id | int | 用户ID |
| keyword | varchar(100) | 搜索关键词 |
| filters | text | 筛选条件（JSON） |
| create_time | datetime | 创建时间 |

### 4.12 conversation（聊天会话表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| house_id | int | 关联房源ID（客服会话为空） |
| user1_id | int | 发起方 |
| user2_id | int | 接收方 |
| type | tinyint | 类型（0租客↔房东 1租客↔客服 2房东↔客服） |
| last_message | varchar(255) | 最后一条消息 |
| last_message_time | datetime | 最后消息时间 |
| create_time | datetime | 创建时间 |

### 4.13 chat_message（聊天消息表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| conversation_id | int | 会话ID |
| sender_id | int | 发送人ID |
| content | text | 消息内容 |
| msg_type | tinyint | 类型（0文本 1图片 2表情 3文件 9系统消息） |
| is_read | tinyint | 已读状态（0未读 1已读） |
| create_time | datetime | 发送时间 |

### 4.14 quick_reply（快捷回复表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| user_id | int | 房东ID |
| title | varchar(50) | 标签 |
| content | text | 回复内容 |
| sort_order | int | 排序 |
| create_time | datetime | 创建时间 |

### 4.15 rental_order（租约表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| house_id | int | 房源ID |
| tenant_id | int | 租客ID |
| owner_id | int | 房东ID |
| start_date | date | 租期开始 |
| end_date | date | 租期结束 |
| monthly_rent | decimal(10,2) | 月租金 |
| contract_file | varchar(255) | 合同文件URL |
| status | tinyint | 状态（0待签约 1进行中 2已到期 3续租中 4退租申请中 5已退租 6待付押金 7已拒绝 8已取消） |
| deposit_amount | decimal(10,2) | 押金金额 |
| deposit_status | tinyint | 押金状态（0未付 1已付 2已退） |
| quit_applicant | int | 退租申请人ID |
| create_time | datetime | 创建时间 |

### 4.16 rental_order_log（租约操作日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| order_id | int | 租约ID |
| operator_id | int | 操作人ID |
| action | varchar(50) | 操作（签约/续租/退租申请/退租确认/退租拒绝） |
| remark | text | 备注 |
| create_time | datetime | 操作时间 |

### 4.17 rent_payment（租金支付表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| order_id | int | 租约ID |
| amount | decimal(10,2) | 金额 |
| pay_month | varchar(10) | 所属月份（如 2026-03） |
| status | tinyint | 状态（0待缴 1已缴 2逾期） |
| pay_method | tinyint | 支付方式（0线下 1支付宝 2微信） |
| pay_proof | varchar(255) | 支付凭证图片URL |
| confirm_status | tinyint | 房东确认（0待确认 1已确认） |
| pay_time | datetime | 实际支付时间 |
| create_time | datetime | 创建时间 |

### 4.18 notification（通知表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| user_id | int | 接收人ID |
| type | tinyint | 类型（0降价 1公告 2审核结果 4支付提醒 5合同提醒） |
| title | varchar(100) | 标题 |
| content | text | 内容 |
| related_id | int | 关联ID |
| is_read | tinyint | 已读状态（0/1） |
| create_time | datetime | 创建时间 |

### 4.19 announcement（系统公告表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| admin_id | int | 发布人 |
| title | varchar(100) | 标题 |
| content | text | 内容 |
| status | tinyint | 状态（0草稿 1已发布 2已下架） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

### 4.20 admin_log（管理员操作日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| admin_id | int | 操作人 |
| action | varchar(50) | 操作类型 |
| target_type | varchar(50) | 目标类型（user/house/comment/review/announcement） |
| target_id | int | 目标ID |
| detail | text | 操作详情 |
| create_time | datetime | 操作时间 |

### 4.21 appointment（预约看房表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键 |
| house_id | int | 房源ID |
| user_id | int | 预约人ID |
| appointment_time | datetime | 预约时间 |
| status | tinyint | 状态（0待确认 1已确认 2已取消 3已完成） |
| remark | varchar(255) | 备注 |
| create_time | datetime | 创建时间 |

---

## 五、API 接口总览

### 5.1 用户相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/login` | 用户登录 |
| POST | `/login/role` | 选择身份确认 |
| POST | `/admin/login` | 管理员登录 |
| POST | `/register/send-code` | 发送注册验证码 |
| POST | `/register` | 注册 |
| PUT | `/user/info` | 修改个人信息 |
| PUT | `/user/password` | 修改密码 |
| POST | `/user/forgot-password` | 忘记密码-发送验证码 |
| PUT | `/user/reset-password` | 忘记密码-重置密码 |
| POST | `/user/bind-email` | 绑定邮箱 |
| GET | `/user/profile/{id}` | 查看用户公开主页 |

### 5.2 房源相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/house` | 发布房源 |
| GET | `/house/list` | 搜索房源（分页） |
| GET | `/house/{id}` | 房源详情 |
| PUT | `/house/{id}` | 修改房源 |
| DELETE | `/house/{id}` | 删除房源 |
| GET | `/house/my` | 我的房源列表（房东） |

### 5.3 房源审核

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/verify/pending` | 待审核房源列表 |
| POST | `/verify/{houseId}` | 审核操作（通过/驳回） |

### 5.4 收藏/预约/备注

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/favorite/{houseId}` | 收藏/取消收藏 |
| GET | `/favorite/list` | 我的收藏 |
| POST | `/appointment` | 预约看房 |
| GET | `/appointment/list` | 我的预约 |
| PUT | `/appointment/{id}` | 确认/取消预约 |
| POST | `/house-note/{houseId}` | 添加/更新备注 |
| GET | `/house-note/{houseId}` | 查看备注 |
| DELETE | `/house-note/{houseId}` | 删除备注 |

### 5.5 聊天相关

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/chat/conversations` | 会话列表 |
| GET | `/chat/messages/{conversationId}` | 历史消息（分页） |
| POST | `/chat/conversation` | 创建会话 |
| PUT | `/chat/read/{conversationId}` | 标记已读 |
| WS | `ws://host/ws/chat?userId=xxx` | 实时收发消息 |

### 5.6 快捷回复

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/quick-reply/list` | 我的快捷回复 |
| POST | `/quick-reply` | 新增 |
| PUT | `/quick-reply/{id}` | 编辑 |
| DELETE | `/quick-reply/{id}` | 删除 |

### 5.7 租约相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/rental-order` | 创建租约 |
| GET | `/rental-order/list` | 我的租约（填充房源标题/图片） |
| GET | `/rental-order/history` | 租赁历史（含已取消/已拒绝） |
| PUT | `/rental-order/sign/{id}` | 房东同意签约 |
| PUT | `/rental-order/reject/{id}` | 房东拒绝签约 |
| PUT | `/rental-order/cancel/{id}` | 待签约阶段双方任一方取消 |
| PUT | `/rental-order/pay-deposit/{id}` | 租客缴纳押金 |
| PUT | `/rental-order/refund-deposit/{id}` | 房东退还押金 |
| PUT | `/rental-order/renew-apply/{id}` | 租客申请续租 |
| PUT | `/rental-order/renew-confirm/{id}` | 房东确认续租 |
| PUT | `/rental-order/quit/{id}` | 退租申请 |
| PUT | `/rental-order/quit-confirm/{id}` | 退租确认 |
| PUT | `/rental-order/quit-cancel/{id}` | 撤回退租申请 |

### 5.8 收租/支付

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/rent-payment/list/{orderId}` | 缴费记录 |
| POST | `/rent-payment/pay/{id}` | 支付（上传凭证） |
| PUT | `/rent-payment/confirm/{id}` | 房东确认收款 |

### 5.9 评论/互评

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/house-comment` | 评论房源 |
| GET | `/house-comment/list/{houseId}` | 房源评论列表 |
| GET | `/house-comment/my` | 我的评论历史 |
| POST | `/user-review` | 提交互评 |
| GET | `/user-review/{userId}` | 某用户收到的评价 |
| GET | `/user-review/my-sent` | 我发出的评价 |
| GET | `/user-review/my-received` | 我收到的评价 |

### 5.10 拉黑

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/block/{userId}` | 拉黑 |
| DELETE | `/block/{userId}` | 取消拉黑 |
| GET | `/block/list` | 黑名单 |

### 5.11 推荐

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/recommend/list` | 为我推荐 |
| GET | `/recommend/search` | 推荐排序+筛选+分页 |
| GET | `/recommend/similar/{houseId}` | 相似推荐 |

### 5.12 通知

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/notification/list` | 我的通知 |
| GET | `/notification/unread-count` | 未读数 |
| PUT | `/notification/read/{id}` | 标记已读 |
| PUT | `/notification/read-all` | 全部已读 |

### 5.13 文件上传

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/upload` | 上传文件/图片 |

### 5.14 管理员接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/user/list` | 用户列表 |
| PUT | `/admin/user/disable/{id}` | 禁用账号（级联拦截/清理，见 3.14） |
| PUT | `/admin/user/enable/{id}` | 启用账号 |
| DELETE | `/admin/user/{id}` | 删除用户 |
| PUT | `/admin/house/takedown/{id}` | 强制下架房源 |
| DELETE | `/admin/house/{id}` | 强制删除房源 |
| DELETE | `/admin/comment/{id}` | 删除违规评论 |
| POST | `/announcement` | 发布公告 |
| PUT | `/announcement/{id}` | 编辑公告 |
| DELETE | `/announcement/{id}` | 删除公告 |
| GET | `/announcement/list` | 公告列表 |
| GET | `/admin/log/list` | 操作日志 |
| GET | `/dashboard/overview` | 仪表盘概览 |
| GET | `/dashboard/order-trend` | 成交趋势 |
| GET | `/dashboard/payment-trend` | 收租金额趋势 |

### 5.15 AI 智能服务

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/search` | 智能找房（自然语言多轮对话 → 搜索结果） |

---

## 六、后端包结构

```
com.example
├── HouseRentalSystemApplication.java        启动类
├── common
│   ├── Result.java                          统一返回结果
│   ├── ConConfig.java                       跨域配置
│   ├── GlobalExceptionHandler.java          全局异常处理
│   └── ImageHashUtil.java                   图片pHash计算工具类
├── config
│   ├── MybatisPlusConfig.java               MyBatis-Plus配置
│   ├── WebConfig.java                       Web配置
│   └── WebSocketConfig.java                 WebSocket配置
├── annotation
│   └── AdminAction.java                     管理员操作注解
├── aspect
│   └── AdminLogAspect.java                  管理员操作日志AOP
├── controller
│   ├── UserController.java
│   ├── HouseController.java
│   ├── VerifyController.java                房源审核
│   ├── ReportController.java
│   ├── FavoriteController.java
│   ├── AppointmentController.java
│   ├── ChatController.java
│   ├── UploadController.java
│   ├── QuickReplyController.java
│   ├── RentalOrderController.java
│   ├── RentPaymentController.java
│   ├── HouseCommentController.java
│   ├── HouseNoteController.java
│   ├── UserReviewController.java
│   ├── BlockController.java
│   ├── RecommendController.java
│   ├── NotificationController.java
│   ├── AnnouncementController.java
│   ├── AdminController.java
│   ├── DashboardController.java
│   └── AiController.java                   AI智能找房接口
├── dto
│   ├── LoginDTO.java
│   └── SearchResult.java                   搜索结果（含分页+提示）
├── vo
│   ├── DashboardOverviewVO.java
│   ├── AreaDistributionVO.java
│   └── TrendVO.java
├── entity
│   ├── User.java
│   ├── House.java
│   ├── ImageHash.java                      图片哈希实体
│   ├── HouseViewLog.java
│   ├── HouseFavorite.java
│   ├── HouseComment.java
│   ├── HouseNote.java
│   ├── UserReview.java
│   ├── UserBlock.java
│   ├── UserPreference.java
│   ├── SearchHistory.java
│   ├── Conversation.java
│   ├── ChatMessage.java
│   ├── QuickReply.java
│   ├── RentalOrder.java
│   ├── RentalOrderLog.java
│   ├── RentPayment.java
│   ├── Report.java
│   ├── Notification.java
│   ├── Announcement.java
│   └── AdminLog.java
├── mapper
│   ├── (各实体对应Mapper)
│   ├── DashboardMapper.java
│   └── ImageHashMapper.java
├── service
│   ├── UserService.java
│   ├── HouseService.java
│   ├── ImageVerifyService.java             图片鉴别服务
│   ├── VerifyService.java                  房源审核服务
│   ├── ReportService.java
│   ├── FavoriteService.java
│   ├── AppointmentService.java
│   ├── ChatService.java
│   ├── UploadService.java
│   ├── QuickReplyService.java
│   ├── RentalOrderService.java
│   ├── RentPaymentService.java
│   ├── HouseCommentService.java
│   ├── HouseNoteService.java
│   ├── UserReviewService.java
│   ├── BlockService.java
│   ├── RecommendService.java
│   ├── NotificationService.java
│   ├── AnnouncementService.java
│   ├── EmailService.java
│   ├── DashboardService.java
│   ├── AiService.java                     AI服务（直接调用通义千问API）
│   └── Impl/
│       └── (各Service实现类)
├── websocket
│   └── ChatWebSocketHandler.java
└── Tool
    └── MD5Util.java
```
