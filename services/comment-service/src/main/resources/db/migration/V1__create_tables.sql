-- article_id references article-service.articles ids; user_id references
-- user-auth-service.users ids (no cross-service FK).
create table comments (
  id varchar(255) primary key,
  body text,
  article_id varchar(255),
  user_id varchar(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
