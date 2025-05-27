package com.secondspin.order.dto;

import com.secondspin.order.enums.OrderStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class OrderListDTO {

    private Integer orderId;

    // Product information
    private Integer productId;

    private String title;

    private String primaryImageUrl;

    private LocalDateTime createTime;

    private BigDecimal price;

    private OrderStatus status;

    private Integer payId;

    private LocalDateTime payTime;
}
