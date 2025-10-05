package com.example.searchservice.model;

import java.time.Instant;

public class EventDoc {
    public String id;
    public String title;
    public String venue;
    public String city;
    public String description;
    public Instant start_time;
    public Instant end_time;
    public Double price_min;
    public Double price_max;
    public Integer total_seats;
    public Integer available_seats;
}
