create table idservice (
    idserviceid serial primary key,
    service varchar(24),
    mediaid int,
    senddate bigint,
    initresponse text,
    resultdate bigint,
    result text,
    foreign key (mediaid) references mediaasset(id)
);
