DROP TABLE IF EXISTS reviews;

CREATE TABLE reviews (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
  user_uuid UUID NOT NULL,
  book_isbn VARCHAR(20) NOT NULL,
  description CLOB,
  rating INT CHECK (rating BETWEEN 1 AND 5),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_user_uuid ON reviews(user_uuid);
CREATE INDEX idx_reviews_book_isbn ON reviews(book_isbn);