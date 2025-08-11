create table users_roles(
    user_id uuid,
    role varchar(100),
    foreign key (user_id) references users(id)
);