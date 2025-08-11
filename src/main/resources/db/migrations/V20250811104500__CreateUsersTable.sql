create table users(
    id uuid,
    name varchar(25),
    surname varchar(25),
    username varchar(50) unique,
    email varchar(50),
    role varchar(100),
    password varchar(100),
    oneTimeToken varchar(100),
    primary key (id)
);