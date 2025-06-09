package com.secondspin.message.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.utils.Result;
import com.secondspin.message.dto.MessagePreviewDTO;
import com.secondspin.message.pojo.Messages;
import com.secondspin.message.service.IMessagesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessagesController {

    private final IMessagesService messagesService;
    private final ObjectMapper jacksonObjectMapper;

    public MessagesController(IMessagesService messagesService, ObjectMapper jacksonObjectMapper) {
        this.messagesService = messagesService;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    @GetMapping("/all")
    public Result<List<MessagePreviewDTO>> getAllMessages(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            return Result.success(messagesService.getMessagePreviews(user.getUserId()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public Result<List<Messages>> getMessagesByUserId(@RequestHeader("user-info") String userJson,
                                                      @PathVariable("userId") Integer userId) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            List<Messages> messages = messagesService.getMessagesWithUserId(user, userId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/unread")
    public Result<Integer> getUnreadMessages(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            return Result.success(messagesService.getUnreadMessage(user.getUserId()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping
    public Result<Messages> sendMessage(@RequestHeader("user-info") String userJson,
                                        @RequestBody Messages message) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            message.setSenderId(user.getUserId())
                    .setContent("Hello!");
            Messages sentMessage = messagesService.sendMessage(message);
            return Result.success(sentMessage);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
