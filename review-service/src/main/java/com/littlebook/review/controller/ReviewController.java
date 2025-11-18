package com.littlebook.review.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @GetMapping("/health")
    public String health() {
        return "review-service OK";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
