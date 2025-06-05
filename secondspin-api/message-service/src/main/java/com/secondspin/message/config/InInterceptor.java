package com.secondspin.message.config;

import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.utils.JwtUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class InInterceptor implements ChannelInterceptor {

    private final UserConnectionPool userConnectionPool;

    public InInterceptor(UserConnectionPool userConnectionPool) {
        this.userConnectionPool = userConnectionPool;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        System.out.println("【入站拦截】STOMP 命令: " + accessor.getCommand());

        try {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                JwtUser user = JwtUtils.parseJwt(accessor.getFirstNativeHeader("SecondSpin"));
                userConnectionPool.addUser(user.getUserId(), accessor.getSessionId());
            } else if (StompCommand.CONNECTED.equals(accessor.getCommand())) {
                Message<?> newMessage = MessageBuilder.createMessage(
                        message.getPayload(),
                        accessor.getMessageHeaders()
                );
                System.out.println(newMessage);
                return newMessage;
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                JwtUser user = JwtUtils.parseJwt(accessor.getFirstNativeHeader("SecondSpin"));
                userConnectionPool.removeUser(user.getUserId());
            }
        } catch (Exception e) {
            System.err.println("Error processing STOMP command: " + e.getMessage());
        }
        return message;
    }
}
