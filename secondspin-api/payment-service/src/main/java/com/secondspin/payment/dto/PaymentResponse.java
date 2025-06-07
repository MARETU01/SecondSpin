package com.secondspin.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentUrl;       // 支付宝跳转链接
    private String outTradeNo;       // 订单号
    private String subject;          // 商品名称
    private BigDecimal totalAmount;  // 支付金额
}