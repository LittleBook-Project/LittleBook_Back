package com.littlebook.service;

import com.littlebook.entity.UserEntity;
import java.util.Optional;

public interface UserService {
    UserEntity registerUser(UserEntity user);
    Optional<UserEntity> findByUserName(String username);
}
