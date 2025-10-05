package com.example.searchservice.api;

import com.example.searchservice.core.EventSearchService;
import com.example.searchservice.dto.EventSearchRequest;
import com.example.searchservice.dto.EventSearchResponse;
import com.example.searchservice.model.EventDoc;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final EventSearchService service;

    public SearchController(EventSearchService service) {
        this.service = service;
    }

    @GetMapping("/events")
    public ResponseEntity<EventSearchResponse> search(@Valid EventSearchRequest req) throws Exception {
        return ResponseEntity.ok(service.search(req));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventDoc> byId(@PathVariable String id) throws Exception {
        EventDoc doc = service.byId(id);
        return (doc == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(doc);
    }
}
