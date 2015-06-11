#Wildbook Public Frontend
##Setup
###Install node and npm
###CentOS
    sudo yum install nodejs
    sudo yum install npm
    
###MacOS
Best to use homebrew in my opinion
    brew install node
    brew install npm
    
###After node and npm install

In order to use the node.js app for the first time take the following steps.

    git pull
    cd Wildbook/node
    npm install
    
I configured my dev environment with symlinks but we can also copy them into place after an install. First go to the install directory. In your development environment that is $WILDBOOK_DEV/node.

    ln -s cust/happywhale/public public/cust
    ln -s cust/happywhale/server server/cust
    
This last one does require a copy as symlinks don't seem to work with the i18next code.

    cp cust/happywhale/locales/dev/cust.json locales/dev

###Customer symlinks
Until our build process copies these files into place we need to make symlinks.

    cd Wildbook/node
    ln -s ../cust/happywhale/public public/cust
    ln -s ../cust/happywhale/server server/cust
    
##Running

    cd node
    node app
    
Now point your browser to http://localhost:3000
