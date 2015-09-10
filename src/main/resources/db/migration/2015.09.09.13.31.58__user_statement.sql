alter table users add column statement varchar(255);

update users set statement = (select "USERSTATEMENT" from "USERS" u where u."EMAILADDRESS" = users.email);
update users set acceptedua = (select "ACCEPTEDUSERAGREEMENT" from "USERS" u where u."EMAILADDRESS" = users.email);
