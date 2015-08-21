create table mediasubmission
(
    id serial primary key,
    username varchar(100),
    name varchar(100),
    email varchar(100),
    description varchar(255),
    latitude double precision,
    longitude double precision,
    verbatimlocation varchar(255),
    submissionid varchar(255),
    status varchar(100),
    starttime bigint,
    endtime bigint,
    timesubmitted bigint
);

create table mediasubmission_media
(
    mediasubmissionid int not null,
    mediaid varchar(255) not null,
    primary key (mediasubmissionid, mediaid)
);

ALTER TABLE mediasubmission_media
  ADD FOREIGN KEY (mediasubmissionid)
      REFERENCES mediasubmission(id)
      ON DELETE CASCADE;
--ALTER TABLE mediasubmission_media
--  ADD FOREIGN KEY (mediaid)
--      REFERENCES "SINGLEPHOTOVIDEO" ("DATACOLLECTIONEVENTID")
--      ON DELETE CASCADE;
