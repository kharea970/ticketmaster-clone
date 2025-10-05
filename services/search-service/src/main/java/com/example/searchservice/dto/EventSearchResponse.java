package com.example.searchservice.dto;

import com.example.searchservice.model.EventDoc;

import java.util.List;

public record EventSearchResponse(
        long total,
        long tookMs,
        List<EventDoc> hits
) {}
