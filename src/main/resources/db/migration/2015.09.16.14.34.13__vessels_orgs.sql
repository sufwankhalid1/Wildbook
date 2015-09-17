alter table survey add column surveynumber varchar(24);
alter table surveypart alter column surveyid set not null;
alter table vessel alter column orgid set not null;
alter table vessel alter column name set not null;
alter table organization alter column name set not null;
alter table users add column orgid int;
