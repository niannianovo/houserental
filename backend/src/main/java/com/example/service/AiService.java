package com.example.service;

import java.util.List;
import java.util.Map;

/**
 * AI 智能找房服务接口
 *
 * 通过大语言模型（通义千问）将用户自然语言描述转为结构化搜索条件，
 * 支持多轮对话逐步细化需求。AI 功能为增强型，关闭后不影响系统正常运行。
 */
public interface AiService {
    /**
     * 智能找房：自然语言 → LLM 解析 → 搜索条件 → 查询房源
     *
     * @param history 多轮对话历史 [{"role":"user","content":"..."},{"role":"ai","content":"..."},...]
     * @param userId  用户ID（用于日志记录）
     * @return { "reply": "AI回复文字", "houses": [匹配的房源列表] }
     */
    Map<String, Object> smartSearch(List<Map<String, String>> history, Integer userId);
}
