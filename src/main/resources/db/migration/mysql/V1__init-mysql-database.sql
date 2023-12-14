

    drop table if exists outgoing_order_items;
    drop table if exists receiving_order_items;
    drop table if exists order_item;
    drop table if exists product;
    drop table if exists category;
    drop table if exists volume;
    drop table if exists outgoing_order;
    drop table if exists receiving_order;

    create table category (
        deleted bit not null,
        version integer,
        created_date datetime(6),
        id bigint not null auto_increment,
        name varchar(20) not null,
        primary key (id),
        constraint uk_name unique (name)
    ) engine=InnoDB;

    create table volume (
        deleted bit not null,
        value_code integer not null check (value_code<=100000),
        version integer,
        created_date datetime(6),
        id bigint not null auto_increment,
        description varchar(15) not null,
        primary key (id),
        constraint uk_description unique (description)
    ) engine=InnoDB;

    create table product (
        cost decimal(38,2) not null,
        deleted bit not null,
        price decimal(38,2) not null,
        stock integer not null,
        unit_size integer not null,
        version integer,
        item_code varchar(7) not null,
        category_id bigint not null,
        created_date datetime(6),
        id bigint not null auto_increment,
        last_modified_date datetime(6),
        volume_id bigint not null,
        upc varchar(12) not null,
        name varchar(40) not null,
        primary key (id),
        constraint uk_item_code unique (item_code),
        constraint uk_upc unique (upc),
        constraint fk_product_volume foreign key (volume_id) references volume (id),
        constraint fk_product_category foreign key (category_id) references category (id)
    ) engine=InnoDB;

    create table order_item (
        price decimal(38,2) not null,
        quantity integer not null,
        created_date datetime(6),
        id bigint not null auto_increment,
        last_modified_date datetime(6),
        product_id bigint not null,
        primary key (id),
        constraint fk_item_product foreign key (product_id) references product (id)
    ) engine=InnoDB;

    create table outgoing_order (
        version integer,
        created_date datetime(6),
        id bigint not null auto_increment,
        last_modified_date datetime(6),
        receiver varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table outgoing_order_items (
        items_id bigint not null,
        outgoing_order_id bigint not null,
        primary key (items_id, outgoing_order_id),
        constraint uk_outgoing_item_id unique (items_id),
        constraint fk_outgoing_item foreign key (items_id) references order_item (id),
        constraint fk_outgoing_order foreign key (outgoing_order_id) references outgoing_order (id)
    ) engine=InnoDB;

    create table receiving_order (
        version integer,
        created_date datetime(6),
        id bigint not null auto_increment,
        last_modified_date datetime(6),
        supplier varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table receiving_order_items (
        items_id bigint not null,
        receiving_order_id bigint not null,
        primary key (items_id, receiving_order_id),
        constraint uk_receiving_item_id unique (items_id),
        constraint fk_receiving_item foreign key (items_id) references order_item (id),
        constraint fk_receiving_order foreign key (receiving_order_id) references receiving_order (id)
    ) engine=InnoDB;
