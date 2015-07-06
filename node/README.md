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
