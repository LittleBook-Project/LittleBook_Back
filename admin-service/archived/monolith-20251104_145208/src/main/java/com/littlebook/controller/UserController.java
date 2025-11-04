package com.littlebook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.littlebook.service.UserService;
import com.littlebook.entity.UserEntity;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public UserEntity register(@RequestBody UserEntity user) {
        return userService.registerUser(user);
    }

    @GetMapping("/{userName}")
    public ResponseEntity<UserEntity> getUser(@PathVariable String userName) {
        Optional<UserEntity> u = userService.findByUserName(userName);
        return u.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
