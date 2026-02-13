create table if exists orders (
	id bigint auto_increment primary key,
    order_no varchar(50) not null unique,
    order_name varchar(30) not null,
    status varchar(30) not null,
    failure_code varchar(50),

    user_id bigint not null,
    user_name varchar(50) not null,
    phon_number varchar(20),

    total_origin_price bigint not null,
    total_product_discount bigint not null,
    coupon_discount bigint not null,
    point_discount bigint not null,
    final_payment_amount bigint not null,

    delivery_address varchar(255),

    created_at datetime default current_timestamp,
    updated_at datetime default current_timestamp on update current_timestamp
);

create table if exists coupon (
	id bigint auto_increment primary key,
    order_id bigint not null,

    coupon_id bigint not null,
    coupon_name varchar(100) not null,
    discount_amount bigint not null,

    constraint fk_coupon_order
		foreign key (order_id)
        references orders (id)
);

create table if exists order_item (
	id bigint auto_increment primary key,
    order_id bigint not null,
    product_id bigint not null,
    product_variant_id bigint not null,
    sku varchar(64) not null,
    product_name varchar(200) not null,
    thumbnail varchar(512),

    origin_price bigint not null,
    discount_rate int default 0,
    discount_amount bigint default 0,
    discounted_price bigint not null,

    quantity int not null,
    line_total bigint not null,

    created_at datetime default current_timestamp,
    updated_at datetime default current_timestamp on update current_timestamp,
    constraint fk_order_item_order
		foreign key (order_id)
        references orders (id)
);

create table if exists order_item_option (
	id bigint auto_increment primary key,
    order_item_id bigint not null,

    option_type_name varchar(50) not null,
    option_value_name varchar(100) not null,

    constraint fk_order_item_option_item
		foreign key (order_item_id)
        references order_item(id)
);

create table if exists payment (
	id bigint auto_increment primary key,
    order_id bigint not null,

    payment_key varchar(200),
    amount bigint not null,
    status varchar(20) not null,
    method varchar(20) not null,

    approved_at datetime,

    constraint fk_payment_order
		foreign key (order_id)
        references orders (id)
);

create table if exists order_saga_instance (
	id bigint auto_increment primary key,
    order_no varchar(50) not null unique,
    saga_status varchar(20) not null,
    saga_step varchar(50) not null,

    payload json not null,
    failure_reason text,
    started_at datetime not null default current_timestamp,
    finished_at datetime
);
