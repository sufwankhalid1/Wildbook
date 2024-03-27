#!/bin/bash

mvn clean
mvn install -DskipTests -Dmaven.javadoc.skip=true
docker build -t test_img .
docker ps -q --filter "name=wildbook_tomcat2" | grep -q . && docker stop wildbook_tomcat2 && docker rm -fv wildbook_tomcat2
docker container run -d -p 8080:8080 -p 8000:8000 --name wildbook_tomcat2  -v "$(pwd)"/wildbook_tomcat_data_dir:/datawildbook_data_dir/ test_img

