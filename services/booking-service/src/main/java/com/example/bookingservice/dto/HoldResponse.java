package com.example.bookingservice.dto;

import java.time.Instant;
import java.util.UUID;

public record HoldResponse(UUID holdId, Instant expiresAt) {}
