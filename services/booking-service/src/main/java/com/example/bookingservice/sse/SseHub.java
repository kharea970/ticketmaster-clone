package com.example.bookingservice.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

import static com.example.bookingservice.config.RedisConfig.CHANNEL_PREFIX;

@Component
@RequiredArgsConstructor
public class SseHub {

    private final StringRedisTemplate redis;
    private final Map<UUID, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Value("${app.sse.heartbeat-seconds:15}")
    private long heartbeat;

    public SseEmitter subscribe(UUID eventId) {
        var emitter = new SseEmitter(Duration.ofSeconds(heartbeat * 2).toMillis());
        emitters.computeIfAbsent(eventId, k -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> remove(eventId, emitter));
        emitter.onTimeout(() -> remove(eventId, emitter));
        // first heartbeat
        sendSafe(emitter, SseEmitter.event().name("hello").data("ok"));

        // subscribe to Redis channel for this event (idempotent)
        redis.convertAndSend(channel(eventId), "{\"type\":\"ping\"}");

        return emitter;
    }

    public void publish(UUID eventId, String json) {
        redis.convertAndSend(channel(eventId), json);
        // also push to local emitters
        emitters.getOrDefault(eventId, Set.of()).forEach(e -> sendSafe(e, SseEmitter.event().name("update").data(json)));
    }

    public MessageListener redisListener() {
        return (message, pattern) -> {
            String channel = new String(message.getChannel());
            String payload = new String(message.getBody());
            if (channel.startsWith(CHANNEL_PREFIX)) {
                UUID eventId = UUID.fromString(channel.substring(CHANNEL_PREFIX.length()));
                emitters.getOrDefault(eventId, Set.of())
                        .forEach(e -> sendSafe(e, SseEmitter.event().name("update").data(payload)));
            }
        };
    }

    private String channel(UUID eventId) { return CHANNEL_PREFIX + eventId; }

    private void remove(UUID eventId, SseEmitter e) {
        var set = emitters.get(eventId);
        if (set != null) set.remove(e);
    }

    private void sendSafe(SseEmitter e, SseEmitter.SseEventBuilder ev) {
        try { e.send(ev); } catch (IOException ignored) { }
    }
}
