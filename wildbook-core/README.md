#Wildbook-core
##Install
To compile classes do

    gradle build
   
To do a clean install which will also build and install the resulting jar file into your local maven repo do
    
    gradle clean install
    
... or you can use maven ...

    mvn clean install

##Run
If you want to run a particular runnable class you can follow the example below

    gradle -PmainClass=org.ecocean.admin.media.ReadMissingExifData -Parguments='-s 1 --all' -Plogdir='<dir_containig_logback.xml>' build execute
    
or a worse way but if the above doesn't work...

    mvn exec:java -DmainClass=org.ecocean.admin.media.ReadMissingExifData -Dexec.args='-s 1 --all' -Plogdir='<dir_containig_logback.xml>'

...which is equivalent to running this at the command-line where the classpath is supplied by gradle...

    java -cp <classpath>:<dir_containing_logback.xml> org.ecocean.admin.media.ReadMissingExifData -s 1 --all

If the **logdir** parameter is excluded it will default to the environment variable $WILDBOOK_LOGDIR. If this is not set, or there is no logback.xml there, it will default to the default logging which will log chattily to the console.

##Flyway

    mvn -Dflyway.configFile=/opt/wildbook/flyway.conf compile flyway:migrate
    
##Build jar with all dependencies
So that command-line apps (e.g. ReadMissingExifData) can be run you can build a jar with all of it's depencies.

    mvn clean compile assembly:single
    
##Test email template example

To test the template found in **src/main/resources/emails/media/anotherSubmission.jade** you would run the following. Parameters such as **individualid** or **userid**, while maybe not applicable to the email template here, are examples of passing parameters to the test for which queries may be run to get the apprpriate data to pass to the template.

    http://localhost:8080/wildbook/test/email/get?template=media/anotherSubmission&individualid=7&userid=2

