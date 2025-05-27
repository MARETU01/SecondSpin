package com.secondspin.order.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.common.utils.Result;
import com.secondspin.order.dto.OrderListDTO;
import com.secondspin.order.pojo.Orders;
import com.secondspin.order.service.IOrdersService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
