#Tomcat Setup

##Context file

You should create the following file for each of your webapps

    $TOMCAT_HOME/conf/Catalina/localhost/<webapp>.xml
    
and it should look something like this...

    <Context docBase="/opt/wildme/happywhale/wildbook/wildbook" 
             path="/happywhale" 
             reloadable="true">
      
        <Parameter name="config.dir" value="/opt/wildme/happywhale/wildbook/config" override="false"/>
    </Context>

This is what I have now for a file named **happywhale.xml**. This will now allow me to address my happywhale wildbook as http://localhost:8080/happywhale so that if I have more than one instance of wildbook running locally I can reference them under separate names.

The most important part of this is the parmeter that allows you to specify the directory of your configuration files. This way you can do that differently for every instance.

Also, this allows me to store my war file outside of the webapps dir under tomcat. Thus, once I install this file on tomcat I don't care where tomcat is anymore and I can even use a different version of tomcat with the same war file. The war file will still be unpacked into the webapps dir on tomcat but it sits outside.

That part is not absolutely necessary, but just convenient. If for instance you want your instance to behave **exactly** as it did before and your wildbook instance was named "wildbook" then just add the file

    $TOMCAT_HOME/conf/Catalina/localhost/wildbook.xml

with the contents

    <Context>
        <Parameter name="config.dir" value="/path/to/config/dir" override="false"/>
    </Context>

and put your configuration files there. You still put the wildbook.war into the webapps dir of $TOMCAT_HOME. But now, because this file is named wildbook.xml, it will pick up this config param.

Here is my **/opt/wildme** directory on my machine as an example...

    cuervomovil:wildme ken$ tree
    .
    ├── happywhale
    │   └── wildbook
    │       ├── config
    │       │   └── stormpathApiKey.properties
    │       └── wildbook.war
    ├── log
    │   └── core.html
    └── logback.xml
    
I just update the wildbook.war file in this directory whenever I want to "install" a new war file to happywhale.

##Logging

Set up the directory to which logs are directed via the system property

    -Dwildbook.logdir=/path/to/logdir

Add the following to CATALINA_OPTS or whatever it is java params are set if you want to use a special logging setup.

    -Dlogback.configurationFile=/path/to/config.xml
    
##Shepherd_data_dir

###Caching
To make sure tomcat 8 doesn't try and cache all the images in shepherd_data_dir you need to copy the META-INF directory (with the context.xml file) into that directory. Tomcat treats the directory as a webapp and this tells that webapp not to perform caching.

##Timeout Issue

On a production server (don't think you need this for dev unless testing the upload issue) add

    wrapper.ping.timeout=0
to

    /etc/tomcat8/tomcat.conf

or tune it with a non-zero value. Zero means never do a ping to check for timeout. The default is 30 (measured in seconds). There is also a param for the time between pings (default of 5 seconds). If any of the responses from the ping of the server take more than ping.timeout to return, then the wrapper attempts to restart tomcat. If the server is very busy, as it was with the processing of images, the server got rebooted.
