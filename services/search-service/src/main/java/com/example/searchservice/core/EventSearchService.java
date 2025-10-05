package com.example.searchservice.core;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.searchservice.dto.EventSearchRequest;
import com.example.searchservice.dto.EventSearchResponse;
import com.example.searchservice.model.EventDoc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventSearchService {

    private final ElasticsearchClient es;
    private final String index;

    public EventSearchService(ElasticsearchClient es,
                              @Value("${app.elastic.index}") String index) {
        this.es = es;
        this.index = index;
    }

    public EventSearchResponse search(EventSearchRequest req) throws Exception {
        List<Query> must = new ArrayList<>();
        List<Query> filters = new ArrayList<>();

        // ---- full text (multi_match) or match_all
        if (req.q != null && !req.q.isBlank()) {
            must.add(MultiMatchQuery.of(m -> m
                    .query(req.q)
                    .fields("title^3", "venue^2", "city^2", "description")
                    .fuzziness("AUTO")
            )._toQuery());
        } else {
            must.add(MatchAllQuery.of(m -> m)._toQuery());
        }

        // ---- city filter
        if (req.city != null && !req.city.isBlank()) {
            filters.add(TermQuery.of(t -> t
                    .field("city.keyword")
                    .value(v -> v.stringValue(req.city))
            )._toQuery());
        }

        if (req.from != null || req.to != null) {
            String gte = (req.from != null) ? "\"gte\":\"" + req.from + "\"," : "";
            String lte = (req.to   != null) ? "\"lte\":\"" + req.to   + "\"," : "";
            String bounds = (gte + lte);
            if (bounds.endsWith(",")) bounds = bounds.substring(0, bounds.length() - 1);

            String json = """
                { "range": { "start_time": { %s } } }
            """.formatted(bounds);

            filters.add(Query.of(q -> q.withJson(new StringReader(json))));
        } else {
            String now = Instant.now().toString();
            String json = """
                { "range": { "start_time": { "gte": "%s" } } }
            """.formatted(now);

            filters.add(Query.of(q -> q.withJson(new StringReader(json))));
        }

        // ---- price range on price_min
        if (req.minPrice != null || req.maxPrice != null) {
            String gte = (req.minPrice != null) ? "\"gte\":" + req.minPrice + "," : "";
            String lte = (req.maxPrice != null) ? "\"lte\":" + req.maxPrice + "," : "";
            String bounds = (gte + lte);
            if (bounds.endsWith(",")) bounds = bounds.substring(0, bounds.length() - 1);

            String json = """
                { "range": { "price_min": { %s } } }
            """.formatted(bounds);

            filters.add(Query.of(q -> q.withJson(new StringReader(json))));
        }

        // ---- sort parsing: e.g. "start_time:asc" or "price_min:desc"
        String sortField = "start_time";
        SortOrder sortOrder = SortOrder.Asc;
        if (req.sort != null && !req.sort.isBlank() && req.sort.contains(":")) {
            String[] p = req.sort.split(":");
            if (p.length == 2) {
                sortField = p[0];
                sortOrder = "desc".equalsIgnoreCase(p[1]) ? SortOrder.Desc : SortOrder.Asc;
            }
        }

        int from = Math.max(0, req.page) * Math.max(1, req.size);
        int size = Math.max(1, req.size);

        // capture into final vars for lambdas
        final String finalSortField = sortField;
        final SortOrder finalSortOrder = sortOrder;
        final int finalFrom = from;
        final int finalSize = size;

        // ---- build search request
        SearchRequest sreq = SearchRequest.of(s -> s
                .index(index)
                .from(finalFrom)
                .size(finalSize)
                .query(q -> q.bool(b -> b.must(must).filter(filters)))
                .sort(so -> so.field(f -> f.field(finalSortField).order(finalSortOrder)))
        );

        SearchResponse<EventDoc> sres = es.search(sreq, EventDoc.class);
        long total = (sres.hits().total() == null) ? 0L : sres.hits().total().value();

        return new EventSearchResponse(
                total,
                sres.took(),
                sres.hits().hits().stream()
                        .map(h -> {
                            EventDoc d = h.source();
                            if (d != null && d.id == null) {
                                d.id = h.id(); // ensure EventDoc has a public 'id' or a setter
                            }
                            return d;
                        })
                        .toList()
        );
    }

    public EventDoc byId(String id) throws Exception {
        var res = es.get(g -> g.index(index).id(id), EventDoc.class);
        return res.found() ? res.source() : null;
    }
}
