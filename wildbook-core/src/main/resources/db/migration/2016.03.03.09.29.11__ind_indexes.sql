drop index users_email_idx;
drop index users_username_idx;
create index on users (lower(email));
create index on users (lower(username));
create index on individuals (lower(nickname));
create index on individuals (alternateid);
