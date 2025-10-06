package com.example.bookingservice.api;

import com.example.bookingservice.core.BookingService;
import com.example.bookingservice.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService service;

    @PostMapping("/holds")
    public ResponseEntity<HoldResponse> hold(@Valid @RequestBody HoldRequest req) {
        return ResponseEntity.ok(service.hold(req));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirm(
            @Valid @RequestBody ConfirmRequest req,
            @RequestParam List<Integer> seats,
            @RequestParam(defaultValue="0") double pricePerSeat
    ) {
        var id = service.confirm(req, seats, java.math.BigDecimal.valueOf(pricePerSeat));
        return ResponseEntity.ok(Map.of("bookingId", id));
    }

    @DeleteMapping("/holds/{holdId}")
    public ResponseEntity<Void> release(
            @PathVariable UUID holdId,
            @RequestParam UUID eventId,
            @RequestParam List<Integer> seats
    ) {
        service.release(eventId, holdId, seats);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{eventId}/snapshot")
    public ResponseEntity<SeatingSnapshot> snapshot(@PathVariable UUID eventId) {
        return ResponseEntity.ok(service.snapshot(eventId));
    }
}
