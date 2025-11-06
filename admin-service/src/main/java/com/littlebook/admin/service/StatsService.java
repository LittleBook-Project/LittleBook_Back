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

@Service
public class StatsService {

    private final UserLoginStatsRepository userRepo;
    private final LoginEventRepository loginRepo;
    private final ReviewActivityRepository reviewRepo;

    public StatsService(UserLoginStatsRepository userRepo,
                        LoginEventRepository loginRepo,
                        ReviewActivityRepository reviewRepo) {
        this.userRepo = userRepo;
        this.loginRepo = loginRepo;
        this.reviewRepo = reviewRepo;
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
}
