alter table users add foreign key (avatarid) references mediaasset(id) on delete cascade;
