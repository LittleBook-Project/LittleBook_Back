CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================
-- ENUMERATIONS
-- ============================

CREATE TYPE role AS ENUM ('ADMIN', 'CLASSIC');
CREATE TYPE reading_status AS ENUM ('READING', 'READ', 'TO_READ');

-- ============================
-- TABLE: USERS
-- ============================

CREATE TABLE users (
    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_name VARCHAR(100) NOT NULL,
    email_address VARCHAR(255) UNIQUE NOT NULL,
    profile_creation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    role role NOT NULL
);

-- ============================
-- TABLE: BOOK
-- ============================

CREATE TABLE book (
    isbn VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100),
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(255),
    publication_date DATE,
    cover_image VARCHAR(255)
);

-- ============================
-- TABLE: REVIEW
-- ============================

CREATE TABLE review (
    id SERIAL PRIMARY KEY,
    description TEXT,
    review_creation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    user_uuid UUID NOT NULL,
    book_isbn VARCHAR(20) NOT NULL,
    CONSTRAINT fk_review_user FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE,
    CONSTRAINT fk_review_book FOREIGN KEY (book_isbn) REFERENCES book(isbn) ON DELETE CASCADE
);

-- ============================
-- TABLE: READING
-- ============================

CREATE TABLE reading (
    id SERIAL PRIMARY KEY,
    user_uuid UUID NOT NULL,
    book_isbn VARCHAR(20) NOT NULL,
    reading_date DATE DEFAULT CURRENT_DATE,
    status reading_status NOT NULL,
    CONSTRAINT fk_reading_user FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE,
    CONSTRAINT fk_reading_book FOREIGN KEY (book_isbn) REFERENCES book(isbn) ON DELETE CASCADE
);

-- ============================
-- TABLE: USER SUBSCRIPTIONS (relation User ↔ User)
-- ============================

CREATE TABLE user_subscription (
    subscriber_uuid UUID NOT NULL,
    subscribed_to_uuid UUID NOT NULL,
    PRIMARY KEY (subscriber_uuid, subscribed_to_uuid),
    CONSTRAINT fk_subscriber FOREIGN KEY (subscriber_uuid) REFERENCES users(uuid) ON DELETE CASCADE,
    CONSTRAINT fk_subscribed_to FOREIGN KEY (subscribed_to_uuid) REFERENCES users(uuid) ON DELETE CASCADE
);