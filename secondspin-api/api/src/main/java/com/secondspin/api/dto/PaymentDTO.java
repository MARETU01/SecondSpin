package com.secondspin.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class PaymentDTO {

    private String alipayTradeNo;

    private LocalDateTime paymentTime;
}
