# Use wildme/wildbook as the base image
FROM wildme/wildbook

# Copy the WAR file from your local project into the Docker image
COPY target/wildbook-7.0.0-EXPERIMENTAL.war /usr/local/tomcat/webapps/wildbook.war
COPY target/wildbook-7.0.0-EXPERIMENTAL /usr/local/tomcat/webapps/wildbook
# The base image wildme/wildbook likely already defines an ENTRYPOINT or CMD to run Tomcat.
# If you need to override it or add additional configuration, you can do so here.


ENV JPDA_ADDRESS="8000"
ENV JPDA_TRANSPORT="dt_socket"

EXPOSE 8080 8000
ENTRYPOINT ["/usr/local/tomcat/bin/catalina.sh", "jpda", "run"]

STOPSIGNAL SIGTERM