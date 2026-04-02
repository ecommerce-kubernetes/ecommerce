create table if not exists product_description_image (
    id bigint auto_increment primary key,
    product_id bigint not null,
    image_url varchar(512) not null,
    sort_order int not null,

 	created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    constraint fk_product_description_image_product
        foreign key (product_id)
        references product(id)
);