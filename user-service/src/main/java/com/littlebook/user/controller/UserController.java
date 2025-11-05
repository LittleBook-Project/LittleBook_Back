package com.littlebook.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/health")
    public String health() {
        return "user-service OK";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
