#Wildbook-core
##Install
To compile classes do

    gradle build
   
    
To do a clean install which will also build and install the resulting jar file into your local maven repo do
    
    gradle clean install

##Run
If you want to run a particular runnable class you can follow the example below

    gradle -PmainClass=org.ecocean.admin.media.ReadMissingExifData -Parguments='-s 1 --all' -Plogdir='<dir_containig_logback.xml>' build execute
    
...which is equivalent to running this at the command-line where the classpath is supplied by gradle...

    java -cp <classpath>:<dir_containing_logback.xml> org.ecocean.admin.media.ReadMissingExifData -s 1 --all

If the **logdir** parameter is excluded it will default to the environment variable $WILDBOOK_LOGDIR. If this is not set, or there is no logback.xml there, it will default to the default logging which will log chattily to the console.

##Build jar with all dependencies
So that command-line apps (e.g. ReadMissingExifData) can be run you can build a jar with all of it's depencies.

    mvn clean compile assembly:single