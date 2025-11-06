-- Archived Postgres schema. Disabled for admin-service H2 development.
-- Original schema has Postgres-specific statements (extensions, enums).
-- Kept here for reference; SQL initialization for H2 uses data.sql instead.
-- Insert a harmless no-op DDL for H2 so the SQL initializer has non-empty content.
-- This avoids startup failures when a leftover schema.sql exists in resources.
CREATE TABLE IF NOT EXISTS noop_init_marker (id INT);
DROP TABLE IF EXISTS noop_init_marker;
