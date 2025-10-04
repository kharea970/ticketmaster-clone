package com.example.eventservice.web;

import com.example.eventservice.dto.CreateEventRequest;
import com.example.eventservice.dto.EventResponse;
import com.example.eventservice.dto.UpdateEventRequest;
import com.example.eventservice.service.EventService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService svc;
    public EventController(EventService svc) { this.svc = svc; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody CreateEventRequest req) { return svc.create(req); }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable UUID id) { return svc.get(id); }

    @PatchMapping("/{id}")
    public EventResponse update(@PathVariable UUID id, @RequestBody UpdateEventRequest req) {
        return svc.update(id, req);
    }
}
