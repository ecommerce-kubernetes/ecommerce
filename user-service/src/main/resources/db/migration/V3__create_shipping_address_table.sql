create table if not exists shipping_address (
	id bigint auto_increment primary key,
    user_id bigint not null,
    receiver_name varchar(50) not null,
    receiver_phone varchar(20) not null,
    zip_code varchar(10) not null,
    address varchar(255) not null,
    address_detail varchar(255) not null,

    CONSTRAINT fk_shipping_address_user FOREIGN KEY (user_id) REFERENCES users(id)
)
