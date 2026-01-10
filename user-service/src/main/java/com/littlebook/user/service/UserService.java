package com.littlebook.user.service;

import com.littlebook.user.dto.CreateUserRequest;
import com.littlebook.user.dto.UpdateUserRequest;
import com.littlebook.user.entity.User;
import com.littlebook.user.enums.AuthProvider;
import com.littlebook.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String ping() { return "pong"; }

    public Optional<User> findById(UUID id) { return userRepository.findById(id); }

    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email); }

    public Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    /**
     * Create or update a user coming from an OAuth provider (Google/Microsoft).
     * - If a user with provider+providerId exists, update profile fields and lastLogin
     * - Else if a user with same email exists, link provider info
     * - Else create a new user
     */
    public User getOrCreateFromOAuth(CreateUserRequest req) {
        // try provider+providerId
        Optional<User> byProvider = userRepository.findByProviderAndProviderId(req.provider(), req.providerId());
        if (byProvider.isPresent()) {
            User existing = byProvider.get();
            existing.setName(req.name());
            existing.setPicture(req.picture());
            existing.setEmailVerified(req.emailVerified());
            existing.setLastLogin(Instant.now());
            return userRepository.save(existing);
        }

        // try by email
        if (req.email() != null) {
            Optional<User> byEmail = userRepository.findByEmail(req.email());
            if (byEmail.isPresent()) {
                User u = byEmail.get();
                u.setProvider(req.provider());
                u.setProviderId(req.providerId());
                u.setName(u.getName() == null ? req.name() : u.getName());
                u.setPicture(u.getPicture() == null ? req.picture() : u.getPicture());
                u.setEmailVerified(req.emailVerified());
                u.setLastLogin(Instant.now());
                return userRepository.save(u);
            }
        }

        // create new
        User created = new User();
        created.setEmail(req.email());
        created.setName(req.name());
        created.setPicture(req.picture());
        created.setProvider(req.provider());
        created.setProviderId(req.providerId());
        created.setEmailVerified(req.emailVerified());
        created.setCreatedAt(Instant.now());
        created.setLastLogin(Instant.now());
        created.setUpdatedAt(Instant.now());
        created.setRoles("ROLE_USER");
        return userRepository.save(created);
    }

    public User updateProfile(UUID id, UpdateUserRequest req) {
        User u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (req.name() != null) u.setName(req.name());
        if (req.picture() != null) u.setPicture(req.picture());
        if (req.roles() != null) u.setRoles(req.roles());
        return userRepository.save(u);
    }

    public void deactivate(UUID id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setActive(false);
            userRepository.save(u);
        });
    }

    // Return all users (used by admin export endpoints / startup snapshots)
    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }
}
