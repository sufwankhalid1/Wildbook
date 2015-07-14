-- For org.ecocean.media.AssetStore

create table assetstore
(
    id serial primary key not null,
    name text unique not null,
    type varchar(20) not null,
    config text,
    writable bool,
    unique (type, config)
);

