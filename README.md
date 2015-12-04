#Wildbook

Wildbook (formerly the “Shepherd Project”) is an open source software framework to support mark-recapture, molecular ecology, and social ecology studies. The biological and statistical communities already support a number of excellent tools, such as Program MARK,GenAlEx, and SOCPROG for use in analyzing wildlife data. Wildbook is a complementary software application that:

-provides a scalable and collaborative platform for intelligent wildlife data storage and management, including advanced, consolidated searching

-provides an easy-to-use software suite of functionality that can be extended to meet the needs of wildlife projects, especially where individual identification is used

-provides an API to support the easy export of data to cross-disciplinary analysis applications (e.g., GenePop ) and other software (e.g., Google Earth)

-provides a platform that supports the exposure of data in biodiversity databases (e.g., GBIF and OBIS)

-provides a platform for animal biometrics that supports easy data access and facilitates matching application deployment for multiple species

Wildbook started as a collaborative software platform for globally-coordinated whale shark (Rhincodon typus ) research as deployed in the Wildbook for Whale Sharks (http://www.whaleshark.org). After many requests to use our software outside of whale shark research, it is now an open source, community-maintained standard for mark-recapture studies.

Please see http://www.wildme.org/wildbook for documentation.

#Building

To build the war file run the following...

    ./warbuild <cust>
    
where <cust> is a custom directive to pick up any user specific settings (e.g. mantamatcher). Leave blank to get the standard build.

To install into a local tomcat installation, first make sure you have a TOMCAT_HOME env variable set pointing to the directory in which tomcat is installed (e.g. /opt/tomcat8). Then simply run the following ...

    ./installwar wildbook-war/target/wildbook-war-6.0.0-SNAPSHOT.war

... or whatever the current version of wildbook war file is. This will install it to a webapp called "wildbook". To install to a different webapp run ...

    ./installwar wildbook-war/target/wildbook-war-6.0.0-SNAPSHOT.war <webapp_name>
    
