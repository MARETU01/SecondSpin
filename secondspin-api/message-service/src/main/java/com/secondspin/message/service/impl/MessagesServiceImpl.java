package com.secondspin.message.service.impl;

import com.secondspin.message.pojo.Messages;
import com.secondspin.message.mapper.MessagesMapper;
import com.secondspin.message.service.IMessagesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
@Service
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper, Messages> implements IMessagesService {

}
