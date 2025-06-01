package com.secondspin.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RefundMethod {
    ORIGINAL("original"),
    ALTERNATIVE("alternative");

    @EnumValue
    @JsonValue
    private final String value;

    RefundMethod(String value) {
        this.value = value;
    }
}
