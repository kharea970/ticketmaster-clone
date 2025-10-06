package com.example.bookingservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record HoldRequest(
        @NotNull UUID eventId,
        @NotBlank String userId,
        @NotEmpty List<@Min(1) Integer> seats,
        @NotNull BigDecimal pricePerSeat
) {}
