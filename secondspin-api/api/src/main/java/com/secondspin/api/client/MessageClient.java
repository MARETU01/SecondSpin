package com.secondspin.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "message-service", fallbackFactory = MessageClient.class)
public interface MessageClient {

    @GetMapping("/hello")
    String hello();
}
