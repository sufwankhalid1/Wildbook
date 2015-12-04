#Gulp

##Setup
Should be nothing to do as all the appropriate dev dependencies should be in package.json. To get them in there I ran the following commands (but again you shouldn't have to do this).

    npm install
    
##Build

In order to convert our jade templates into html templates that are than packaged into a js file for use by Angular's$templateCache we run the following.

    gulp templates
    
To compile the less files into css files run...

    gulp less

And if you want to watch less and jade files for changes automatically. That is, as you edit less files the less task would automatically run and as you edit jade files the templates task would automatically run then just leave the following running in a shell...

    gulp watch
    
To do a full build of the system which compiles everything and makes the war file, you would run...

    gulp build
    
    
    