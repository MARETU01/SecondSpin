package com.secondspin.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "test-service", fallbackFactory = TestClient.class)
public interface TestClient {

    @GetMapping("/hello")
    String hello();
}
