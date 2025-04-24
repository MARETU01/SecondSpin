package com.secondspin.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "product-service", fallbackFactory = ProductClient.class)
public interface ProductClient {

    @GetMapping("/hello")
    String hello();
}
