package com.secondspin.payment.controller;

import com.secondspin.common.dto.Response;
import com.secondspin.payment.dto.PaymentRequest;
import com.secondspin.payment.service.impl.AliPayService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    private AliPayService aliPayService;

    @PostMapping("/create")
    public Response<String> createPayment(@RequestBody PaymentRequest request) {
        try {
            String payForm = aliPayService.createPayment(
                    request.getOut_trade_no(),  // 对应样板的 out_trade_no
                    request.getSubject(),       // 对应样板的 subject
                    request.getTotal_amount()   // 对应样板的 total_amount
            );
            return Response.success(payForm);
        } catch (Exception e) {
            return Response.failure("创建支付失败: " + e.getMessage());
        }
    }
}