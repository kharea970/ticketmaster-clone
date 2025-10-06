package com.example.bookingservice.dto;

import java.util.List;
import java.util.UUID;

public record SeatingSnapshot(
        UUID eventId,
        List<Integer> booked,          // seats already sold
        List<ActiveHold> holds         // current temporary holds
) {}
