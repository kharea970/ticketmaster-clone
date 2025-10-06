package com.example.bookingservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name="bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {
    @Id @UuidGenerator
    private UUID id;

    @Column(name="event_id", nullable=false)
    private UUID eventId;

    @Column(name="user_id", nullable=false)
    private String userId;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name="total_amount", nullable=false)
    private BigDecimal totalAmount;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @Version
    private long version;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookingItem> items = new ArrayList<>();

    public enum Status { PENDING, CONFIRMED, CANCELLED }
}
