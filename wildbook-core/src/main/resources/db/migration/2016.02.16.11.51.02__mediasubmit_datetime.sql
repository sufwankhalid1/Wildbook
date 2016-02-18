alter table mediasubmission add column msdate date;
alter table mediasubmission add column mstime time without time zone;
update mediasubmission set msdate = to_timestamp(starttime/1000)::date, mstime = to_timestamp(starttime/1000)::time without time zone;
alter table mediasubmission drop column starttime;
alter table mediasubmission drop endtime;
