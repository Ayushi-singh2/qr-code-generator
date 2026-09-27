-- Run this once, e.g. with: mysql -u root -p < setup.sql
-- (JPA/Hibernate will create the `users` and `qr_codes` tables automatically
--  on first application startup, since spring.jpa.hibernate.ddl-auto=update.)

CREATE DATABASE IF NOT EXISTS qr_generator_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Optional: create a dedicated application user instead of using root.
-- CREATE USER 'qr_app'@'localhost' IDENTIFIED BY 'change_this_password';
-- GRANT ALL PRIVILEGES ON qr_generator_db.* TO 'qr_app'@'localhost';
-- FLUSH PRIVILEGES;
