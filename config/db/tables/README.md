How to set up the database
==========================

Assumes Postgres.  Assumes a lot of things, actually.


Create a user to own and access the databases
---------------------------------------------

As postgres user:
`$ createuser -dP shepherd`


Create production and test databases
------------------------------------

As postgres user:
`$ createdb -O shepherd wildbook`
`$ createdb -O shepherd wildbook_test`

Test as you:
`$ psql -U shepherd -h localhost -p 5432 wildbook_test`
(`~/.pgpass` makes life nicer)


Create tables
-------------

As you:
`$ for f config/db/tables/*.sql; psql -U shepherd -h localhost -p 5432 wildbook < $f`
`$ for f config/db/tables/*.sql; psql -U shepherd -h localhost -p 5432 wildbook_test < $f`


Set application properties
--------------------------

Use the example as a guide:
`$ cp src/main/resources/bundles/s6db.properties{.example,}`
`$ $EDITOR src/main/resources/bundles/s6db.properties`
