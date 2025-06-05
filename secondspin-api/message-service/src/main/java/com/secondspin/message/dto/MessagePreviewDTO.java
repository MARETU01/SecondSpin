package com.secondspin.message.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class MessagePreviewDTO {

    private Integer personId;

    private String personName;

    private String personAvatar;

    private String productTitle;

    private String productImage;

    private Integer unreadCount;

    private String lastMessageContent;

    private LocalDateTime lastMessageTime;
}
