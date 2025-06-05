package com.secondspin.message.service;

import com.secondspin.common.dto.JwtUser;
import com.secondspin.message.dto.MessagePreviewDTO;
import com.secondspin.message.pojo.Messages;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
public interface IMessagesService extends IService<Messages> {

    Messages sendMessage(Messages message);

    List<MessagePreviewDTO> getMessagePreviews(Integer userId);

    List<Messages> getMessagesWithUserId(JwtUser user, Integer userId);
}
