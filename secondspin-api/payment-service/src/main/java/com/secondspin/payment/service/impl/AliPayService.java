package com.secondspin.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.secondspin.payment.config.AliPayConfigProperties;
import com.secondspin.common.utils.HtmlFormUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
@Slf4j
@Service
public class AliPayService {

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private AliPayConfigProperties aliPayConfigProperties;

    public String createPayment(String outTradeNo, String subject, BigDecimal totalAmount) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(aliPayConfigProperties.getReturnUrl());
        request.setNotifyUrl(aliPayConfigProperties.getNotifyUrl());

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("subject", subject);
        bizContent.put("total_amount", totalAmount.toString());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        request.setBizContent(bizContent.toString());

        // 执行请求，获取 HTML 表单字符串
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);

        String form = alipayClient.pageExecute(request).getBody();
        log.info("支付宝支付表单已生成：\n{}", form);

//        if (response == null || !response.isSuccess()) {
//            log.error("支付宝调用失败: {}", response.getSubMsg());
//            throw new AlipayApiException("支付宝调用失败：" + response.getSubMsg());
//        }
//
//        // 提取跳转 URL
//        String paymentUrl = HtmlFormUtils.extractActionUrl(response.getBody());
//        log.info("生成的支付宝支付跳转 URL: {}", paymentUrl);
//
//        return paymentUrl;
        return form;
    }
}
//public class AliPayService {
//
//    @Resource
//    private AlipayClient alipayClient;
//
//    @Resource
//    private AliPayConfigProperties aliPayConfigProperties;
//
//    public String createPayment(String out_trade_no, String subject, BigDecimal total_amount) throws AlipayApiException {
//        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
//        request.setNotifyUrl(aliPayConfigProperties.getNotifyUrl());
//        request.setReturnUrl(aliPayConfigProperties.getReturnUrl());
//
//        // 直接拼接 biz_content（和样板完全一致）
//        JSONObject bizContent = new JSONObject();
//        bizContent.put("out_trade_no", out_trade_no);          // 订单编号
//        bizContent.put("total_amount", total_amount.toString()); // 订单金额
//        bizContent.put("subject", subject);                   // 商品名称
//        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY"); // 固定配置
//
//        request.setBizContent(bizContent.toString());
//
//        // 执行请求并获取支付表单
//        String form = alipayClient.pageExecute(request).getBody();
//        log.info("支付宝支付表单已生成：\n{}", form);
//        return form;
//    }
//}