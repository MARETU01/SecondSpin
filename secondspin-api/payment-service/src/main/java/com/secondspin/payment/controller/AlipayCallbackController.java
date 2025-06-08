package com.secondspin.payment.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.secondspin.api.client.OrderClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
@Slf4j
public class AlipayCallbackController {

    private final OrderClient orderClient;

    private static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxJprgZq2ep8eYRGYCPjHNDOeskYIwmfWjiJ3cK8RoAhE/cvto2PR41WKXLKM5Sx09VzCHwyCGBHUtGLymPrFvs7L4Ab6h+ozbclWZ2W6N56anrNE3+Rci0HBdFRCAfMjDxJe/zKSh6KYFmYCtMOaLECXYtM9/su2pYYXF6srcQBubvRVasKgdvMLYxSR6AS18iPdIKp8H/XVV+vZE3SD3nwNVKWhxtsKkK1ICf+/VlqWp7cCJySgOyxamYnSJX11dG5k0IVEULC2dTEHtnGa5rEqxrI4ykjxITEz1byyp5GTiBIxAu5Or+pmTFpvpJwh/b2y5kjjALwgp+lXrZhonQIDAQAB";

    public AlipayCallbackController(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @PostMapping("/callback")
    public String payNotify(HttpServletRequest request) {
        try {
            log.info("支付回调，消息接收 {}", request.getParameter("trade_status"));

            if ("TRADE_SUCCESS".equals(request.getParameter("trade_status"))) {
                Map<String, String> params = new HashMap<>();
                request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

                String tradeNo = params.get("out_trade_no");
                String gmtPayment = params.get("gmt_payment");
                String alipayTradeNo = params.get("trade_no");
                String sign = params.get("sign");
                String content = AlipaySignature.getSignCheckContentV1(params);
                String subject = params.get("subject");
                String totalAmount = params.get("total_amount");
                String buyerId = params.get("buyer_id");
                String buyerPayAmount = params.get("buyer_pay_amount");

                if (AlipaySignature.rsa256CheckContent(content, sign, ALIPAY_PUBLIC_KEY, "UTF-8")) {
                    log.info("支付回调，交易名称: {}", subject);
                    log.info("支付回调，交易状态: {}", params.get("trade_status"));
                    log.info("支付回调，支付宝交易凭证号: {}", alipayTradeNo);
                    log.info("支付回调，商户订单号: {}", tradeNo);
                    log.info("支付回调，交易金额: {}", totalAmount);
                    log.info("支付回调，买家在支付宝唯一id: {}", buyerId);
                    log.info("支付回调，买家付款时间: {}", gmtPayment);
                    log.info("支付回调，买家付款金额: {}", buyerPayAmount);
                    log.info("支付回调，支付回调，更新订单 {}", tradeNo);

                    // orderService.changeOrderPaySuccess(tradeNo);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime paymentTime = LocalDateTime.parse(gmtPayment, formatter);
                    orderClient.payOrder(Integer.valueOf(tradeNo), Long.valueOf(alipayTradeNo), paymentTime);
                }
            }

            return "success";
        } catch (Exception e) {
            log.error("支付回调，处理失败", e);
            return "false";
        }
    }
}