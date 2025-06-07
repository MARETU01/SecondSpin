package com.secondspin.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.utils.JwtUtils;
import com.secondspin.gateway.config.AuthProperties;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final ObjectMapper jacksonObjectMapper;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public AuthGlobalFilter(AuthProperties authProperties, ObjectMapper jacksonObjectMapper) {
        this.authProperties = authProperties;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        if (isExcludedPath(path)) {
            String token = exchange.getRequest().getHeaders().getFirst("SecondSpin");
            if (token == null || token.isEmpty()) {
                return chain.filter(exchange);
            }
            JwtUser user;
            try {
                user = JwtUtils.parseJwt(token);
            } catch (ExpiredJwtException e) {
                // 构建错误响应
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                // 创建错误消息体
                String errorMessage = "{\"code\": 401, \"message\": \"JWT token expired\"}";
                byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
                DataBuffer buffer = response.bufferFactory().wrap(bytes);

                return response.writeWith(Mono.just(buffer));
            }
            String userJson = jacksonObjectMapper.writeValueAsString(user);
            ServerWebExchange newExchange = exchange.mutate()
                    .request(builder -> builder.header("user-info", userJson))
                    .build();
            return chain.filter(newExchange);
        }
        String token = exchange.getRequest().getHeaders().getFirst("SecondSpin");
        if (token == null || token.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        JwtUser user = JwtUtils.parseJwt(token);
        String userJson = jacksonObjectMapper.writeValueAsString(user);
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header("user-info", userJson))
                .build();
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private boolean isExcludedPath(String requestPath) {
        return authProperties.getExcludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }
}
