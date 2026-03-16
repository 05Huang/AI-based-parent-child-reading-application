package com.qz.sns.web.config;

import com.alibaba.fastjson.JSON;
import com.qz.sns.common.utils.JwtUtil;
import com.qz.sns.sv.config.props.JwtProperties;
import com.qz.sns.sv.session.UserSession;
import com.qz.sns.web.ws.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final JwtProperties jwtProperties;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler, JwtProperties jwtProperties) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        try {
                            if (!(request instanceof ServletServerHttpRequest)) {
                                return false;
                            }
                            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
                            System.out.println("收到 WebSocket 握手请求，uri=" + servletRequest.getRequestURI() + ", query=" + servletRequest.getQueryString());
                            String token = servletRequest.getHeader("accessToken");
                            if (token == null || token.isEmpty()) {
                                List<String> authHeaders = request.getHeaders().get("accessToken");
                                if (authHeaders != null && !authHeaders.isEmpty()) {
                                    token = authHeaders.get(0);
                                }
                            }
                            if (token == null || token.isEmpty()) {
                                token = servletRequest.getParameter("accessToken");
                            }
                            if (token == null || token.isEmpty()) {
                                System.out.println("WebSocket 握手失败：accessToken 为空");
                                return false;
                            }
                            String info = JwtUtil.getInfo(token);
                            if (info == null || info.isEmpty()) {
                                System.out.println("WebSocket 握手失败：Token 解析用户信息为空");
                                return false;
                            }
                            if (!JwtUtil.checkSign(token, jwtProperties.getAccessTokenSecret())) {
                                System.out.println("WebSocket 握手失败：Token 签名校验不通过");
                                return false;
                            }
                            UserSession session = JSON.parseObject(info, UserSession.class);
                            if (session == null || session.getUserId() == null) {
                                System.out.println("WebSocket 握手失败：UserSession 或 userId 为空");
                                return false;
                            }
                            attributes.put("userId", session.getUserId());
                            System.out.println("WebSocket 握手成功，userId=" + session.getUserId());
                            return true;
                        } catch (Exception e) {
                            System.out.println("WebSocket 握手异常：" + e.getMessage());
                            return false;
                        }
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
                    }
                })
                .setAllowedOriginPatterns("*");
    }
}
