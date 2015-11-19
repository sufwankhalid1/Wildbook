//
// To add a new library to the package.json file run
// e.g. npm install gulp-rsync --save-dev
//
var gulp = require('gulp');
var jade = require('gulp-jade');
var templateCache = require('gulp-angular-templatecache');
var less = require('gulp-less');
var rsync = require('rsyncwrapper').rsync;
var argv = require('yargs').argv;
var debug = require('gulp-debug');
var run = require('gulp-run');

var webapp = argv.w || argv.webapp || "wildbook";

function doRsync(opts) {
    opts.recursive = true;
    
    rsync(opts,
        function(error, stdout, stderr, cmd) {
            if (error) {
                console.log(error.message);
            }
        }
    );
}

gulp.task('templates', function() {
    return gulp.src('src/main/templates/**/*.jade').pipe(jade())
        .pipe(templateCache()).pipe(gulp.dest('src/main/webapp/javascript'));
});

gulp.task('less', function() {
    return gulp.src('src/main/webapp/less/wildbook.less').pipe(less()).pipe(gulp.dest('src/main/webapp/css'));
});

gulp.task('watch', function() {
    gulp.watch(['src/main/webapp/less/*.less', 'src/main/templates/**/*.jade'], ['updatewar']);
    //gulp.watch('src/main/templates/**/*.jade', ['templates']);
});

gulp.task('updatewar', ['less', 'templates'], function() {
    if (!process.env.TOMCAT_HOME) {
        throw "Must have TOMCAT_HOME env variable set.";
    }
    
    doRsync({
        src: ['src/main/webapp/css', 'src/main/webapp/images', 'src/main/webapp/jade', 'src/main/webapp/javascript'],
        dest: process.env.TOMCAT_HOME + '/webapps/' + webapp
    });
});

gulp.task('updatewarclasses', function() {
    var cmd = 'mvn compile -o';
    var cust = argv.c || argv.cust;
    if (cust) {
        cmd += ' -Dwildbook.cust=' + cust;
    }
    run(cmd);
    
    doRsync({
        src: ['target/classes'],
        dest: process.env.TOMCAT_HOME + '/webapps/' + webapp + '/WEB-INF/classes'
    });
});

gulp.task('updatewartools', function() {
    doRsync({
        src: ['src/main/webapp/tools', 'src/main/webapp/bcomponents'],
        dest: process.env.TOMCAT_HOME + '/webapps/' + webapp
    });
});
