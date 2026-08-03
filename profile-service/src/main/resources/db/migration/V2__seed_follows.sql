-- Seed follow relationships mirroring the monolith's seed data.
-- user_id / follow_id are referenced ids owned by user-service (no foreign keys here).

INSERT INTO follows (user_id, follow_id) VALUES
('user-1', 'user-2'),
('user-2', 'user-1'),
('user-3', 'user-1'),
('user-3', 'user-2');
