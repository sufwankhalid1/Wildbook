alter table users drop constraint users_avatarid_fkey;
alter table users alter column avatarid type text;
alter table users rename column avatarid to avatar;
