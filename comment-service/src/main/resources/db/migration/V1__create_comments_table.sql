-- comment-service owns only the comments table.
-- article_id and user_id are referenced IDs (no foreign keys to articles/users).
create table comments (
  id varchar(255) primary key,
  body text,
  article_id varchar(255),
  user_id varchar(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
