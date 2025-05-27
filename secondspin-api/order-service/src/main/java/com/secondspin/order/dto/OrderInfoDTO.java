package com.secondspin.order.dto;

import com.secondspin.order.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class OrderInfoDTO extends OrderListDTO {

    private Integer buyerId;

    // Seller information
    private Integer sellerId;

    private String sellerName;

    private String sellerAvatarUrl;

    // Shipping address information
    private Integer shippingAddress;

    private String shippingAddressDetail;

    private OrderStatus status;
}
