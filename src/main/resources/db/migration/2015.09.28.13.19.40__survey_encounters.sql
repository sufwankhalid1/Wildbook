create table surveypart_encounters (
    surveypartid int,
    encounterid int,
    primary key (surveypartid, encounterid),
    foreign key (surveypartid) references surveypart(surveypartid),
    foreign key (encounterid) references encounters(encounterid)
);
