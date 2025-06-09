package com.secondspin.payment.controller;

import com.secondspin.common.utils.Result;
import com.secondspin.payment.dto.PaymentRequest;
import com.secondspin.payment.dto.PaymentResponse;
import com.secondspin.payment.service.impl.AliPayService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    private AliPayService aliPayService;

//    @PostMapping("/create")
//    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
//        try {
//            String paymentUrl = aliPayService.createPayment(
//                    request.getOut_trade_no(),
//                    request.getSubject(),
//                    request.getTotal_amount()
//            );
//
//            PaymentResponse response = new PaymentResponse();
//            response.setPaymentUrl(paymentUrl);
//            response.setOutTradeNo(request.getOut_trade_no());
//            response.setSubject(request.getSubject());
//            response.setTotalAmount(request.getTotal_amount());
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            throw new RuntimeException("创建支付失败: " + e.getMessage());
//        }
//    }

    @PostMapping("/create")
    public Result<String> createPayment(@RequestBody PaymentRequest request) {
        try {
            String payForm = aliPayService.createPayment(
                    request.getOut_trade_no(),  // 对应样板的 out_trade_no
                    request.getSubject(),       // 对应样板的 subject
                    request.getTotal_amount()   // 对应样板的 total_amount
            );
            return Result.success(payForm);
        } catch (Exception e) {
            return Result.failure("创建支付失败: " + e.getMessage());
        }
    }
}