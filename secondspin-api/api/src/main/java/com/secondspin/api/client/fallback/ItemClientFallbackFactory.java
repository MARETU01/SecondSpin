package com.secondspin.api.client.fallback;

import com.secondspin.api.client.TestClient;
import org.springframework.cloud.openfeign.FallbackFactory;


public class ItemClientFallbackFactory implements FallbackFactory<TestClient> {
    @Override
    public TestClient create(Throwable cause) {
        return new TestClient() {
            @Override
            public String hello() {
                return "error";
            }
        };
    }
}
