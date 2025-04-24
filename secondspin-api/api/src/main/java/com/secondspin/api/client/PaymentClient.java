package com.secondspin.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "payment-service", fallbackFactory = PaymentClient.class)
public interface PaymentClient {

    @GetMapping("/hello")
    String hello();
}
