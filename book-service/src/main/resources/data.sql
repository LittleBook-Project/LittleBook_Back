-- Initial test book inserted at startup for local testing

INSERT INTO books (
  id,
  openlibrary_id,
  isbn_10,
  isbn_13,
  title,
  subtitle,
  authors,
  publish_year,
  cover_url,
  description,
  subjects,
  source_data,
  created_at,
  updated_at,
  last_synced_at
)
VALUES (
  '22222222-2222-2222-2222-222222222222',
  'OL12345W',
  '207036822X',
  '9782070368228',
  'Le Petit Prince',
  'Édition illustrée',
  'Antoine de Saint-Exupéry',
  1943,
  'https://covers.openlibrary.org/b/id/12345-L.jpg',
  'Un conte poétique et philosophique intemporel.',
  'Fable,Classique,Littérature française',
  '{"openlibrary_link":"https://openlibrary.org/works/OL12345W"}',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);

-- Second test book for admin/review activity
INSERT INTO books (
  id,
  openlibrary_id,
  isbn_10,
  isbn_13,
  title,
  subtitle,
  authors,
  publish_year,
  cover_url,
  description,
  subjects,
  source_data,
  created_at,
  updated_at,
  last_synced_at
)
VALUES (
  'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  'OL67890W',
  '2253006327',
  '9782253006329',
  'L''Étranger',
  NULL,
  'Albert Camus',
  1942,
  'https://covers.openlibrary.org/b/id/67890-L.jpg',
  'Roman existentiel classique de la littérature française.',
  'Roman,Classique,Littérature française',
  '{"openlibrary_link":"https://openlibrary.org/works/OL67890W"}',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);
