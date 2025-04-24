package com.secondspin.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "user-service", fallbackFactory = UserClient.class)
public interface UserClient {

    @GetMapping("/hello")
    String hello();
}
