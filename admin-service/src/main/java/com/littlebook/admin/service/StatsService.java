package com.littlebook.admin.service;

import com.littlebook.admin.entity.LoginEvent;
import com.littlebook.admin.entity.ReviewActivity;
import com.littlebook.admin.entity.UserLoginStats;
import com.littlebook.admin.repository.LoginEventRepository;
import com.littlebook.admin.repository.ReviewActivityRepository;
import com.littlebook.admin.repository.UserLoginStatsRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.Transactional;
import com.littlebook.admin.dto.LoginRecordRequest;
import java.time.LocalDateTime;
import java.time.Duration;
import com.littlebook.admin.dto.SetLastLoginRequest;
import com.littlebook.admin.dto.SetTotalLoginsRequest;

@Service
public class StatsService {

    private final UserLoginStatsRepository userRepo;
    private final LoginEventRepository loginRepo;
    private final ReviewActivityRepository reviewRepo;
    private final UserClient userClient;

    public StatsService(UserLoginStatsRepository userRepo,
                        LoginEventRepository loginRepo,
                        ReviewActivityRepository reviewRepo,
                        ObjectProvider<UserClient> userClientProvider) {
        this.userRepo = userRepo;
        this.loginRepo = loginRepo;
        this.reviewRepo = reviewRepo;
        this.userClient = userClientProvider.getIfAvailable();
    }

    public List<UserLoginStats> listUsers() {
        return userRepo.findAll();
    }

    public Optional<UserLoginStats> getUser(UUID id) {
        return userRepo.findById(id);
    }

    public List<LoginEvent> listLoginEvents() {
        return loginRepo.findAll();
    }

    public List<ReviewActivity> listReviewActivities() {
        return reviewRepo.findAll();
    }

    public Map<String, Object> summary() {
        Map<String, Object> m = new HashMap<>();
        long nbUsers = userRepo.count();
        long nbLogins = loginRepo.count();
        long nbReviews = reviewRepo.countByEventType("review_created") + reviewRepo.countByEventType("review_liked");
        long nbLikes = reviewRepo.countByEventType("review_liked");

        m.put("nbUsers", nbUsers);
        m.put("nbLogins", nbLogins);
        m.put("nbReviews", nbReviews);
        m.put("nbLikes", nbLikes);
        return m;
    }

    @Transactional
    public UserLoginStats recordLogin(UUID userId, LoginRecordRequest req) {
        // create login event
        LoginEvent ev = new LoginEvent();
        ev.setUserId(userId);
        ev.setProvider(req.getProvider());
        ev.setIpAddress(req.getIpAddress());
        ev.setUserAgent(req.getUserAgent());
        LocalDateTime now = req.getLoggedInAt() != null ? req.getLoggedInAt() : LocalDateTime.now();
        ev.setLoggedInAt(now);
        loginRepo.save(ev);

        // update or create user stats
        UserLoginStats stats = userRepo.findById(userId).orElseGet(() -> {
            UserLoginStats s = new UserLoginStats();
            s.setUserId(userId);
            s.setTotalLogins(0);
            s.setFirstLoginAt(now);
            s.setUpdatedAt(now);
            return s;
        });

        int prev = stats.getTotalLogins();
        stats.setTotalLogins(prev + 1);
        if (stats.getFirstLoginAt() == null) {
            stats.setFirstLoginAt(now);
        }
        stats.setLastLoginAt(now);

        // compute avgDaysBetweenLogins as days between first and last divided by (n-1)
        if (stats.getTotalLogins() > 1) {
            long daysBetween = Duration.between(stats.getFirstLoginAt(), stats.getLastLoginAt()).toDays();
            double avg = (double) daysBetween / (stats.getTotalLogins() - 1);
            stats.setAvgDaysBetweenLogins(avg);
        } else {
            stats.setAvgDaysBetweenLogins(0.0);
        }

        stats.setUpdatedAt(now);
        userRepo.save(stats);
        return stats;
    }

    @Transactional
    public UserLoginStats incrementLogin(UUID userId) {
        // create a minimal LoginRecordRequest with current time
        LoginRecordRequest req = new LoginRecordRequest();
        req.setLoggedInAt(LocalDateTime.now());
        return recordLogin(userId, req);
    }

    @Transactional
    public UserLoginStats setLastLogin(UUID userId, SetLastLoginRequest req) {
        LocalDateTime at = req.getLastLoginAt() != null ? req.getLastLoginAt() : LocalDateTime.now();
        UserLoginStats stats = userRepo.findById(userId).orElseGet(() -> {
            UserLoginStats s = new UserLoginStats();
            s.setUserId(userId);
            s.setTotalLogins(0);
            s.setFirstLoginAt(at);
            s.setUpdatedAt(at);
            return s;
        });

        stats.setLastLoginAt(at);
        if (stats.getFirstLoginAt() == null) stats.setFirstLoginAt(at);
        // Recompute avgDaysBetweenLogins
        if (stats.getTotalLogins() > 1) {
            long daysBetween = Duration.between(stats.getFirstLoginAt(), stats.getLastLoginAt()).toDays();
            double avg = (double) daysBetween / (stats.getTotalLogins() - 1);
            stats.setAvgDaysBetweenLogins(avg);
        }
        stats.setUpdatedAt(LocalDateTime.now());
        userRepo.save(stats);
        return stats;
    }

    @Transactional
    public UserLoginStats setTotalLogins(UUID userId, SetTotalLoginsRequest req) {
        int total = req.getTotalLogins();
        UserLoginStats stats = userRepo.findById(userId).orElseGet(() -> {
            UserLoginStats s = new UserLoginStats();
            s.setUserId(userId);
            s.setFirstLoginAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        stats.setTotalLogins(total);
        // recompute avg if possible
        if (stats.getFirstLoginAt() != null && stats.getLastLoginAt() != null && stats.getTotalLogins() > 1) {
            long daysBetween = Duration.between(stats.getFirstLoginAt(), stats.getLastLoginAt()).toDays();
            double avg = (double) daysBetween / (stats.getTotalLogins() - 1);
            stats.setAvgDaysBetweenLogins(avg);
        }
        stats.setUpdatedAt(LocalDateTime.now());
        userRepo.save(stats);
        return stats;
    }

    /**
     * Retourne une liste d'utilisateurs avec leurs stats de login enrichies par les données du user-service.
     * Chaque élément est une Map contenant les champs du user-service (email, name, roles...) et les stats locales
     * (totalLogins, lastLoginAt, ...).
     */
    public List<Map<String, Object>> listUsersEnriched() {
        List<UserLoginStats> stats = userRepo.findAll();
        List<Map<String, Object>> out = new ArrayList<>();

        for (UserLoginStats s : stats) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("userId", s.getUserId());
            entry.put("totalLogins", s.getTotalLogins());
            entry.put("lastLoginAt", s.getLastLoginAt());
            entry.put("firstLoginAt", s.getFirstLoginAt());
            entry.put("avgDaysBetweenLogins", s.getAvgDaysBetweenLogins());

            if (userClient != null) {
                try {
                    Optional<Map<String, Object>> userOpt = userClient.getUserById(s.getUserId());
                    userOpt.ifPresent(u -> entry.putAll(u));
                } catch (Exception ex) {
                    // ignore enrichment failures - return at least stats
                }
            }
            out.add(entry);
        }

        return out.stream().collect(Collectors.toList());
    }
}
