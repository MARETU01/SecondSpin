package com.secondspin.api.client;

import com.secondspin.api.dto.AddressDTO;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "user-service")
public interface UserClient {

    @RequestLine("GET /address/{id}")
    AddressDTO getAddress(@Param("id") Long id);
}
