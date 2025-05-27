package com.secondspin.api.client;

import com.secondspin.api.dto.ProductViewDTO;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

@FeignClient(value = "product-service")
public interface ProductClient {

    @RequestLine("GET /products/info?ids={ids}")
    List<ProductViewDTO> getProductView(@Param("ids") List<Integer> productIds);
}
