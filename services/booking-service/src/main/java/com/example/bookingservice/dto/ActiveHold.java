package com.example.bookingservice.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ActiveHold(
        UUID holdId,
        List<Integer> seats,
        Instant expiresAt
) {}
