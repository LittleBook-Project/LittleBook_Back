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

-- Utilisateurs factices supplémentaires pour rendre la table admin plus réaliste
INSERT INTO users (id, email, name, picture, provider, provider_id, last_login_at, roles, is_active)
VALUES
( '33333333-3333-3333-3333-333333333333', 'alice.dupont@example.com', 'Alice Dupont', 'https://i.pravatar.cc/150?img=1', 'LOCAL', null, '2025-11-20 09:15:00', 'ROLE_USER', true ),
( '44444444-4444-4444-4444-444444444444', 'bob.martin@example.com', 'Bob Martin', 'https://i.pravatar.cc/150?img=2', 'GOOGLE', 'google-555', '2025-11-02 08:30:00', 'ROLE_USER', false ),
( '55555555-5555-5555-5555-555555555555', 'caroline.leblanc@example.com', 'Caroline Leblanc', 'https://i.pravatar.cc/150?img=3', 'MICROSOFT', 'ms-999', '2025-11-11 12:00:00', 'ROLE_ADMIN', false ),
( '66666666-6666-6666-6666-666666666666', 'david.renard@example.com', 'David Renard', 'https://i.pravatar.cc/150?img=4', 'GOOGLE', 'google-777', '2025-12-28 14:45:00', 'ROLE_USER', true ),
( '77777777-7777-7777-7777-777777777777', 'eve.leroy@example.com', 'Eve Leroy', 'https://i.pravatar.cc/150?img=5', 'LOCAL', null, '2025-12-01 10:20:00', 'ROLE_USER', true );
