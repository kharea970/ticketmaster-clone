package com.example.gatewayservice.filters;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter {
    private static final String HDR = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String id = exchange.getRequest().getHeaders().getFirst(HDR);
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();

        ServerHttpRequest req = exchange.getRequest().mutate().header(HDR, id).build();
        exchange.getResponse().getHeaders().set(HDR, id);
        return chain.filter(exchange.mutate().request(req).build());
    }
}
