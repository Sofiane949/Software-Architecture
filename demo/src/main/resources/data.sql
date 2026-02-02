-- Insertion des rôles
INSERT INTO roles (name, description) VALUES ('USER', 'Standard user role');
INSERT INTO roles (name, description) VALUES ('ADMIN', 'Administrator role with full access');
INSERT INTO roles (name, description) VALUES ('MODERATOR', 'Moderator role with limited admin access');

-- Insertion des utilisateurs (mot de passe: password123 pour tous)
-- BCrypt hash de "password123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (username, email, password, is_active, created_at, updated_at) 
VALUES ('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, email, password, is_active, created_at, updated_at) 
VALUES ('john_doe', 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, email, password, is_active, created_at, updated_at) 
VALUES ('jane_smith', 'jane@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Association utilisateurs-rôles
-- admin -> ADMIN, MODERATOR, USER
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (1, 3);

-- john_doe -> USER, MODERATOR
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 3);

-- jane_smith -> USER
INSERT INTO user_roles (user_id, role_id) VALUES (3, 1);
