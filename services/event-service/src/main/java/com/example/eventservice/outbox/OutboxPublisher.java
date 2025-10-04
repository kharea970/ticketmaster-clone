package com.example.eventservice.outbox;

import com.example.eventservice.domain.Event;
import com.example.eventservice.domain.OutboxEvent;
import com.example.eventservice.repo.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OutboxPublisher {
    private final OutboxEventRepository repo;
    private final ObjectMapper om;
    public OutboxPublisher(OutboxEventRepository repo, ObjectMapper om) { this.repo = repo; this.om = om; }

    public void publishEventUpsert(Event e) {
        try {
            var doc = Map.of(
                    "id", e.getId().toString(),
                    "title", e.getTitle(),
                    "venue", e.getVenue(),
                    "city", e.getCity(),
                    "startTime", e.getStartTime(),
                    "endTime", e.getEndTime(),
                    "priceMin", e.getPriceMin(),
                    "priceMax", e.getPriceMax()
            );
            var evt = new OutboxEvent();
            evt.setAggregate("event");
            evt.setAggregateId(e.getId());
            evt.setType("EVENT_UPSERTED");
            evt.setPayload(om.valueToTree(doc));
            repo.save(evt);
        } catch (Exception ex) {
            throw new RuntimeException("serialize outbox payload", ex);
        }
    }
}

