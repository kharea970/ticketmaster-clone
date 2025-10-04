package com.example.eventservice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EventResponse(
        UUID id, String title, String venue, String city, String description,
        OffsetDateTime startTime, OffsetDateTime endTime,
        BigDecimal priceMin, BigDecimal priceMax,
        Integer totalSeats, Integer availableSeats
) {}
