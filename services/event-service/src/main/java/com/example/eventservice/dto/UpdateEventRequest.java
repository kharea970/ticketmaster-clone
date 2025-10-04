package com.example.eventservice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UpdateEventRequest(
        String title, String venue, String city, String description,
        OffsetDateTime startTime, OffsetDateTime endTime,
        BigDecimal priceMin, BigDecimal priceMax,
        Integer totalSeats, Integer availableSeats
) {}
