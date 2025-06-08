package com.secondspin.api.client;

import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

import java.time.LocalDateTime;

@FeignClient(value = "order-service")
public interface OrderClient {

    @RequestLine("PUT /orders/{id}")
    Boolean payOrder(@Param("id") Integer id, Long alipayTradeNo, LocalDateTime paymentTime);
}
