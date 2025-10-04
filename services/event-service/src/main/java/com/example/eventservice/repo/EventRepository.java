package com.example.eventservice.repo;

import com.example.eventservice.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByCityIgnoreCaseAndStartTimeBetween(String city,
                                                        OffsetDateTime from, OffsetDateTime to);
    List<Event> findByTitleContainingIgnoreCase(String q);
}
