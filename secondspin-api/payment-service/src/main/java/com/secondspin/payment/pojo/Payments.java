package com.secondspin.payment.pojo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.secondspin.payment.enums.PaymentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@TableName("payments")
public class Payments implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "payment_id", type = IdType.AUTO)
    private Integer paymentId;

    private Integer orderId;

    private Integer payerId;

    private String transactionId;

    private BigDecimal amount;

    private LocalDateTime createTime;

    private PaymentStatus status;

    private LocalDateTime payTime;

    private Integer refundId;


}
