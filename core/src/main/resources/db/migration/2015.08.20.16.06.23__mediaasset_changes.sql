alter table mediaasset rename column type to category;
alter table mediaasset add column root int;
alter table mediaasset add column thumbstore int;
alter table mediaasset add column thumbpath text;
alter table mediaasset add column tags text[];
