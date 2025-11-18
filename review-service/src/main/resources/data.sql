-- Initial test reviews inserted at startup for local testing

INSERT INTO reviews (
  id,
  user_uuid,
  book_isbn,
  description,
  rating,
  created_at,
  updated_at
)
VALUES
(
  '33333333-3333-3333-3333-333333333333',
  '11111111-1111-1111-1111-111111111111',
  '9782070368228',
  'Un livre magnifique, poétique et profond, à relire à chaque âge.',
  5,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
),
(
  '33333333-3333-3333-3333-333333333334',
  '11111111-1111-1111-1111-111111111111',
  '9782070368228',
  'Très beau mais un peu surcoté à mon goût.',
  4,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);
