package com.example.ecommerce.common;

import com.example.ecommerce.mapper.ConsultationMapper;
import com.example.ecommerce.model.CustomerConsultation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("!test")
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> userNames = new ConcurrentHashMap<>();
    private final ConsultationMapper consultationMapper;
    private final ObjectMapper om = new ObjectMapper();

    public ChatWebSocketHandler(ConsultationMapper consultationMapper) {
        this.consultationMapper = consultationMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = extractUserId(session);
        sessions.put(userId, session);
        userNames.put(userId, "用户" + userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = extractUserId(session);
        String payload = message.getPayload();
        @SuppressWarnings("unchecked")
        var msg = om.readValue(payload, Map.class);
        String content = String.valueOf(msg.get("content"));
        String to = msg.get("to") != null ? String.valueOf(msg.get("to")) : "admin";

        // 保存到数据库
        CustomerConsultation c = new CustomerConsultation();
        c.setUserId(Long.valueOf(userId));
        c.setSubject("在线咨询");
        c.setContent(content);
        c.setStatus("PENDING");
        c.setCreatedAt(LocalDateTime.now());
        consultationMapper.insert(c);

        // 发送给目标用户或广播
        String response = om.writeValueAsString(Map.of(
            "from", userId,
            "fromName", userNames.getOrDefault(userId, "用户" + userId),
            "content", content,
            "time", LocalDateTime.now().toString()
        ));

        if (sessions.containsKey(to)) {
            sessions.get(to).sendMessage(new TextMessage(response));
        }
        session.sendMessage(new TextMessage(response));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = extractUserId(session);
        sessions.remove(userId);
        userNames.remove(userId);
    }

    private String extractUserId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
