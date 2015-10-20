create table individuals (
    individualid serial primary key,
    alternateid varchar(100),
    dateidentified varchar(255),
    sex varchar(255),
    nickname varchar(255),
    avatarid int,
    foreign key (avatarid) references mediaasset(id)
);

create table encounters (
    encounterid serial primary key,
    individualid int,
    encdate bigint,
    latitude double precision,
    longitude double precision,
    state varchar(128),
    submitter varchar(100),
    locationid varchar(255),
    verbatimLocation text,
    foreign key (individualid) references individuals(individualid)
);

create table encounter_media (
    encounterid int,
    mediaid int,
    primary key (encounterid, mediaid),
    foreign key (encounterid) references encounters(encounterid),
    foreign key (mediaid) references mediaasset(id)
);

create table temp_enc_convert (
    catalognumber varchar(100),
    encounterid int,
    primary key (catalognumber, encounterid)
);

create table temp_ind_convert (
    oldindividualid varchar(100),
    individualid int,
    primary key (oldindividualid, individualid)
);
