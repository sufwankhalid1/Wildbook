#Shepherd_data_dir

##Caching
To make sure tomcat 8 doesn't try and cache all the images in shepherd_data_dir you need to copy the META-INF directory (with the context.xml file) into that directory. Tomcat treats the directory as a webapp and this tells that webapp not to perform caching.
