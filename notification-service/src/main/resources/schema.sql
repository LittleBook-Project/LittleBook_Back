-- Schema for notification-service (H2-friendly)
DROP TABLE IF EXISTS notifications;

CREATE TABLE notifications (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
  user_uuid UUID NOT NULL,
  type VARCHAR(100) NOT NULL,
  title VARCHAR(200) NOT NULL,
  message VARCHAR(2000) NOT NULL,
  read_flag BOOLEAN DEFAULT FALSE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_notifications_user_uuid ON notifications(user_uuid);
CREATE INDEX idx_notifications_user_uuid_read_flag ON notifications(user_uuid, read_flag);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_type ON notifications(type);
