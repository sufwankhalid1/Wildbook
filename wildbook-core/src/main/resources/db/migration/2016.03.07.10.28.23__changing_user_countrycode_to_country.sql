alter table users rename column countrycode to country;
alter table users alter column country set data type varchar(200);
