package com.secondspin.order.pojo;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.secondspin.order.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.EnumTypeHandler;

/**
 * <p>
 * 
 * </p>
 *
 * @author
 * @since 2025-04-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("orders")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "order_id", type = IdType.AUTO)
    private Integer orderId;

    private Integer buyerId;

    private Integer sellerId;

    private Integer productId;

    private LocalDateTime createTime;

    private BigDecimal price;

    private Integer shippingAddress;

    private OrderStatus status;

    private String payId;

    private LocalDateTime payTime;

    private Boolean deleted;


}
