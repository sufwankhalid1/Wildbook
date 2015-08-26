alter table mediasubmission_media rename column mediaid to oldmediaid;
alter table mediasubmission_media add column mediaid int;
alter table mediasubmission_media add foreign key (mediaid) references mediaasset(id);
