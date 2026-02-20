create table if not exists processed_saga_event (
    id bigint auto_increment primary key,
    saga_id bigint not null,
    command_type varchar(50) not null,
    processed_at datetime not null,

    CONSTRAINT uk_saga_id_command_type UNIQUE (saga_id, command_type)
)