package com.secondspin.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    FAILED("failed"),
    REFUNDED("refunded");

    @EnumValue
    @JsonValue
    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }
}
