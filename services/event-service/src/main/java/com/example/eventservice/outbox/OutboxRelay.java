package com.example.eventservice.outbox;

import com.example.eventservice.domain.OutboxEvent;
import com.example.eventservice.repo.OutboxEventRepository;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;

@Component
public class OutboxRelay {
    private final OutboxEventRepository repo;
    private final RestClient es;

    public OutboxRelay(OutboxEventRepository repo, RestClient es) {
        this.repo = repo; this.es = es;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.interval:1000}")
    public void poll() {
        var batch = repo.findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                "PENDING", OffsetDateTime.now());
        for (var evt : batch) processOne(evt);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(OutboxEvent evt) {
        try {
            evt.setStatus("IN_PROGRESS");
            repo.save(evt);

            if ("EVENT_UPSERTED".equals(evt.getType())) {
                var id = evt.getAggregateId().toString();
                es.put()
                        .uri("/events/_doc/{id}?refresh=wait_for", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(evt.getPayload())
                        .retrieve()
                        .toBodilessEntity();
            }
            evt.setStatus("PROCESSED");
        } catch (Exception ex) {
            evt.setAttempts(evt.getAttempts() + 1);
            long delay = Math.min(300, 1L << Math.min(8, evt.getAttempts()));
            evt.setNextAttemptAt(OffsetDateTime.now().plusSeconds(delay));
            evt.setStatus("PENDING");
        }
        repo.save(evt);
    }
}

