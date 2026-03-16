package com.qz.sns.web.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Object uid = session.getAttributes().get("userId");
        if (!(uid instanceof Long)) {
            System.out.println("WebSocket 连接建立失败，未找到 userId 属性");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("unauthorized"));
            return;
        }
        Long userId = (Long) uid;
        System.out.println("WebSocket 连接建立成功，userId=" + userId + "，sessionId=" + session.getId());
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Object uid = session.getAttributes().get("userId");
        if (uid instanceof Long) {
            Long userId = (Long) uid;
            Set<WebSocketSession> set = userSessions.get(userId);
            if (set != null) {
                set.remove(session);
                if (set.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        } else {
            for (Set<WebSocketSession> set : userSessions.values()) {
                set.remove(session);
            }
        }
    }

    public void sendToUser(Long userId, Map<String, Object> payload) {
        if (userId == null || payload == null) {
            return;
        }
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            System.out.println("sendToUser: userId=" + userId + " 当前无 WebSocket 会话，payload=" + payload);
            return;
        }
        try {
            String text = objectMapper.writeValueAsString(payload);
            System.out.println("sendToUser: 向 userId=" + userId + " 推送消息，sessionCount=" + sessions.size() + "，内容=" + text);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(text));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToUsers(Collection<Long> userIds, Map<String, Object> payload) {
        if (userIds == null || userIds.isEmpty() || payload == null) {
            return;
        }
        System.out.println("sendToUsers: 目标用户=" + userIds + "，payload=" + payload);
        Set<WebSocketSession> targets = new HashSet<>();
        for (Long userId : userIds) {
            if (userId == null) {
                continue;
            }
            Set<WebSocketSession> set = userSessions.get(userId);
            if (set != null && !set.isEmpty()) {
                targets.addAll(set);
            }
        }
        if (targets.isEmpty()) {
            System.out.println("sendToUsers: 未找到任何在线 WebSocket 会话");
            return;
        }
        try {
            String text = objectMapper.writeValueAsString(payload);
            System.out.println("sendToUsers: 向 " + targets.size() + " 个会话推送群聊消息，内容=" + text);
            for (WebSocketSession session : targets) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(text));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
