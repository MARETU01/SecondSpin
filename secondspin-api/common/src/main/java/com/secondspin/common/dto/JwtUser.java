package com.secondspin.common.dto;

import lombok.Data;

@Data
public class JwtUser {
    private Integer userId;

    private String username;

    private String email;
}
