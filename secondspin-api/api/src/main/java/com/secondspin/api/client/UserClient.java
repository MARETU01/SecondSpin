package com.secondspin.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "user-service")
public interface UserClient {

    @GetMapping("/hello")
    String hello();
}
