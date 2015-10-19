create table organization (
    orgid serial primary key,
    name varchar(100)
);

create table vessel (
    vesselid serial primary key,
    orgid int,
    type varchar(24),
    name varchar(100),
    foreign key (orgid) references organization(orgid)
);

create table survey (
    surveyid serial primary key,
    orgid int,
    foreign key (orgid) references organization(orgid)
);

create table surveypart (
    surveypartid serial primary key,
    surveyid int,
    vesselid int,
    code varchar(24),
    comments varchar(1000),
    starttime bigint,
    endtime bigint,
    latitude double precision,
    longitude double precision,
    foreign key (surveyid) references survey(surveyid),
    foreign key (vesselid) references vessel(vesselid)
);

create index surveypart_survey_idx on surveypart(surveyid);

create table surveypoint (
    surveypartid int,
    pointtime bigint,
    latitude double precision,
    longitude double precision,
    elevation double precision,
    foreign key (surveypartid) references surveypart(surveypartid)
);
