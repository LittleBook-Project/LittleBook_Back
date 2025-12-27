package com.littlebook.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/health")
    public String health() {
        return "admin-service OK";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
