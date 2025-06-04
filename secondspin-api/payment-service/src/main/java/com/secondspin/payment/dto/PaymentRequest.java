package com.secondspin.payment.dto;



import lombok.Data;
import java.math.BigDecimal;


@Data
public class PaymentRequest {
    private String out_trade_no;  // 订单编号
    private String subject;       //
    private BigDecimal total_amount; //
}