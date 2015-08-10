#Wildbook Public Frontend
##Setup
Install node and npm.

In order to use the node.js app for the first time take the following steps.

    git pull
    cd node
    npm install
    
I configured my dev environment with symlinks but we can also copy them into place after an install. First go to the install directory. In your development environment that is $WILDBOOK_DEV/node.

    cd public
    ln -s ../cust/happywhale/public cust
    cd ..
    
##Broken
NOTE: We are not overriding the i18next config files below because I can't get that to work.

This last one does require a copy as symlinks don't seem to work with the i18next code.

    cp cust/happywhale/locales/dev/cust.json locales/dev

##Running

    cd node
    node app
    
Now point your browser to http://localhost:3000

##Deploying

###On Your Development Machine

Make sure you have the config directory of secrets (we need to make this a private repo) for the customer. Currently, due to the dep.xml file configuration this needs to be in ```<parent_dir_of_Wildbook>/cust/happywhale/wildbook/config``` and so far just contains the stormpath api keys (more to be added later). For example...

    cuervomovil:Wildbook ken$ tree ../cust/happywhale
    ../cust/happywhale
    └── wildbook
        └── config
            └── stormpathApiKey.properties

This should have to be very rarely updated so it should just be there once you create it. Then move on to building...

    cd ${dev-dir}/Wildbook
    mvn clean install -Dcust=happywhale
    
Actually, instead of the above, I do the following because the tests don't work for me anyway and I don't want to waste time building javadocs that are unused. Skipping tests and javadocs over and over again saves me a lot of time.

    mvn clean compile package -DskipTests -Dmaven.javadoc.skip=true -Dcust=happywhale
    
    scp target/wildbook-5.3.0-RELEASE.war target/wildbook-5.3.0-RELEASE-config.war devhappywhale.com:/var/tmp
    
    cd ${dev-dir}/Wildbook/node
    mvn assembly:single -Dcust=happywhale
    scp target/animalus-0.1-SNAPSHOT-node.zip devhappywhale.com:/var/tmp
    
###Server
    
Simply log on to the server and run

    cd /opt/happywhale
    ./install_all
    
And this will install both tomcat war and node assuming that the names of the war and zip are the same as those above. If our versions change, simply chang the install_all script.

For the node side of things this simply does the following...

    cd /opt/happywhale
    sudo unzip -qo -d /opt/happywhale /var/tmp/animalus-0.1-SNAPSHOT-node.zip
    sudo stop happywhale_web
    sudo start happywhale_web
