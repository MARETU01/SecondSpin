package com.secondspin.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondspin.api.dto.ProductViewDTO;
import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.common.utils.Result;
import com.secondspin.product.dto.ProductInfoDTO;
import com.secondspin.product.dto.ProductListDTO;
import com.secondspin.product.pojo.Products;
import com.secondspin.product.service.IProductsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final IProductsService productsService;
    private final ObjectMapper jacksonObjectMapper;


    public ProductsController(IProductsService productsService, ObjectMapper jacksonObjectMapper) {
        this.productsService = productsService;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    @PostMapping
    public Result<Integer> postProduct(@RequestHeader("user-info") String userJson,
                              @RequestParam(value = "product") String productJson,
                              @RequestParam(value = "primary_order", required = false) Integer primaryOrder,
                              @RequestParam(value = "files", required = false) MultipartFile[] files) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        Products product = jacksonObjectMapper.readValue(productJson, Products.class).setSellerId(user.getUserId());
        try {
            return Result.success(productsService.addProduct(product, files, primaryOrder));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/home")
    public Result<PageDTO<ProductListDTO>> getHomeProducts(QueryDTO queryDTO) {
        try {
            return Result.success(productsService.getHomeProducts(queryDTO));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<ProductInfoDTO> getProduct(@PathVariable("id") Integer id, @RequestHeader(value = "user-info", required = false) String userJson) throws JsonProcessingException {
        JwtUser user = null;
        if (userJson != null) {
            user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        }
        try {
            return Result.success(productsService.getProductInfo(user, id));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/info")
    public List<ProductViewDTO> getProductView(@RequestParam("ids") List<Integer> productIds) {
        return productsService.getProductsByIdList(productIds)
                .stream()
                .map(product -> new ProductViewDTO()
                        .setProductId(product.getProductId())
                        .setTitle(product.getTitle())
                        .setPrimaryImageUrl(product.getPrimaryImageUrl()))
                .toList();
    }
}
