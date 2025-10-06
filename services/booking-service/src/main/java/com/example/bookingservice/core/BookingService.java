package com.example.bookingservice.core;

import com.example.bookingservice.dto.*;
import com.example.bookingservice.lock.SeatLockManager;
import com.example.bookingservice.model.Booking;
import com.example.bookingservice.model.BookingItem;
import com.example.bookingservice.repo.BookingItemRepository;
import com.example.bookingservice.repo.BookingRepository;
import com.example.bookingservice.sse.SseHub;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository repo;
    private final SeatLockManager locks;
    private final SseHub sse;
    private final BookingItemRepository itemRepo;

    @Value("${app.booking.hold-ttl-seconds:600}")
    long ttlSeconds;

    public HoldResponse hold(HoldRequest req) {
        UUID holdId = locks.tryHold(req.eventId(), req.seats());
        if (holdId == null) {
            throw new IllegalStateException("One or more seats are already held");
        }
        // publish SSE change
        sse.publish(req.eventId(), """
      {"type":"hold","holdId":"%s","seats":%s}
      """.formatted(holdId, req.seats().toString()));

        return new HoldResponse(holdId, Instant.now().plusSeconds(ttlSeconds));
    }

    @Transactional
    public UUID confirm(ConfirmRequest req, List<Integer> seats, BigDecimal pricePerSeat) {
        // In a real system call payment-service to validate paymentId here.

        var booking = Booking.builder()
                .eventId(req.eventId())
                .userId(req.userId())
                .status(Booking.Status.CONFIRMED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .totalAmount(pricePerSeat.multiply(BigDecimal.valueOf(seats.size())))
                .build();

        seats.forEach(s ->
                booking.getItems().add(BookingItem.builder()
                        .booking(booking).seatNo(s).price(pricePerSeat).build()));

        var saved = repo.save(booking);

        // Release the temporary hold keys; seats are now sold (state is in DB).
        locks.release(req.eventId(), req.holdId(), seats);

        // Notify UI
        sse.publish(req.eventId(), """
      {"type":"confirm","bookingId":"%s","seats":%s}
      """.formatted(saved.getId(), seats.toString()));

        return saved.getId();
    }

    public void release(UUID eventId, UUID holdId, List<Integer> seats) {
        locks.release(eventId, holdId, seats);
        sse.publish(eventId, """
      {"type":"release","holdId":"%s","seats":%s}
      """.formatted(holdId, seats.toString()));
    }

    public SeatingSnapshot snapshot(UUID eventId) {
        var booked = itemRepo.findConfirmedSeatNos(eventId);

        var holdsMap = locks.activeHolds(eventId);
        var holds = holdsMap.values().stream()
                .sorted(Comparator.comparing(h -> h.expiresAt() == null ? Instant.EPOCH : h.expiresAt()))
                .toList();

        return new SeatingSnapshot(eventId, booked, holds);
    }
}