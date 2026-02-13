create table if not exists option_type (
    id bigint auto_increment primary key,
    name varchar(16) unique
);

create table if not exists option_value (
	id bigint auto_increment primary key,
    name varchar(16) not null,
    option_type_id bigint not null,
    constraint fk_option_value_type foreign key (option_type_id) references option_type (id)
);

create table if not exists category (
	id bigint auto_increment primary key,
    name varchar(100) not null,
    depth int not null,
    path varchar(255),
    image_url varchar(512),
    parent_id bigint,
    created_at datetime default current_timestamp,
    updated_at datetime default current_timestamp on update current_timestamp,

    constraint fk_category_parent
		foreign key (parent_id)
        references category (id)
        on delete set null
);

create table if not exists product (
	id bigint auto_increment primary key,
    category_id bigint not null,
    name varchar(200) not null,
    status varchar(30) not null,
    description LONGTEXT,

    original_price bigint,
    lowest_price bigint,
    max_discount_rate int,

    rating double default 0.0,
    review_count bigint default 0,
    popularity_score double default 0.0,

    thumbnail varchar(512),
    published_at datetime,
    sale_stopped_at datetime,

    created_at datetime default current_timestamp,
    updated_at datetime default current_timestamp on update current_timestamp,

    constraint fk_product_category
		foreign key (category_id)
        references category (id)
);

create table if not exists product_option (
	id bigint auto_increment primary key,
    product_id bigint not null,
    option_type_id bigint not null,
    priority int not null default 0,

	created_at datetime default current_timestamp,
    updated_at datetime default current_timestamp on update current_timestamp,

    constraint fk_product_option_product
		foreign key (product_id)
        references product (id),

	constraint fk_product_option_type
		foreign key (option_type_id)
        references option_type (id)
);

create table if not exists product_image (
	id bigint auto_increment primary key,
    product_id bigint not null,
    image_url varchar(512) not null,
    sort_order int not null,

	created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    constraint fk_product_image_product
		foreign key (product_id)
        references product (id)
);

create table if not exists product_variant (
	id bigint auto_increment primary key,
    product_id bigint not null,
    sku varchar(64) unique,

    price bigint not null,
    original_price bigint not null,
    discount_amount bigint default 0,
    discount_rate bigint default 0,

    stock_quantity int not null default 0,

    constraint fk_product_variant_product
		foreign key (product_id)
        references product (id)
);

create table if not exists product_variant_option (
	id bigint auto_increment primary key,
    variant_id bigint not null,
    option_value_id bigint not null,

	created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    constraint fk_pvo_variant
		foreign key (variant_id)
        references product_variant (id),

    constraint fk_pvo_value
		foreign key (option_value_id)
        references option_value (id)
);