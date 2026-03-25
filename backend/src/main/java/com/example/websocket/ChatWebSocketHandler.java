package com.example.websocket;

import com.example.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ChatService chatService;

    // 在线用户：userId -> session
    private static final ConcurrentHashMap<String, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getParam(session, "userId");
        if (userId != null) {
            onlineSessions.put(userId, session);
            log.info("用户{}已连接WebSocket", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // TODO 解析消息JSON，存库，转发给目标用户
        String payload = message.getPayload();
        log.info("收到消息: {}", payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getParam(session, "userId");
        if (userId != null) {
            onlineSessions.remove(userId);
            log.info("用户{}已断开WebSocket", userId);
        }
    }

    public void sendToUser(String userId, String message) {
        WebSocketSession session = onlineSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("发送消息给用户{}失败", userId, e);
            }
        }
    }

    public boolean isOnline(String userId) {
        WebSocketSession session = onlineSessions.get(userId);
        return session != null && session.isOpen();
    }

    private String getParam(WebSocketSession session, String key) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2 && kv[0].equals(key)) {
                    return kv[1];
                }
            }
        }
        return null;
    }
}
