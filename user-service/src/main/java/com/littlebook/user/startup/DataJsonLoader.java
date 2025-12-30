package com.littlebook.user.startup;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlebook.user.dto.CreateUserRequest;
import com.littlebook.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Component
public class DataJsonLoader {

    private static final Logger log = LoggerFactory.getLogger(DataJsonLoader.class);

    private final UserService userService;
    private final ObjectMapper mapper;

    @Value("${app.data.users-file:/data/users.json}")
    private String usersFilePath;

    public DataJsonLoader(UserService userService, ObjectMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @PostConstruct
    public void load() {
        try {
            File f = new File(usersFilePath);
            if (!f.exists()) {
                log.info("No users file found at {} — skipping import", usersFilePath);
                return;
            }
            log.info("Loading users from {}", usersFilePath);
            byte[] bytes = Files.readAllBytes(f.toPath());
            List<CreateUserRequest> list = mapper.readValue(bytes, new TypeReference<>() {});
            int added = 0;
            for (CreateUserRequest r : list) {
                userService.getOrCreateFromOAuth(r);
                added++;
            }
            log.info("Imported {} users from {}", added, usersFilePath);
        } catch (Exception ex) {
            log.error("Failed to load users from {}: {}", usersFilePath, ex.getMessage(), ex);
        }
    }
}
