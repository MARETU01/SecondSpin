package com.secondspin.payment.pojo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.secondspin.payment.enums.RefundReason;
import com.secondspin.payment.enums.RefundStatus;
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
@TableName("refunds")
public class Refunds implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "refund_id", type = IdType.AUTO)
    private Integer refundId;

    private Integer orderId;

    private Integer paymentId;

    private Integer buyerId;

    private Integer sellerId;

    private BigDecimal refundAmount;

    private RefundReason refundReason;

    private String refundDescription;

    private RefundStatus status;

    private LocalDateTime createTime;

    private LocalDateTime processTime;

    private LocalDateTime completeTime;

    /**
     * 处理退款的管理员或系统用户ID
     */
    private Integer processorId;

    /**
     * 原路退回或替代退款方式
     */
    private String refundMethod;

    /**
     * 退款交易ID
     */
    private String transactionId;


}
