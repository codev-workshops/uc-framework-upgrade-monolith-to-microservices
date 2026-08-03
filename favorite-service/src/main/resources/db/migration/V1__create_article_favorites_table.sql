-- favorite-service owns only the article_favorites table.
-- article_id and user_id are referenced IDs (no foreign keys to articles/users).
create table article_favorites (
  article_id varchar(255) not null,
  user_id varchar(255) not null,
  primary key(article_id, user_id)
);
