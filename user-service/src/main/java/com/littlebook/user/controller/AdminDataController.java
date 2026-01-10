package com.littlebook.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlebook.user.dto.CreateUserRequest;
import com.littlebook.user.dto.UserResponse;
import com.littlebook.user.entity.User;
import com.littlebook.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/admin")
public class AdminDataController {

    private static final Logger log = LoggerFactory.getLogger(AdminDataController.class);

    private final UserService userService;
    private final ObjectMapper mapper;

    public AdminDataController(UserService userService, ObjectMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponse>> exportAll() {
        List<User> users = userService.findAll();
        List<UserResponse> resp = users.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> importUsers(@RequestBody List<CreateUserRequest> payload) {
        int count = 0;
        for (CreateUserRequest r : payload) {
            userService.getOrCreateFromOAuth(r);
            count++;
        }
        log.info("Imported {} users via admin import", count);
        return ResponseEntity.ok("Imported " + count + " users");
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getName(),
                u.getPicture(),
                u.getProvider(),
                u.getProviderId(),
                u.isEmailVerified(),
                u.getRoles(),
                u.getCreatedAt(),
                u.getLastLogin(),
                u.getUpdatedAt(),
                u.isActive()
        );
    }
}
