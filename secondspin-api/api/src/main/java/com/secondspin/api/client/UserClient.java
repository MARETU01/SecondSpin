package com.secondspin.api.client;

import com.secondspin.api.dto.AddressDTO;
import com.secondspin.api.dto.UserDTO;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "user-service")
public interface UserClient {

    @RequestLine("GET /users/{id}")
    UserDTO getUser(@Param("id") Long id);

    @RequestLine("GET /address/{id}")
    AddressDTO getAddress(@Param("id") Long id);
}
