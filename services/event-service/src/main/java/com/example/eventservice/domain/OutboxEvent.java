package com.example.eventservice.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private String aggregate;
    @Column(nullable = false) private UUID aggregateId;
    @Column(nullable = false) private String type;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;
    @Column(nullable = false) private String status = "PENDING";
    @Column(nullable = false) private Integer attempts = 0;
    @Column(nullable = false) private OffsetDateTime nextAttemptAt = OffsetDateTime.now();
    @Column(nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @PreUpdate void touch() { updatedAt = OffsetDateTime.now(); }
    // getters/setters
}
