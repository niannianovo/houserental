package com.example.service.Impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.House;
import com.example.service.AiService;
import com.example.service.HouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI 智能找房服务实现
 *
 * 通过 RestTemplate 调用阿里云通义千问（DashScope）大模型 API，
 * 将用户的自然语言租房描述解析为结构化搜索条件，再调用现有的房源搜索接口返回结果。
 *
 * 核心流程：
 * 1. 前端将用户的多轮对话历史（history）发送过来
 * 2. 拼接系统提示词（SYSTEM_PROMPT）+ 对话历史，调用大模型
 * 3. 大模型返回结构化 JSON（包含 reply 回复文字 + conditions 搜索条件）
 * 4. 解析搜索条件，调用 HouseService.search() 查询房源
 * 5. 如果精确条件无结果，自动逐步放宽条件推荐相近房源
 *
 * 容错设计：
 * - API 调用失败时返回友好提示，不阻塞用户使用
 * - AI 返回的 JSON 可能被 markdown 代码块包裹，自动清洗
 * - AI 功能是增强型的，关闭后不影响系统正常运行
 */
@Service
@Slf4j
public class AiServiceImpl implements AiService {

    /** 通义千问 API Key（从配置文件注入） */
    @Value("${ai.api-key}")
    private String apiKey;

    /** 使用的模型名称，默认 qwen-plus */
    @Value("${ai.model:qwen-plus}")
    private String model;

    /** DashScope API 基础地址（OpenAI 兼容模式） */
    @Value("${ai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Autowired
    private HouseService houseService;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 系统提示词（System Prompt）
     *
     * 指导大模型的行为：
     * - 从用户自然语言中提取结构化搜索条件（省、市、区、价格、类型、户型等）
     * - 严格按 JSON 格式返回，便于程序解析
     * - 对模糊描述做合理推断（如"3000左右" → minPrice:2500, maxPrice:3500）
     * - 非找房类消息时友好回应并引导用户描述需求
     */
    private static final String SYSTEM_PROMPT =
            "你是「安居租房」平台的智能找房助手。用户会用自然语言描述租房需求，你需要：\n\n"
            + "1. 从用户的描述中提取搜索条件，返回一个JSON对象，字段包括：\n"
            + "   - province: 省份（如\"湖南省\"，没提到就不填）\n"
            + "   - city: 城市（如\"长沙市\"，注意补全\"市\"后缀）\n"
            + "   - district: 区县（如\"岳麓区\"，注意补全\"区\"后缀）\n"
            + "   - keyword: 关键词（地址、小区名等）\n"
            + "   - minPrice: 最低价格（数字）\n"
            + "   - maxPrice: 最高价格（数字，如果用户说\"2000左右\"，设为1500-2500）\n"
            + "   - houseType: 房屋类型（0=整租，1=合租，没提到就不填）\n"
            + "   - roomCount: 几室（数字，如\"两居室\"=2、\"一室一厅\"=1）\n"
            + "   - hallCount: 几厅（数字，如\"一室一厅\"=1、\"两室两厅\"=2、\"一室零厅\"=0）\n\n"
            + "2. 同时给出一段友好的回复文字，告诉用户你帮他找了什么条件的房子。\n\n"
            + "请严格按以下JSON格式返回，不要返回其他内容：\n"
            + "{\"reply\": \"回复文字\", \"conditions\": {\"province\": \"\", \"city\": \"\", \"district\": \"\", "
            + "\"keyword\": \"\", \"minPrice\": null, \"maxPrice\": null, \"houseType\": null, \"roomCount\": null, \"hallCount\": null}}\n\n"
            + "注意：\n"
            + "- 只填用户明确提到的条件，没提到的字段设为null或空字符串\n"
            + "- 价格要合理推断区间，比如\"3000左右\"→minPrice:2500,maxPrice:3500\n"
            + "- 如果用户的消息不是在找房（比如闲聊、问好），reply里友好回应并引导他描述找房需求，conditions里所有字段都为null";

    /**
     * 智能找房主入口
     *
     * @param history 多轮对话历史，格式：[{"role":"user","content":"..."}, {"role":"ai","content":"..."}, ...]
     * @param userId  用户ID（用于日志记录）
     * @return { "reply": "AI回复文字", "houses": [房源列表] }
     */
    @Override
    public Map<String, Object> smartSearch(List<Map<String, String>> history, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 调用大模型，获取自然语言解析结果
            String aiResponse = callLLM(history);
            log.info("【AI找房】用户:{}, 轮次:{}, AI返回:{}", userId, history.size(), aiResponse);

            // 2. 解析 AI 返回的 JSON
            JSONObject parsed = parseAiResponse(aiResponse);
            String reply = parsed.getString("reply");
            JSONObject conditions = parsed.getJSONObject("conditions");

            result.put("reply", reply);

            // 3. 如果提取到有效搜索条件，执行房源搜索
            if (conditions != null && hasValidCondition(conditions)) {
                List<House> houses = searchByConditions(conditions);
                result.put("houses", houses);

                // 精确条件无结果时，自动放宽条件推荐相近房源
                if (houses.isEmpty()) {
                    String relaxHint = relaxSearch(conditions, result);
                    if (relaxHint != null) {
                        result.put("reply", reply + "\n\n" + relaxHint);
                    } else {
                        result.put("reply", reply + "\n\n抱歉，暂时没有找到相关房源。");
                    }
                }
            } else {
                // AI 未提取到搜索条件（可能是闲聊），返回空列表
                result.put("houses", Collections.emptyList());
            }

        } catch (Exception e) {
            // API 调用失败的容错处理
            log.error("【AI找房】调用失败", e);
            result.put("reply", "抱歉，AI助手暂时无法响应，请稍后再试，或直接使用搜索功能找房。");
            result.put("houses", Collections.emptyList());
        }

        return result;
    }

