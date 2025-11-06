package com.littlebook.admin.controller;

import com.littlebook.admin.dto.LoginRecordRequest;
import com.littlebook.admin.dto.SetLastLoginRequest;
import com.littlebook.admin.dto.SetTotalLoginsRequest;
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

    @GetMapping("/users/enriched")
    public List<Map<String, Object>> listUsersEnriched() {
        return statsService.listUsersEnriched();
    }

    @PostMapping("/users/{id}/login")
    public ResponseEntity<UserLoginStats> recordLogin(@PathVariable UUID id, @RequestBody LoginRecordRequest req) {
        UserLoginStats updated = statsService.recordLogin(id, req);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/users/{id}/increment")
    public ResponseEntity<UserLoginStats> incrementLogin(@PathVariable UUID id) {
        UserLoginStats updated = statsService.incrementLogin(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/users/{id}/last-login")
    public ResponseEntity<UserLoginStats> setLastLogin(@PathVariable UUID id, @RequestBody SetLastLoginRequest req) {
        UserLoginStats updated = statsService.setLastLogin(id, req);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/users/{id}/total-logins")
    public ResponseEntity<UserLoginStats> setTotalLogins(@PathVariable UUID id, @RequestBody SetTotalLoginsRequest req) {
        UserLoginStats updated = statsService.setTotalLogins(id, req);
        return ResponseEntity.ok(updated);
    }
}
