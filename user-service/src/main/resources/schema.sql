-- Schema for user-service (H2-friendly)
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  name VARCHAR(255),
  picture VARCHAR(1024),
  provider VARCHAR(50) NOT NULL,
  provider_id VARCHAR(255),
  email_verified BOOLEAN DEFAULT FALSE,
  password_hash VARCHAR(255),
  roles VARCHAR(255),
  is_active BOOLEAN DEFAULT TRUE,
  last_login_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider, provider_id);
