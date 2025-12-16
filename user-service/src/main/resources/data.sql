-- Initial test users inserted at startup for local testing
INSERT INTO users (id, email, name, picture, provider, provider_id, last_login_at)
VALUES
(
  '11111111-1111-1111-1111-111111111111',
  'test.user@example.com',
  'Test User',
  'https://example.com/avatar.png',
  'GOOGLE',
  'google-1234567890',
  CURRENT_TIMESTAMP
),
(
  '22222222-2222-2222-2222-222222222222',
  'second.user@example.com',
  'Second User',
  'https://example.com/avatar2.png',
  'GOOGLE',
  'google-0987654321',
  CURRENT_TIMESTAMP
);
