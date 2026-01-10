package com.littlebook.recommendation.controller;

import com.littlebook.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping("/user/{userUuid}")
    public ResponseEntity<?> recommendForUser(@PathVariable String userUuid, @org.springframework.web.bind.annotation.RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(service.recommendForUser(userUuid, size));
    }
}
