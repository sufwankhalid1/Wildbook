//
// To add a new library to the package.json file run
// e.g. npm install gulp-rsync --save-dev
//
'use strict';

var gulp = require('gulp');
var gutil = require('gulp-util');

var jade = require('gulp-jade');
var templateCache = require('gulp-angular-templatecache');
var less = require('gulp-less');
var rsync = require('rsyncwrapper').rsync;
var argv = require('yargs').argv;
//var debug = require('gulp-debug');
//var fs = require('fs');
var browserify = require('browserify');
var source = require("vinyl-source-stream");
var watchify = require("watchify");
var path = require("path");
var concatCss = require('gulp-concat-css');
var del = require('del');
var runSequence = require('run-sequence');
var exec = require('child_process').exec;

var webapp = argv.w || argv.webapp || "wildbook";

//===============================
// Build paths to use
//===============================

var paths = {
    src: path.join('src', 'main'),
    target: 'target'
};

paths.srcjs = path.join(paths.src, 'javascript');
paths.webapp = path.join(paths.src, 'webapp');
paths.less = path.join(paths.src, 'less');

paths.js = path.join(paths.webapp, 'javascript');
paths.css = path.join(paths.webapp, 'css');

paths.dist = path.join(paths.target, 'dist');

paths.distcss = path.join(paths.dist, 'css');
paths.distjs = path.join(paths.dist, 'javascript');

paths.mainjs = path.join(paths.srcjs, 'main.js');

//===============================
// END Build paths to use
//===============================

function reportError(ex) {
    gutil.log(gutil.colors.yellow.bgRed('Error: '), ex.message);
}

function doBundling(bundler, output) {
    return bundler.bundle()
        .on('error', reportError)
        .pipe(source(output))
        .pipe(gulp.dest(paths.distjs));
}

function execute(cmd, callback) {
    return exec(cmd, function(err, stdout, stderr) {
        console.log(stdout);
        console.log(stderr);
        if (err) {
            reportError(err);
        } else {
            if (callback) {
                callback();
            }
        }
    });
}

function doRsync(opts) {
    opts.recursive = true;

    rsync(opts,
        function(error, stdout, stderr, cmd) {
            if (error) {
                gutil.log(gutil.colors.magenta('Error: '), error.message);
            }
        }
    );
}

function getBundler(files, output, minify) {
    var bundler;

    if (minify) {
        let debugable = ! (argv.nomap);

        bundler = new browserify({debug: debugable});

        //bundler.transform('browserify-css', {global: true});

        if (debugable) {
            bundler.plugin('minifyify', {
                output: path.join(paths.distjs, output + '.map'),
                map: output + '.map'
            });
        } else {
            bundler.plugin('minifyify', {map: false});
        }
    } else {
        bundler = new browserify();
    }

    bundler.add(files);

    return bundler;
}

function getMainBundler(minify) {
    return getBundler(paths.mainjs, 'bundle.js', minify);
}

function subdirs(filepath, filter) {
    return path.join(filepath, "**", filter || "*");
}

//
//  Putting this in a function so that people not using the watch functions
//  don't have to have TOMCAT_HOME set. This throws an error if it's not set.
//
function getDevDir() {
    return path.join(process.env.TOMCAT_HOME, 'webapps', webapp);
}

function updatewar() {
    if (!process.env.TOMCAT_HOME) {
        throw "Must have TOMCAT_HOME env variable set.";
    }
    //
    // Not sure I want the overhead of copying the images every time. unncessary for me since
    // I'm not usually changing those. Move this to another task if you want to have an easy way to
    // just update those?
    //
//    gulp.src(subdirs(path.join(paths.webapp, 'images')), {base: paths.webapp})
//         .pipe(gulp.dest(getDevDir()));

    gulp.src([subdirs(paths.css),
              subdirs(path.join(paths.webapp, 'jade')),
              subdirs(paths.js)], {base: paths.webapp})
         .pipe(gulp.dest(getDevDir()));

    gulp.src(path.join(paths.webapp, '*.jsp')).pipe(gulp.dest(getDevDir()));

    gulp.src(subdirs(paths.dist), {base: paths.dist})
        .pipe(gulp.dest(getDevDir()));
}

