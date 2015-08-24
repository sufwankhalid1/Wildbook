#How to set up the database

Assumes Postgres for the examples.

##Create a db user to own and access the database

As postgres user:

    createuser -dP shepherd


##Create database

Create what you need:

As postgres user:

    createdb -O shepherd wildbook

Test as you:

    psql -U shepherd -h localhost -p 5432 wildbook

(`~/.pgpass` makes life nicer)


##Set up db migration properties

Use the example as a guide:

    cp config/db/flyway.conf.example <private place>/flyway.conf

And edit it to put in appropriate values. Make other config files if you have multiple databases.


##Run migrations to bring db up to date

Install flyway [commandline version](http://flywaydb.org/documentation/commandline/)

For the following commands you can skip the -configFile property if you run flyway from the same directory as the config file AND it's named flyway.conf.

You may have to baseline your db first. This should only be true for this transition period for our development dbs. Once those are up-to-date this command should not be needed by any others. But for those run ...

    flyway -configFile=<path_to_configfile> -baselineVersion=2015.05.14.10.03.01 baseline

To run the migration and bring your db up to the current level...

    flyway --configFile=<path_to_configfile> migrate

To get the record of installed patches you can do

    flyway --configFile=<path_to_configfile> info

which just shows the results of the `schema_version` table that flyway installed on your db.

##Creating new migration

There is a script in the root of wildbook called `makesqlpatch` which should be run as follows ...
    
    ./makesqlpatch -m "brief message"
    
...and this will create a file in `src/main/resources/db/migration` of the format `YY.MM.DD.hh.mm.ss__brief_message.sql`

This will be your new file to use. The script will both echo the name (and path) to the console so you can just copy it, also it will try and open it with the `open` command (which might not work on your system).

##Set application properties

Use the example as a guide:

    cp src/main/resources/bundles/s6db.properties{.example,}

    $EDITOR src/main/resources/bundles/s6db.properties
