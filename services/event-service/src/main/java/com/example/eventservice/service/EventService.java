package com.example.eventservice.service;

import com.example.eventservice.domain.Event;
import com.example.eventservice.dto.CreateEventRequest;
import com.example.eventservice.dto.EventResponse;
import com.example.eventservice.dto.UpdateEventRequest;
import com.example.eventservice.outbox.OutboxPublisher;
import com.example.eventservice.repo.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EventService {
    private final EventRepository repo;
    private final OutboxPublisher outboxPublisher;
    public EventService(EventRepository repo, OutboxPublisher outboxPublisher) { this.repo = repo;
        this.outboxPublisher = outboxPublisher;
    }

    @Transactional
    public EventResponse create(CreateEventRequest r) {
        var e = new Event();
        e.setTitle(r.title()); e.setVenue(r.venue()); e.setCity(r.city());
        e.setDescription(r.description());
        e.setStartTime(r.startTime()); e.setEndTime(r.endTime());
        e.setPriceMin(r.priceMin()); e.setPriceMax(r.priceMax());
        e.setTotalSeats(r.totalSeats()); e.setAvailableSeats(r.availableSeats());
        var saved = repo.save(e);
        outboxPublisher.publishEventUpsert(saved);
        return toResp(saved);
    }

    @Transactional(readOnly = true)
    public EventResponse get(UUID id) {
        return repo.findById(id).map(this::toResp)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
    }

    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest r) {
        var e = repo.findById(id).orElseThrow();
        if (r.title()!=null) e.setTitle(r.title());
        if (r.venue()!=null) e.setVenue(r.venue());
        if (r.city()!=null) e.setCity(r.city());
        if (r.description()!=null) e.setDescription(r.description());
        if (r.startTime()!=null) e.setStartTime(r.startTime());
        if (r.endTime()!=null) e.setEndTime(r.endTime());
        if (r.priceMin()!=null) e.setPriceMin(r.priceMin());
        if (r.priceMax()!=null) e.setPriceMax(r.priceMax());
        if (r.totalSeats()!=null) e.setTotalSeats(r.totalSeats());
        if (r.availableSeats()!=null) e.setAvailableSeats(r.availableSeats());
        outboxPublisher.publishEventUpsert(e);
        return toResp(e);
    }

    private EventResponse toResp(Event e) {
        return new EventResponse(
                e.getId(), e.getTitle(), e.getVenue(), e.getCity(), e.getDescription(),
                e.getStartTime(), e.getEndTime(), e.getPriceMin(), e.getPriceMax(),
                e.getTotalSeats(), e.getAvailableSeats()
        );
    }
}
