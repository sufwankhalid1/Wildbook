create table mediasubmission_media
(
    mediasubmissionid int not null,
    mediaid varchar(255) not null,
    primary key (mediasubmissionid, mediaid)
);

ALTER TABLE mediasubmission_media
  ADD FOREIGN KEY (mediasubmissionid)
      REFERENCES "MEDIASUBMISSION" ("ID")
      ON DELETE CASCADE;
ALTER TABLE mediasubmission_media
  ADD FOREIGN KEY (mediaid)
      REFERENCES "SINGLEPHOTOVIDEO" ("DATACOLLECTIONEVENTID")
      ON DELETE CASCADE;
