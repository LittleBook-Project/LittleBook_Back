package com.littlebook.admin.repository;

import com.littlebook.admin.entity.LoginEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginEventRepository extends JpaRepository<LoginEvent, Long> {
}
