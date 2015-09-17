alter table surveypart drop column starttime;
alter table surveypart drop column endtime;

alter table surveypart add column starttime time with time zone;
alter table surveypart add column endtime time with time zone;
