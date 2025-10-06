package com.example.bookingservice.api;

import com.example.bookingservice.sse.SseHub;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class SseController {
    private final SseHub hub;

    @GetMapping(value="/events/{eventId}/seats", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable UUID eventId) {
        return hub.subscribe(eventId);
    }
}
