package com.secondspin.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RefundReason {
    BUYER_CANCELLED("buyer_cancelled"),
    SELLER_CANCELLED("seller_cancelled"),
    PRODUCT_ISSUE("product_issue"),
    SHIPPING_ISSUE("shipping_issue"),
    OTHER("other");

    @EnumValue
    @JsonValue
    private final String value;

    RefundReason(String value) {
        this.value = value;
    }
}
