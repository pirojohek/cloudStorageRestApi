create schema if not exists users;

create table users.t_user
(
    c_id         serial primary key,
    c_username varchar(256) not null unique check (length(trim(c_username)) > 3),
    c_password varchar(512) not null
)