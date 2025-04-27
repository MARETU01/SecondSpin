package com.secondspin.product.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Condition {
    NEW("new"),
    LIKE_NEW("like_new"),
    GOOD("good"),
    FAIR("fair"),
    POOR("poor");

    @EnumValue
    @JsonValue
    private final String value;

    Condition(String value) {
        this.value = value;
    }
}