    /**
     * 调用通义千问大模型 API
     *
     * 使用 OpenAI 兼容模式（/chat/completions），便于切换模型。
     * 消息结构：system（系统提示词）+ 多轮对话历史（user/assistant 交替）
     *
     * @param history 对话历史（前端传来的 role 为 user/ai，需转为 API 的 user/assistant）
     * @return 大模型返回的文本内容
     */
    private String callLLM(List<Map<String, String>> history) {
        String url = baseUrl + "/chat/completions";

        // 设置请求头：Bearer Token 认证
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 构建消息列表：系统提示词 + 历史对话
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示词（指导大模型行为）
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);
        messages.add(systemMsg);

        // 添加历史对话（前端 role="ai" → API role="assistant"）
        for (Map<String, String> msg : history) {
            Map<String, String> apiMsg = new HashMap<>();
            String role = msg.get("role");
            apiMsg.put("role", "ai".equals(role) ? "assistant" : "user");
            apiMsg.put("content", msg.get("content"));
            messages.add(apiMsg);
        }

        // 构建请求体
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);

        // 发送 POST 请求
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        // 从响应中提取 AI 回复内容
        JSONObject responseJson = JSON.parseObject(response.getBody());
        return responseJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }

    /**
     * 解析 AI 返回的 JSON 字符串
     * AI 有时会用 markdown 代码块包裹 JSON（```json ... ```），需要清洗
     */
    private JSONObject parseAiResponse(String aiResponse) {
        String cleaned = aiResponse.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
        }
        return JSON.parseObject(cleaned);
    }

    /**
     * 检查 AI 解析出的条件是否有至少一个有效值
     * 全部为 null 或空字符串时返回 false（表示 AI 未识别到找房意图）
     */
    private boolean hasValidCondition(JSONObject conditions) {
        for (String key : conditions.keySet()) {
            Object val = conditions.get(key);
            if (val != null && !"".equals(val.toString().trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据 AI 解析出的条件调用房源搜索
     * 最多返回6条结果（适合聊天界面展示）
     */
    private List<House> searchByConditions(JSONObject c) {
        Page<House> page = houseService.search(
                null,
                c.getString("keyword"),
                null,
                c.getString("province"),
                c.getString("city"),
                c.getString("district"),
                c.getInteger("houseType"),
                c.getInteger("roomCount"),
                c.getInteger("hallCount"),
                c.getInteger("minPrice"),
                c.getInteger("maxPrice"),
                null, null,
                1, 6
        );
        return page.getRecords();
    }

    /**
     * 逐步放宽搜索条件，尝试找到相近房源
     *
     * 约束强度：室数（roomCount）为强约束，始终保留到第4步；厅数（hallCount）为软约束，
     * 从第2步开始即可放开。
     *
     * 放宽策略（逐步执行，找到结果即停止）：
     *   第1步：扩大价格范围（±50%），其他条件全部保留
     *   第2步：放开"厅数"限制，保留原价格和其他条件
     *   第3步：放开"厅数"+"租赁方式"，扩大价格范围（仍保留室数）
     *   第4步：去掉区县限制，扩大到全城（仍保留室数）
     *   第5步：同城热门兜底（去掉所有条件，只保留城市）
     *
     * @param c      AI 解析出的原始搜索条件
     * @param result  结果Map，放宽后找到的房源会写入 result["houses"]
     * @return 放宽提示语（告诉用户做了什么调整），null 表示放宽后仍无结果
     */
    private String relaxSearch(JSONObject c, Map<String, Object> result) {
        Integer minPrice = c.getInteger("minPrice");
        Integer maxPrice = c.getInteger("maxPrice");
        Integer houseType = c.getInteger("houseType");
        Integer roomCount = c.getInteger("roomCount");
        Integer hallCount = c.getInteger("hallCount");
        String province = c.getString("province");
        String city = c.getString("city");
        String district = c.getString("district");
        String keyword = c.getString("keyword");

        // 第1步：扩大价格范围（最低价×0.5，最高价×1.5），保留室数和厅数
        if (minPrice != null || maxPrice != null) {
            Integer relaxMin = minPrice != null ? (int)(minPrice * 0.5) : null;
            Integer relaxMax = maxPrice != null ? (int)(maxPrice * 1.5) : null;
            List<House> houses = doSearch(keyword, province, city, district, houseType, roomCount, hallCount, relaxMin, relaxMax);
            if (!houses.isEmpty()) {
                result.put("houses", houses);
                return "没有找到该价位的房源，已为您扩大价格范围，推荐以下相近房源：";
            }
        }

        // 第2步：放开"厅数"限制（室数为强约束，厅数可调），保留原价格
        if (hallCount != null) {
            List<House> houses = doSearch(keyword, province, city, district, houseType, roomCount, null, minPrice, maxPrice);
            if (!houses.isEmpty()) {
                result.put("houses", houses);
                return "没有找到完全匹配的户型，已为您放宽厅数条件，推荐相同室数的房源：";
            }
        }

        // 第3步：放开"厅数"+"租赁方式"，扩大价格范围（仍保留室数）
        if (houseType != null || hallCount != null || minPrice != null || maxPrice != null) {
            Integer relaxMin = minPrice != null ? (int)(minPrice * 0.5) : null;
            Integer relaxMax = maxPrice != null ? (int)(maxPrice * 1.5) : null;
            List<House> houses = doSearch(keyword, province, city, district, null, roomCount, null, relaxMin, relaxMax);
            if (!houses.isEmpty()) {
                result.put("houses", houses);
                return "没有找到完全匹配的房源，已为您放宽价格、租赁方式和厅数，推荐以下相近房源：";
            }
        }

        // 第4步：去掉区县限制，扩大到全城（仍保留室数）
        if (district != null && !district.isEmpty()) {
            List<House> houses = doSearch(keyword, province, city, null, null, roomCount, null, null, null);
            if (!houses.isEmpty()) {
                result.put("houses", houses);
                return "该区域暂无匹配房源，已为您扩大到全城范围，推荐以下房源：";
            }
        }

        // 第5步：同城热门兜底（去掉所有条件，只保留城市）
        if (city != null && !city.isEmpty()) {
            List<House> houses = doSearch(null, province, city, null, null, null, null, null, null);
            if (!houses.isEmpty()) {
                result.put("houses", houses);
                return "暂未找到符合条件的房源，为您推荐" + city + "的热门房源：";
            }
        }

        return null; // 所有放宽策略都没找到结果
    }

    /**
     * 简化的搜索调用（封装 houseService.search 参数）
     */
    private List<House> doSearch(String keyword, String province, String city, String district,
                                 Integer houseType, Integer roomCount, Integer hallCount,
                                 Integer minPrice, Integer maxPrice) {
        Page<House> page = houseService.search(null, keyword, null, province, city, district,
                houseType, roomCount, hallCount, minPrice, maxPrice, null, null, 1, 6);
        return page.getRecords();
    }
}
