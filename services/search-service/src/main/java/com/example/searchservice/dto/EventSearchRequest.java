package com.example.searchservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSearchRequest {
    public String q;
    public String city;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public Instant from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public Instant to;
    public Double minPrice;
    public Double maxPrice;

    @Min(0) public int page = 0;
    @Min(1) @Max(200) public int size = 20;
    public String sort = "start_time:asc";
}
