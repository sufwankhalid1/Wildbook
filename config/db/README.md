How to set up the database
==========================

Assumes Postgres for the examples.


Create a db user to own and access the database
-----------------------------------------------

As postgres user:

`$ createuser -dP shepherd`


Create database
---------------

Still undecided if we want separate dev/prod/test dbs.
Create what you need:

As postgres user:

`$ createdb -O shepherd wildbook`

Test as you:

`$ psql -U shepherd -h localhost -p 5432 wildbook`

(`~/.pgpass` makes life nicer)


Create tables
-------------

(Hopefully obsolete)

As you, for each db:

`$ for f tables/*.sql; psql -U shepherd -h localhost -p 5432 wildbook < $f`


Set up db migration properties
------------------------------

Use the example as a guide:

`$ cp flyway.conf{.example,}`

`$ $EDITOR flyway.conf`

Make other config files if you have multiple databases.


Run migrations to bring db up to date
-------------------------------------

Install flyway [commandline version](http://flywaydb.org/documentation/commandline/)

From config/db:

`$ flyway info`

`$ flyway migrate`

If using a secondary db, you'll need to specify an alternate config file
like so:

`$ flyway -configFile=flyway_test.conf info`


Set application properties
--------------------------

Use the example as a guide:

`$ cp src/main/resources/bundles/s6db.properties{.example,}`

`$ $EDITOR src/main/resources/bundles/s6db.properties`
