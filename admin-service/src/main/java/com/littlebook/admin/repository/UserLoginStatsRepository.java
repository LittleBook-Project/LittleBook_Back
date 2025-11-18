package com.littlebook.admin.repository;

import com.littlebook.admin.entity.UserLoginStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserLoginStatsRepository extends JpaRepository<UserLoginStats, UUID> {
}
