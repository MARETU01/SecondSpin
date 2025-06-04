package com.secondspin.payment.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Data
@ConfigurationProperties(prefix = "alipay", ignoreInvalidFields = true)
public class AliPayConfigProperties {
    private boolean enabled;
    @Value("${alipay.app_id}")
    private String appId;
    @Value("${alipay.merchant_private_key}")
    private String merchantPrivateKey;
    @Value("${alipay.alipay_public_key}")
    private String alipayPublicKey;
    @Value("${alipay.notify_url}")
    private String notifyUrl;
    @Value("${alipay.return_url}")
    private String returnUrl;
    @Value("${alipay.gatewayUrl}")
    private String gatewayUrl;
    @Value("${alipay.sign_type}")
    private String signType;
    @Value("${alipay.charset}")
    private String charset;
}