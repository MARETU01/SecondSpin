package com.secondspin.api.dto;

import lombok.Data;

@Data
public class AddressDTO {

    private Integer addressId;

    private Integer userId;

    private String recipientName;

    private String detailAddress;

    private String phone;
}
