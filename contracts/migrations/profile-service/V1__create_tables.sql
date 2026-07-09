-- follows: user_id follows follow_id. Both are user ids owned by user-auth-service;
-- there is intentionally NO FK across service boundaries.
create table follows (
  user_id varchar(255) not null,
  follow_id varchar(255) not null,
  primary key(user_id, follow_id)
);