gulp.task('templates', function() {
    return gulp.src('src/main/templates/**/*.jade').pipe(jade())
        .pipe(templateCache()).pipe(gulp.dest(paths.distjs));
});

gulp.task('less', function() {
    return gulp.src(path.join(paths.less, 'wildbook.less'))
        .pipe(less()).pipe(gulp.dest(paths.distcss));
});

gulp.task('watch', function() {
    //
    // Set up a watch for our less and jade templates.
    //
    gulp.watch([subdirs(paths.less, '*.less'),
                subdirs(path.join(paths.src, 'templates'), '*.jade')], ['updatewar']);
    gulp.watch(subdirs(paths.webapp, '*.jsp'), ['updatewar']);

    //
    // Set up watchify for our javascript code.
    //
    let watcher = watchify(getMainBundler(false));

    function bundle() {
        return doBundling(watcher, 'bundle.js').on('end', updatewar);
    }

    // Listen for changes to paths.mainjs or any of its dependencies
    watcher.on('update', bundle)
           .on('time', function(time) {
               gutil.log('Browserified js in', gutil.colors.magenta(time + " ms"));
            });

    // Run it right away.
    return bundle();
});


gulp.task('concat-tools', function() {
    gulp.src(['node_modules/ag-grid/dist/ag-grid.min.css',
              'node_modules/ag-grid/dist/theme-fresh.min.css',
              'node_modules/angular-busy/dist/angular-busy.min.css',
              'node_modules/angular-material/angular-material.min.css',
              'node_modules/angular-material/angular-material.layouts.min.css',
              'node_modules/leaflet/dist/leaflet.css',
              'node_modules/leaflet.markercluser/dist/MarkerCluster.Default.css',
              'node_modules/leaflet.markercluser/dist/MarkerCluster.css',
              'src/vendor/png-time-input/png-time-input.css'],
               {base:'node_modules' })
         .pipe(concatCss('tools.css'))
         .pipe(gulp.dest(paths.distcss));

    gulp.src('node_modules/jquery/dist/jquery.min.js', {base: 'node_modules/jquery/dist'})
    .pipe(gulp.dest(paths.distjs));

    gulp.src('node_modules/leaflet/dist/images/**', {base:'node_modules'})
    .pipe(gulp.dest(paths.distcss));
});

gulp.task('updatewar', ['less', 'templates'], function() {
    updatewar();
});

gulp.task('updatewar-classes', function() {
    var cmd = 'mvn compile -o';
    var cust = argv.c || argv.cust;
    if (cust) {
        cmd += ' -Dwildbook.cust=' + cust;
    }

    execute(cmd, function(err) {
            doRsync({
                src: [path.join(paths.target, 'classes')],
                dest: path.join(getDevDir(), 'WEB-INF', 'classes')
            });
        }
    );
});

gulp.task('updatewartools', function() {
    doRsync({
//        src: [path.join(paths.webapp, 'tools'), path.join(paths.webapp, 'bcomponents')],
        src: [path.join(paths.webapp, 'tools')],
        dest: getDevDir()
    });
    // doRsync({
    //     src: [path.join(paths.distjs, 'tools-bundle.*')],
    //     dest: getDevDir()
    // });
});

gulp.task('clean', function() {
    return del('target');
});

gulp.task('build', function() {
    //
    // TODO: Once gulp 4.0 is out you can replace this with the native series task capability
    //
    runSequence(
        'clean',
        ['less', 'templates', 'browserify-tools', 'concat-tools', 'browserify'],
        function() {
            execute('mvn install');
        }
    );
});

gulp.task('browserify-tools', function() {
    return doBundling(getBundler(path.join(paths.srcjs, 'tools.js'), 'tools-bundle.js', true), 'tools-bundle.js');
});

gulp.task('browserify', function() {
    //
    // TODO: For now default to not minifying our code because there is a problem with angular.
    // I think we are defining one of our directives/functions without the list of variables but
    // can't spot it right off.  Until then we default to not minifying. If we fix we can go back
    // to defaulting to minifying.
    //
    var minify;
//    if (argv.nominify) {
//        minify = false;
//    } else {
//        minify = true;
//    }
    minify = argv.minify;

    return doBundling(getMainBundler(minify), 'bundle.js');
});
