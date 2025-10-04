package com.example.eventservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateEventRequest(
        @NotBlank String title,
        @NotBlank String venue,
        @NotBlank String city,
        @NotNull OffsetDateTime startTime,
        @NotNull OffsetDateTime endTime,
        @PositiveOrZero BigDecimal priceMin,
        @PositiveOrZero BigDecimal priceMax,
        @Positive @NotNull Integer totalSeats,
        @PositiveOrZero @NotNull Integer availableSeats,
        String description
) {}
