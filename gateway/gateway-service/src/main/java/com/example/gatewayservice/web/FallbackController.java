package com.example.gatewayservice.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {
    @GetMapping(value = "/fallback", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> fallback() {
        return Mono.just("{\"message\":\"Service temporarily unavailable\"}");
    }

    @GetMapping("/_system/ping")
    public Mono<String> ping() { return Mono.just("pong"); }
}
