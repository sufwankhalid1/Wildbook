alter table mediaasset add column submitter varchar(100);

update mediaasset set submitter = (select "SUBMITTER" from "SINGLEPHOTOVIDEO" spv INNER JOIN temp_spv_media tsm on tsm.spvid = spv."DATACOLLECTIONEVENTID" where tsm.mediaid = mediaasset.id limit 1);
