package com.secondspin.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RefundStatus {
    PENDING("requested"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    REJECTED("rejected"),
    FAILED("failed");

    @EnumValue
    @JsonValue
    private final String value;

    RefundStatus(String value) {
        this.value = value;
    }
}
