package com.secondspin.order.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondspin.api.dto.PaymentDTO;
import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.common.utils.Result;
import com.secondspin.order.dto.OrderListDTO;
import com.secondspin.order.enums.OrderStatus;
import com.secondspin.order.pojo.Orders;
import com.secondspin.order.service.IOrdersService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final IOrdersService ordersService;
    private final ObjectMapper jacksonObjectMapper;

    public OrderController(IOrdersService ordersService, ObjectMapper jacksonObjectMapper) {
        this.ordersService = ordersService;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    @GetMapping
    public Result<PageDTO<OrderListDTO>> getOrders(@RequestHeader("user-info") String userJson,
                                                   QueryDTO queryDTO) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            return Result.success(ordersService.getOrders(user.getUserId(), queryDTO));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping
    public Result<Orders> createOrder(@RequestHeader("user-info") String userJson,
                                       @RequestBody Orders order) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            return Result.success(ordersService.createOrder(user.getUserId(), order));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PutMapping
    public Result<Orders> updateOrder(@RequestHeader("user-info") String userJson,
                                       @RequestBody Orders order) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            return Result.success(ordersService.updateOrder(user.getUserId(), order));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Boolean payOrder(@PathVariable Integer id, @RequestBody PaymentDTO paymentDTO) {
        return ordersService
                .lambdaUpdate()
                .eq(Orders::getOrderId, id)
                .set(Orders::getStatus, OrderStatus.SHIPPED)
                .set(Orders::getPayId, paymentDTO.getAlipayTradeNo())
                .set(Orders::getPayTime, paymentDTO.getPaymentTime())
                .update();
    }
}
