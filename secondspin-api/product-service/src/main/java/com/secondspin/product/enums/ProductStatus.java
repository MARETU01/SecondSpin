package com.secondspin.product.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ProductStatus {
    AVAILABLE("available"),
    RESERVED("reserved"),
    SOLD("sold"),
    REMOVED("removed");

    @EnumValue
    @JsonValue
    private final String value;

    ProductStatus(String value) {
        this.value = value;
    }
}
