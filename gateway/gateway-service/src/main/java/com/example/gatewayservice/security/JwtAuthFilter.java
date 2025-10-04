package com.example.gatewayservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;


@Component
public class JwtAuthFilter implements GlobalFilter {

    private final byte[] secret;
    private static final AntPathMatcher matcher = new AntPathMatcher();

    // Endpoints that don't require auth
    private static final List<String> PUBLIC_PATHS = List.of(
            "/actuator/**",
            "/_system/**",
            "/api/events/search/**",
            "/api/events/{id}"  // viewing events is public; adjust as needed
    );

    public JwtAuthFilter(@Value("${security.jwt.secret:change-me}") String secret) {
        this.secret = Base64.getEncoder().encode(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var path = exchange.getRequest().getPath().value();
        boolean publicPath = PUBLIC_PATHS.stream().anyMatch(p -> matcher.match(p, path));
        var auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (publicPath) return chain.filter(exchange);
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = auth.substring(7);
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userId = claims.containsKey("userId") ? String.valueOf(claims.get("userId")) : claims.getSubject();
        String roles = claims.containsKey("roles") ? String.valueOf(claims.get("roles")) : "";

        ServerHttpRequest req = exchange.getRequest().mutate()
                .header("X-User-Id", userId == null ? "" : userId)
                .header("X-User-Roles", roles)
                .build();

        return chain.filter(exchange.mutate().request(req).build());
    }
}
