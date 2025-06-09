package com.secondspin.api.client;

import com.secondspin.api.dto.PaymentDTO;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;

@FeignClient(value = "order-service")
public interface OrderClient {

    @RequestLine("PUT /orders/{id}")
    Boolean payOrder(@Param("id") Integer id, @RequestBody PaymentDTO paymentDTO);
}
