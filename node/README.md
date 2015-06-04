#Wildbook Public Frontend
##Setup
Install node and npm.

In order to use the node.js app for the first time take the following steps.

    git pull
    cd node
    npm install
    
I configured my dev environment with symlinks but we can also copy them into place after an install. First go to the install directory. In your development environment that is $WILDBOOK_DEV/node.

    ln -s cust/happywhale/public public/cust
    ln -s cust/happywhale/server server/cust
    
This last one does require a copy as symlinks don't seem to work with the i18next code.

    cp cust/happywhale/locales/dev/cust.json locales/dev

