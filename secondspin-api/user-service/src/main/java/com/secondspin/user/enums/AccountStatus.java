package com.secondspin.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AccountStatus {
    ACTIVE("active"),
    SUSPENDED("suspended"),
    BANNED("banned");

    @EnumValue
    @JsonValue
    private final String value;

    AccountStatus(String value) {
        this.value = value;
    }
}
