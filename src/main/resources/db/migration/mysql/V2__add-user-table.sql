
    drop table if exists users;

    create table users(
        id bigint not null auto_increment,
        username varchar(15) not null,
        password varchar(255) not null,
        first_name varchar(255) not null,
        last_name varchar(255) not null,
        role varchar(255) not null,
        enabled bit not null,
        created_date datetime(6),
        primary key(id),
        constraint uk_username unique (username)
    ) engine=InnoDB