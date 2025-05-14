package com.secondspin.api.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Integer userId;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String avatarUrl;

    private String accountStatus;
}
