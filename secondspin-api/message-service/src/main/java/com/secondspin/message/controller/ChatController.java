package com.secondspin.message.controller;

import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.utils.JwtUtils;
import com.secondspin.message.config.UserConnectionPool;
import com.secondspin.message.pojo.Messages;
import com.secondspin.message.service.IMessagesService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final UserConnectionPool userConnectionPool;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final IMessagesService messagesService;

    public ChatController(UserConnectionPool userConnectionPool, org.springframework.messaging.simp.SimpMessagingTemplate simpMessagingTemplate, IMessagesService messagesService) {
        this.userConnectionPool = userConnectionPool;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.messagesService = messagesService;
    }

    @MessageMapping("/send")
    public Messages handlePrivateMessage(Messages message, SimpMessageHeaderAccessor headerAccessor) {
        JwtUser sender = JwtUtils.parseJwt(headerAccessor.getFirstNativeHeader("SecondSpin"));
        message.setSenderId(sender.getUserId());

        Messages newMessage = messagesService.sendMessage(message);

        System.out.println(newMessage);

        if (userConnectionPool.isUserOnline(message.getReceiverId())) {
            simpMessagingTemplate.convertAndSend("/private/" + message.getReceiverId(), newMessage);
            System.out.println("消息已发送给在线用户: " + message.getReceiverId());
        }
        return newMessage;
    }
}
