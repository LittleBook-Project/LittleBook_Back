-- user_login_stats cohérent avec users
INSERT INTO user_login_stats (user_id, total_logins, last_login_at, first_login_at, avg_days_between_logins, updated_at) VALUES
('11111111-1111-1111-1111-111111111111', 5, TIMESTAMP '2025-11-03 12:00:00', TIMESTAMP '2025-10-01 09:00:00', 7.5, TIMESTAMP '2025-11-04 10:00:00'),
('22222222-2222-2222-2222-222222222222', 2, TIMESTAMP '2025-11-02 08:30:00', TIMESTAMP '2025-11-01 07:15:00', 1.0, TIMESTAMP '2025-11-02 09:00:00');

INSERT INTO login_event (user_id, provider, ip_address, user_agent, logged_in_at) VALUES
('11111111-1111-1111-1111-111111111111', 'google', '203.0.113.1', 'Mozilla/5.0', TIMESTAMP '2025-11-03 12:00:00'),
('22222222-2222-2222-2222-222222222222', 'password', '198.51.100.5', 'curl/7.82.0', TIMESTAMP '2025-11-02 08:30:00');

-- review_activity cohérent avec books + users
INSERT INTO review_activity (book_id, user_id, event_type, created_at) VALUES
('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'review_created', TIMESTAMP '2025-11-01 10:00:00'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'review_liked',  TIMESTAMP '2025-11-02 11:15:00');
