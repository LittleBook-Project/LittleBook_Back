-- Schema for book-service (H2-friendly)
DROP TABLE IF EXISTS books;

CREATE TABLE books (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
  openlibrary_id VARCHAR(100) UNIQUE,
  isbn_10 VARCHAR(20),
  isbn_13 VARCHAR(20),
  title VARCHAR(500) NOT NULL,
  subtitle VARCHAR(500),
  authors VARCHAR(1000),
  publish_year INT,
  cover_url VARCHAR(1024),
  description CLOB,
  subjects VARCHAR(1000),
  source_data CLOB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_synced_at TIMESTAMP
);

CREATE INDEX idx_books_isbn10 ON books(isbn_10);
CREATE INDEX idx_books_isbn13 ON books(isbn_13);
CREATE INDEX idx_books_olid ON books(openlibrary_id);
CREATE INDEX idx_books_title ON books(title);
