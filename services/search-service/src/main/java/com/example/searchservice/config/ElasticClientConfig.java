package com.example.searchservice.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticClientConfig {

    @Bean
    public RestClient lowLevelRestClient(
            @Value("${app.elastic.host:http://localhost:9200}") String host
    ) {
        return RestClient.builder(HttpHost.create(host))
                .setRequestConfigCallback(rcb -> rcb
                        .setConnectTimeout(5_000) // ms
                        .setSocketTimeout(60_000) // ms
                )
                .build();
    }

    @Bean
    public ElasticsearchTransport transport(RestClient lowLevelRestClient) {
        return new RestClientTransport(lowLevelRestClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}
