-- article_id references article-service.articles ids; user_id references
-- user-auth-service.users ids (no cross-service FK).
create table article_favorites (
  article_id varchar(255) not null,
  user_id varchar(255) not null,
  primary key(article_id, user_id)
);
