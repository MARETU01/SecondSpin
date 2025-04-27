package com.secondspin.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("pending"),
    PAID("paid"),
    SHIPPED("shipped"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    REFUNDED("refunded");

    @EnumValue
    @JsonValue
    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }
}
