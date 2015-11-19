#Gulp

#Angular Jade Templates
##Setup
    npm install --global gulp
    npm install gulp --save-dev
    npm install gulp-jade --save-dev
    npm install gulp-angular-templatecache --save-dev
    npm install gulp-less --save-dev
    npm install gulp-watch --save-dev
    
##Run

In order to convert our jade templates into html templates that are than packaged into a js file for use by Angular's$templateCache we run the following.

    gulp templates
    
To compile the less files into css files run...

    gulp less

And if you want to watch less and jade files for changes automatically. That is, as you edit less files the less task would automatically run and as you edit jade files the templates task would automatically run then just leave the following running in a shell...

    gulp watch
    
    
    