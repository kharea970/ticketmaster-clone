package com.example.bookingservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name="booking_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingItem {
    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="booking_id", nullable=false)
    private Booking booking;

    @Column(name="seat_no", nullable=false)
    private int seatNo;

    @Column(nullable=false)
    private BigDecimal price;
}
