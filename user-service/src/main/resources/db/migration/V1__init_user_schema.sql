create table if not exists users (
	id bigint auto_increment primary key,
    email varchar(100) not null unique,
    name varchar(50) not null,
    encrypted_pwd varchar(255) not null,
    phone_number varchar(20),
    gender varchar(10) not null,
    birth_date date,
    point bigint default 0 not null,
    role varchar(20) not null,
    created_at datetime default current_timestamp,
    updated_at datetime default current_timestamp on update current_timestamp
)