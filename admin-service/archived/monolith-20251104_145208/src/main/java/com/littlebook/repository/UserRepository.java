package com.littlebook.repository;

import com.littlebook.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    // Recherche d’un utilisateur par son nom (correspond au champ 'userName' dans UserEntity)
    Optional<UserEntity> findByUserName(String userName);
}