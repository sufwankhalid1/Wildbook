create table mediasubmission
(
    id serial,
    description varchar(255),
    email varchar(100),
    endtime bigint,
    latitude double precision,
    longitude double precision,
    name varchar(100),
    starttime bigint,
    submissionid varchar(255),
    timesubmitted bigint,
    username varchar(100),
    verbatimlocation varchar(255),
    status varchar(100),
    primary key (id)
);
    