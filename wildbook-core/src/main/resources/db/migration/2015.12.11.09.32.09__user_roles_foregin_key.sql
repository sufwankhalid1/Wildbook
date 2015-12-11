delete from userroles ur where not exists (select * from users u where u.userid = ur.userid);

alter table userroles add foreign key (userid) references users(userid) on delete cascade;
