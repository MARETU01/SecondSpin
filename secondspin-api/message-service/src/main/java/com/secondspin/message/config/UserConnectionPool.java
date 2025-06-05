package com.secondspin.message.config;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserConnectionPool {
    // 使用ConcurrentHashMap存储用户与session的映射
    private final ConcurrentHashMap<Integer, String> onlineUsers = new ConcurrentHashMap<>();

    // 添加用户到连接池
    public void addUser(Integer userId, String sessionId) {
        onlineUsers.put(userId, sessionId);
        System.out.println("用户上线: " + userId + ", 当前在线: " + onlineUsers.size());
    }

    // 从连接池移除用户
    public void removeUser(Integer userId) {
        onlineUsers.remove(userId);
        System.out.println("用户下线: " + userId + ", 剩余在线: " + onlineUsers.size());
    }

    // 检查用户是否在线
    public boolean isUserOnline(Integer userId) {
        return onlineUsers.containsKey(userId);
    }

    // 获取在线用户列表
    public ConcurrentHashMap<Integer, String> getOnlineUsers() {
        return onlineUsers;
    }
}