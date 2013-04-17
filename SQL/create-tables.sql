drop table if exists users;
create table users (
  id integer primary key autoincrement,
  username text not null,
  password text not null // TODO hash
);

drop table if exists follow;
create table follow (
  id integer primary key autoincrement,
  follower_id integer not null references users (id),
  followed_id integer not null references users (id)
);

drop table if exists messages;
create table messages (
  id integer primary key autoincrement,
  user_id integer not null references users (id),
  body text not null,
  ts timestamp not null default now()
);

insert into users (username, password) 
values ('user1', 'abc'), ('user2', 'def');

insert into follow (follower_id, followed_id) 
values (2, 1);

insert into messages (user_id, body, ts) 
values (1, 'message1 by user1', timestamp '2013-01-01'),
(1, 'message2 by user1', timestamp '2013-01-03'),
(2, 'message1 by user2', timestamp '2013-01-02');
