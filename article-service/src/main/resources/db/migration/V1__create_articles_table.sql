create table articles (
  id varchar(255) primary key,
  user_id varchar(255) not null,
  slug varchar(255) not null,
  title varchar(255),
  description varchar(255),
  body text,
  created_at timestamp,
  updated_at timestamp
);
