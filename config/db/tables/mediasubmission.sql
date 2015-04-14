create table mediasubmission
(
    id serial,
    username varchar(100),
    name varchar(100),
    email varchar(100),
    description varchar(255),
    latitude double precision,
    longitude double precision,
    verbatimlocation varchar(255),
    starttime timestamp with time zone,
    endtime timestamp with time zone,
    submissionid varchar(255),
    status varchar(100),
    timesubmitted timestamp with time zone,
    primary key (id)
);
