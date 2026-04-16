package com.example.controller;

import com.example.common.Result;
import com.example.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 智能找房控制器
 *
 * 接收用户的多轮对话历史，调用大模型解析自然语言需求为搜索条件，返回匹配的房源。
 * 请求体格式：{ "history": [{"role":"user","content":"..."}, ...], "userId": 123 }
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class AiController {
    @Autowired
    private AiService aiService;

    /**
     * 智能找房接口
     * @param params 包含 history（对话历史数组）和 userId（用户ID）
     * @return { "reply": "AI回复文字", "houses": [房源列表] }
     */
    @PostMapping("/search")
    @SuppressWarnings("unchecked")
    public Result<Map<String, Object>> smartSearch(@RequestBody Map<String, Object> params) {
        List<Map<String, String>> history = (List<Map<String, String>>) params.get("history");
        Integer userId = Integer.parseInt(params.getOrDefault("userId", "0").toString());
        return Result.success(aiService.smartSearch(history, userId));
    }
}
