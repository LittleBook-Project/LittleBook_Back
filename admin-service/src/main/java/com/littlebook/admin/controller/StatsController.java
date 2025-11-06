package com.littlebook.admin.controller;

import com.littlebook.admin.entity.LoginEvent;
import com.littlebook.admin.entity.ReviewActivity;
import com.littlebook.admin.entity.UserLoginStats;
import com.littlebook.admin.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/users")
    public List<UserLoginStats> listUsers() {
        return statsService.listUsers();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserLoginStats> getUser(@PathVariable UUID id) {
        return statsService.getUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/login-events")
    public List<LoginEvent> listLoginEvents() {
        return statsService.listLoginEvents();
    }

    @GetMapping("/reviews")
    public List<ReviewActivity> listReviewActivities() {
        return statsService.listReviewActivities();
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return statsService.summary();
    }
}
