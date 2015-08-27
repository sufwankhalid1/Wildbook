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

To run the migration you should use the maven plugin...

    mvn -e -Dflyway.configFile=<path_to_configfile> clean compile flyway:migrate

To get the record of installed patches you can do

    mvn -e -Dflyway.configFile=<path_to_configfile> flyway:info
    
..or..
    
    flyway --configFile=<path_to_configfile> info

which just shows the results of the `schema_version` table that flyway installed on your db.

###OLD

**File based which will not work unless you have the full CLASSPATH for the maven project set now that we have added some java patches. Just use the maven plugin above instead**

To run the migration and bring your db up to the current level...

    flyway --configFile=<path_to_configfile> migrate

##Creating new migration

There is a script in the root of wildbook called `makesqlpatch` which should be run as follows ...
    
    ./makesqlpatch -m "brief message"
    
...and this will create a file in `src/main/resources/db/migration` of the format `YY.MM.DD.hh.mm.ss__brief_message.sql`

This will be your new file to use. The script will echo the name (and path) to the console so you can just copy it and launch your favorite editor with it.

You can also make a java patch with the ```-j``` option. This will create a class file with the name of the class being like ```VYY_MM_DD_hh_mm_ss__brief_message.java```. Unlike the SQL files you are more locked into your naming scheme here. It must start with "V" and instead of dots it needs to have underbars. This java file will be placed in ```src/main/java/db/migration``` and will be auto-magically picked up by flyway and executed within a transaction.

##Set application properties

Use the example as a guide:

    cp src/main/resources/bundles/s6db.properties{.example,}

    $EDITOR src/main/resources/bundles/s6db.properties
