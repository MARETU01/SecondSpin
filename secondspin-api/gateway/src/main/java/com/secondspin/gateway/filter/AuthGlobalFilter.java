package com.secondspin.gateway.filter;

import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.utils.JwtUtils;
import com.secondspin.gateway.config.AuthProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final PathMatcher pathMatcher = new AntPathMatcher();

    public AuthGlobalFilter(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }
        String token = exchange.getRequest().getHeaders().getFirst("SecondSpin");
        if (token == null || token.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        JwtUser user = JwtUtils.parseJwt(token);
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header("user-info", user.toString()))
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
