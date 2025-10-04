package com.example.gatewayservice.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {
    @Bean(name = "userRateLimiter")
    public RedisRateLimiter userRateLimiter() {
        return new RedisRateLimiter(10, 20,1);
    }
}
