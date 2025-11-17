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
