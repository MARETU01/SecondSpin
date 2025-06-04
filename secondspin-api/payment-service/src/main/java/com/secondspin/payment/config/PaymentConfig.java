package com.secondspin.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
@EnableConfigurationProperties(AliPayConfigProperties.class)
public class PaymentConfig {

    @Bean(name = "alipayClient")
    @ConditionalOnProperty(value = "alipay.enabled", havingValue = "true")
    public AlipayClient alipayClient(AliPayConfigProperties properties) {
        // 初始化 AlipayClient
        AlipayClient client = new DefaultAlipayClient(
                properties.getGatewayUrl(),
                properties.getAppId(),
                properties.getMerchantPrivateKey(),  // 商户私钥
                "json",
                properties.getCharset(),
                properties.getAlipayPublicKey(),     // 支付宝公钥
                properties.getSignType()
        );

        // 打印密钥（用于调试）
        System.out.println("支付宝公钥"+properties.getAlipayPublicKey());
        System.out.println("商户私钥"+properties.getMerchantPrivateKey());

        return client;
    }
}