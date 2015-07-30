#Tomcat Setup

##Context file

In ```$TOMCAT_HOME/conf/context.xml``` add the following line (changing path as desired) inside of the *<Contex>* tag to allow your webapp to find the directory of configuration elements.

        <Parameter name="config.dir" value="/opt/wildbook/config" override="false"/>

Here is my ```/opt/wildbook/``` directory on my machine as an example...

    /opt/wildbook/
    ├── config
    │   ├── stormpathApiKey.properties
    │   └── text
    │       └── en
    │           ├── newmedia.html
    │           └── thankyou.html
    ├── log
    │   └── core.html
    └── logback.xml
    
##Logging

###centOS
For centOS I added the following to ```/etc/tomcat8/tomcat.conf``` file.

    wrapper.java.additional.4=-Dlogging.config=/opt/wildbook/logback.xml

###Ubuntu
Edit ```JAVA_OPTS``` in ```/etc/default/tomcat8```

###MacOS
For my dev machine I start tomcat with the included startup.sh script. So I just make sure I have the following environment variable set.

    export CATALINA_OPTS="-Xmx512m -Dlogging.config=/opt/wildbook/logback.xml"
    
##Shepherd_data_dir

###Caching
To make sure tomcat 8 doesn't try and cache all the images in shepherd_data_dir you need to copy the META-INF directory (with the context.xml file) into that directory. Tomcat treats the directory as a webapp and this tells that webapp not to perform caching.

##Timeout Issue

On a production server (don't think you need this for dev unless testing the upload issue) add

    wrapper.ping.timeout=0
to

    /etc/tomcat8/tomcat.conf

or tune it with a non-zero value. Zero means never do a ping to check for timeout. The default is 30 (measured in seconds). There is also a param for the time between pings (default of 5 seconds). If any of the responses from the ping of the server take more than ping.timeout to return, then the wrapper attempts to restart tomcat. If the server is very busy, as it was with the processing of images, the server got rebooted.
