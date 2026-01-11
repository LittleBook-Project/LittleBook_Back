-- Initial test notifications for local testing
-- Cohérent avec users, books et reviews existants

INSERT INTO notifications (
  id,
  user_uuid,
  type,
  title,
  message,
  read_flag,
  created_at
) VALUES
(
  '660e8400-e29b-41d4-a716-446655440000',
  '11111111-1111-1111-1111-111111111111',
  'REVIEW_CREATED',
  'Review publiée',
  'Votre review sur "Le Petit Prince" a bien été publiée.',
  false,
  CURRENT_TIMESTAMP
),
(
  '660e8400-e29b-41d4-a716-446655440001',
  '11111111-1111-1111-1111-111111111111',
  'REVIEW_LIKED',
  'Review appréciée',
  'Quelqu’un a aimé votre review sur "Le Petit Prince".',
  false,
  CURRENT_TIMESTAMP
),
(
  '660e8400-e29b-41d4-a716-446655440002',
  '11111111-1111-1111-1111-111111111111',
  'BOOK_ADDED',
  'Nouveau livre ajouté',
  '"L’Étranger" est maintenant disponible dans la bibliothèque.',
  true,
  CURRENT_TIMESTAMP
),
(
  '660e8400-e29b-41d4-a716-446655440003',
  '11111111-1111-1111-1111-111111111111',
  'SYSTEM',
  'Bienvenue sur LittleBook',
  'Bienvenue sur LittleBook ! Commencez à explorer et partager vos lectures.',
  true,
  CURRENT_TIMESTAMP
);
