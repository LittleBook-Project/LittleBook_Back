-- Initial test user inserted at startup for local testing
INSERT INTO users (id, email, name, picture, provider, provider_id, last_login_at)
VALUES (
  '11111111-1111-1111-1111-111111111111',
  'test.user@example.com',
  'Test User',
  'https://example.com/avatar.png',
  'GOOGLE',
  'google-1234567890',
  CURRENT_TIMESTAMP
);
