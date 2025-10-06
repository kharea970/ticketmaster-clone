package com.example.bookingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConfirmRequest(
        @NotNull UUID eventId,
        @NotNull UUID holdId,
        @NotBlank String userId,
        @NotBlank String paymentId   // validated against payment-service (stub)
) {}
