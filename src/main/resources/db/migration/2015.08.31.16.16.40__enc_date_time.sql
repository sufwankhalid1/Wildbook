alter table encounters rename column encdate to encdateold;
alter table encounters add column encdate date;
alter table encounters add column starttime timetz;
alter table encounters add column endtime timetz;

update encounters set encdate = date(to_timestamp(encdateold/1000)), starttime = to_timestamp(encdateold/1000)::timetz;

alter table encounters drop column encdateold;
