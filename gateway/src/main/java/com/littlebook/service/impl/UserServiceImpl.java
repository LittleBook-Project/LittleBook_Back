package com.littlebook.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.littlebook.service.UserService;
import com.littlebook.repository.UserRepository;
import com.littlebook.entity.UserEntity;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Override
	public UserEntity registerUser(UserEntity user) {
		// Hachage du mot de passe avant sauvegarde
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}

	@Override
	public Optional<UserEntity> findByUserName(String username) {
		return userRepository.findByUserName(username);
	}
}
