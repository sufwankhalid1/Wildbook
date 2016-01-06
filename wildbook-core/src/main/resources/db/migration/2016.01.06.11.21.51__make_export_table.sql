create table exports
(
    exportid serial primary key not null,
    userid int,
    datetimestamp TIMESTAMP WITH TIME ZONE,
    type varchar(32),
    outputdir varchar(32),
    status smallint,
    error  text,
    delivered boolean default false
);

