package com.example.gatewayservice.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class KeyResolverConfig {

    @Bean(name = "userKeyResolver")
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // 1) Prefer authenticated user
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("u:" + userId);
            }

            // 2) Try X-Forwarded-For if behind LB/ingress
            String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                String ip = xff.split(",")[0].trim();
                return Mono.just("ip:" + ip);
            }

            // 3) Fallback to remote address
            InetSocketAddress remote = (InetSocketAddress) exchange.getRequest().getRemoteAddress();
            String ip = "unknown";
            if (remote != null) {
                if (remote.getAddress() != null) {
                    ip = remote.getAddress().getHostAddress();
                } else {
                    ip = remote.getHostString(); // unresolved
                }
            }
            return Mono.just("ip:" + ip);
        };
    }
}
