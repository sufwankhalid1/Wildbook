create table mediaasset 
(
    id serial primary key not null,
    store integer references assetstore(id) not null,
    type varchar(20) not null,
    path text not null,
    parent integer references mediaasset(id),
    unique (store, path)
);

