package com.secondspin.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "order-service", fallbackFactory = OrderClient.class)
public interface OrderClient {

    @GetMapping("/hello")
    String hello();
}
