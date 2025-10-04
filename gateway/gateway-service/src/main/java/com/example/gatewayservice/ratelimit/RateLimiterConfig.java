package com.example.gatewayservice.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {
    // 10 tokens/sec, burst up to 20 by default (tune per route if needed)
    @Bean(name = "userRateLimiter")
    public RedisRateLimiter userRateLimiter() {
        return new RedisRateLimiter(10, 20);
    }
}
