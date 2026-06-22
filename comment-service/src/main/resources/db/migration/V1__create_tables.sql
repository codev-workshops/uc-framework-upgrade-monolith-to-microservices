create table comments (
  id varchar(255) primary key,
  body text,
  user_id varchar(255),
  article_id varchar(255),
  created_at TIMESTAMP NOT NULL
);

create table users (
  id varchar(255) primary key,
  username varchar(255) UNIQUE,
  password varchar(255),
  email varchar(255) UNIQUE,
  bio text,
  image varchar(511)
);
