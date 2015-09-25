create table users (
    id serial primary key,
    username varchar(100),
    fullname varchar(255),
    creationdate date default current_date,
    email varchar(255),
    lastlogin bigint,
    password text,
    salt text,
    phonenumber varchar(255),
    physicaladdress varchar(255),
    avatarid int
);

create unique index on users (username);
create unique index on users (email);

create table userroles (
    userid int,
    context varchar(100),
    rolename varchar(100),
    primary key (userid, context, rolename)
);

alter table mediasubmission add column userid int;
alter table mediaasset add column submitterid int;

alter table mediasubmission add foreign key (userid) references users(id);
alter table mediaasset add foreign key (submitterid) references users(id);

insert into users (username, fullname, creationdate, email, lastlogin, password, salt, phonenumber, physicaladdress, avatarid)
    select "USERNAME", "FULLNAME", to_timestamp("DATEINMILLISECONDS"/1000)::date, "EMAILADDRESS",
           "LASTLOGIN", "PASSWORD", "SALT", "PHONENUMBER", "PHYSICALADDRESS", "USERIMAGEID"
    from "USERS";

insert into userroles select id, "CONTEXT", "ROLE_NAME" FROM "USER_ROLES" ur inner join users u on u.username = ur."USERNAME";

update mediasubmission set userid = (select id from users u
       where lower(u.username) = lower(mediasubmission.email)
       or lower(u.email) = lower(mediasubmission.email));
update mediasubmission set userid = (select id from users u
       where lower(u.username) = lower(mediasubmission.username)
       or lower(u.email) = lower(mediasubmission.username));
update mediaasset set submitterid = (select id from users u
    where lower(u.username) = lower(mediaasset.submitter)
    or lower(u.email) = lower(mediaasset.submitter));
