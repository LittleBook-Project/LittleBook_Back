package com.littlebook.admin.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTest {

    @Test
    void ping_returnsPong() {
        AdminService svc = new AdminService();
        assertEquals("pong", svc.ping());
    }
}
