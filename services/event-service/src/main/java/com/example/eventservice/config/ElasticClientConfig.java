package com.example.eventservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ElasticClientConfig {
    @Bean
    RestClient elasticClient(@Value("${elastic.base-url:http://elasticsearch:9200}") String base) {
        return RestClient.builder().baseUrl(base).build();
    }
}
