package com.example.eventservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false) private String title;
    @Column(nullable = false) private String venue;
    @Column(nullable = false) private String city;
    @Column(length = 2000) private String description;

    @Column(nullable = false) private OffsetDateTime startTime;
    @Column(nullable = false) private OffsetDateTime endTime;

    @Column(precision = 10, scale = 2) private BigDecimal priceMin;
    @Column(precision = 10, scale = 2) private BigDecimal priceMax;

    @Column(nullable = false) private Integer totalSeats;
    @Column(nullable = false) private Integer availableSeats;

    @Column(nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(nullable = false) private OffsetDateTime updatedAt;

    @Version private Long version;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        createdAt = now; updatedAt = now;
    }
    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }

    // getters/setters omitted for brevity
    // (or use Lombok if you prefer)
}
