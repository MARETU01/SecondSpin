package com.secondspin.message.service.impl;

import com.secondspin.api.client.ProductClient;
import com.secondspin.api.client.UserClient;
import com.secondspin.api.dto.ProductViewDTO;
import com.secondspin.api.dto.UserDTO;
import com.secondspin.common.dto.JwtUser;
import com.secondspin.message.dto.MessagePreviewDTO;
import com.secondspin.message.pojo.Messages;
import com.secondspin.message.mapper.MessagesMapper;
import com.secondspin.message.service.IMessagesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private final ProductClient productClient;
    private final UserClient userClient;

    public MessagesServiceImpl(ProductClient productClient, UserClient userClient) {
        this.productClient = productClient;
        this.userClient = userClient;
    }

    @Override
    public Messages sendMessage(Messages message) {
        save(message);
        return getById(message.getMessageId());
    }

    @Override
    public List<MessagePreviewDTO> getMessagePreviews(Integer userId) {
        List<Messages> allMessages = lambdaQuery()
                .eq(Messages::getSenderId, userId)
                .or()
                .eq(Messages::getReceiverId, userId)
                .list();

        if (allMessages.isEmpty()) {
            return List.of();
        }

        Map<Integer, Messages> latestMessagesByProduct = allMessages.stream()
                .collect(Collectors.groupingBy(
                        Messages::getProductId,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(Messages::getSendTime)),
                                opt -> opt.orElse(null)
                        )
                ));

        Map<Integer, Long> unreadCountMap = allMessages.stream()
                .filter(msg -> msg.getReceiverId().equals(userId))
                .filter(msg -> !msg.getReadStatus())
                .collect(Collectors.groupingBy(
                        Messages::getProductId,
                        Collectors.counting()
                ));

        Set<Integer> productIds = latestMessagesByProduct.keySet();
        Set<Integer> relatedUserIds = latestMessagesByProduct.values().stream()
                .map(msg -> msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId())
                .collect(Collectors.toSet());

        List<ProductViewDTO> productViewDTOS = productClient.getProductView(new ArrayList<>(productIds));
        List<UserDTO> userInfos = userClient.getUsersInfo(new ArrayList<>(relatedUserIds));

        Map<Integer, ProductViewDTO> productMap = productViewDTOS.stream()
                .collect(Collectors.toMap(ProductViewDTO::getProductId, Function.identity()));
        Map<Integer, UserDTO> userMap = userInfos.stream()
                .collect(Collectors.toMap(UserDTO::getUserId, Function.identity()));

        return latestMessagesByProduct.values().stream()
                .map(message -> {
                    Integer otherUserId = message.getSenderId().equals(userId) ?
                            message.getReceiverId() : message.getSenderId();
                    ProductViewDTO product = productMap.get(message.getProductId());
                    UserDTO user = userMap.get(otherUserId);

                    return new MessagePreviewDTO()
                            .setPersonId(otherUserId)
                            .setPersonName(user != null ? user.getUsername() : null)
                            .setPersonAvatar(user != null ? user.getAvatarUrl() : null)
                            .setLastMessageContent(message.getContent())
                            .setLastMessageTime(message.getSendTime())
                            .setUnreadCount(unreadCountMap.getOrDefault(message.getProductId(), 0L).intValue())
                            .setProductTitle(product != null ? product.getTitle() : null)
                            .setProductImage(product != null ? product.getPrimaryImageUrl() : null);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Messages> getMessagesWithUserId(JwtUser user, Integer userId) {
        List<Messages> messages = lambdaQuery()
                .eq(Messages::getSenderId, user.getUserId())
                .eq(Messages::getReceiverId, userId)
                .or()
                .eq(Messages::getSenderId, userId)
                .eq(Messages::getReceiverId, user.getUserId())
                .orderByAsc(Messages::getSendTime)
                .list();
        lambdaUpdate()
                .eq(Messages::getReceiverId, user.getUserId())
                .eq(Messages::getSenderId, userId)
                .set(Messages::getReadStatus, true)
                .update();
        return messages;
    }
}
