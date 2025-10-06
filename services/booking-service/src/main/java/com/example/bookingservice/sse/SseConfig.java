package com.example.bookingservice.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class SseConfig {
    private final SseHub hub;
    private final RedisMessageListenerContainer container;

    @jakarta.annotation.PostConstruct
    void wire() {
        container.addMessageListener(hub.redisListener(), new org.springframework.data.redis.listener.PatternTopic("seats:event:*"));
    }
}
