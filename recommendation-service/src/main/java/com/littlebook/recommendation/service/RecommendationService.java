package com.littlebook.recommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final RestTemplate rest;
    private final ObjectMapper mapper = new ObjectMapper();

    public RecommendationService() {
        this.rest = new RestTemplate();
    }

    public List<Map<String, Object>> recommendForUser(String userUuid, int limit) {
        try {
            // 1) fetch all reviews from review-service
            String reviewsUrl = "http://review-service:8083/api/review";
            ResponseEntity<JsonNode[]> resp = rest.getForEntity(reviewsUrl, JsonNode[].class);
            JsonNode[] reviews = resp.getBody();
            if (reviews == null) reviews = new JsonNode[0];

            // 2) compute average rating and count per book, and track user's reviewed isbns
            Map<String, List<Integer>> ratingsByIsbn = new HashMap<>();
            Set<String> userReviewedIsbns = new HashSet<>();

            for (JsonNode r : reviews) {
                String isbn = null;
                if (r.has("bookIsbn") && !r.get("bookIsbn").isNull()) isbn = r.get("bookIsbn").asText();
                if (isbn == null) continue;
                int rating = r.has("rating") && !r.get("rating").isNull() ? r.get("rating").asInt() : 0;
                ratingsByIsbn.computeIfAbsent(isbn, k -> new ArrayList<>()).add(rating);
                if (r.has("userUuid") && !r.get("userUuid").isNull()) {
                    String u = r.get("userUuid").asText();
                    if (userUuid.equals(u)) userReviewedIsbns.add(isbn);
                }
            }

            // 3) compute aggregates
            List<Map.Entry<String, Double>> avgList = ratingsByIsbn.entrySet().stream()
                    .map(e -> Map.entry(e.getKey(), e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0)))
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .collect(Collectors.toList());

            // 4) prepare top N excluding user's reviewed books
            List<Map<String, Object>> recommendations = new ArrayList<>();
            for (Map.Entry<String, Double> e : avgList) {
                if (recommendations.size() >= limit) break;
                String isbn = e.getKey();
                if (userReviewedIsbns.contains(isbn)) continue;
                // fetch book details
                try {
                    String bookUrl = "http://book-service:8084/api/book/" + isbn;
                    ResponseEntity<JsonNode> bookResp = rest.getForEntity(bookUrl, JsonNode.class);
                    JsonNode book = bookResp.getBody();
                    Map<String, Object> item = new HashMap<>();
                    item.put("isbn", isbn);
                    item.put("averageRating", e.getValue());
                    item.put("title", book != null && book.has("title") ? book.get("title").asText() : null);
                    item.put("authors", book != null && book.has("authors") ? book.get("authors").asText() : null);
                    recommendations.add(item);
                } catch (Exception ex) {
                    log.warn("Failed to fetch book {}: {}", isbn, ex.getMessage());
                }
            }

            // 5) Fallback: if we have no personalized recommendations, return top-N overall (ignore user's reviewed)
            if (recommendations.isEmpty()) {
                for (Map.Entry<String, Double> e : avgList) {
                    if (recommendations.size() >= limit) break;
                    String isbn = e.getKey();
                    try {
                        String bookUrl = "http://book-service:8084/api/book/" + isbn;
                        ResponseEntity<JsonNode> bookResp = rest.getForEntity(bookUrl, JsonNode.class);
                        JsonNode book = bookResp.getBody();
                        Map<String, Object> item = new HashMap<>();
                        item.put("isbn", isbn);
                        item.put("averageRating", e.getValue());
                        item.put("title", book != null && book.has("title") ? book.get("title").asText() : null);
                        item.put("authors", book != null && book.has("authors") ? book.get("authors").asText() : null);
                        recommendations.add(item);
                    } catch (Exception ex) {
                        log.warn("Failed to fetch book {}: {}", isbn, ex.getMessage());
                    }
                }
            }

            return recommendations;
        } catch (Exception ex) {
            log.error("Recommendation error: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }
}
