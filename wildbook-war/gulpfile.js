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
var debug = require('gulp-debug');
var fs = require('fs');
var browserify = require('browserify');
var source = require("vinyl-source-stream");
var watchify = require("watchify");
var path = require("path");
var concat = require('gulp-concat');
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

paths.devdeploy = path.join(process.env.TOMCAT_HOME, 'webapps', webapp);
paths.mainjs = path.join(paths.srcjs, 'main.js');

//===============================
// END Build paths to use
//===============================

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

gulp.task('templates', function() {
    return gulp.src('src/main/templates/**/*.jade').pipe(jade())
        .pipe(templateCache()).pipe(gulp.dest(paths.distjs));
});

gulp.task('less', function() {
    return gulp.src(path.join(paths.less, 'wildbook.less'))
        .pipe(less()).pipe(gulp.dest(paths.distcss));
});
    
gulp.task('watch', function() {
    gulp.watch([subdirs(path.join(paths.webapp, 'less'), '*.less'),
                subdirs(path.join(paths.src, 'templates'), '*.jade')], ['updatewar']);
});


//'node_modules/jquery-ui/themes/base/minified/jquery-ui.min.css'
gulp.task('concat-tools', function() {
    gulp.src(['node_modules/ag-grid/dist/ag-grid.min.css',
              'node_modules/ag-grid/dist/theme-fresh.min.css',
              'node_modules/angular-busy/dist/angular-busy.min.css',
              'node_modules/angular-material/angular-material.min.css'])
         .pipe(concat('tools.css'))
         .pipe(gulp.dest(paths.distcss));
    gulp.src('node_modules/jquery/dist/jquery.min.js', {base: 'node_modules/jquery/dist'})
    .pipe(gulp.dest(paths.distjs));

//    gulp.src(['node_modules/jquery-ui/autocomplete.js', 'node_modules/jquery-ui/core.js'])
//    .pipe(concat('jquery-ui.js'))
//    .pipe(gulp.dest(paths.distjs));
});

function subdirs(filepath, filter) {
    return path.join(filepath, "**", filter || "*");
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
//         .pipe(gulp.dest(paths.devdeploy));
    
    gulp.src([subdirs(paths.css),
              subdirs(path.join(paths.webapp, 'jade')),
              subdirs(paths.js)], {base: paths.webapp})
         .pipe(gulp.dest(paths.devdeploy));

    gulp.src(path.join(paths.webapp, '*.jsp')).pipe(gulp.dest(paths.devdeploy));
    
    gulp.src(subdirs(paths.dist), {base: paths.dist})
        .pipe(gulp.dest(paths.devdeploy));
}

gulp.task('updatewar', function() {
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
                dest: path.join(paths.devdeploy, 'WEB-INF', 'classes')
            });
        }
    );
});

gulp.task('updatewartools', function() {
    doRsync({
//        src: [path.join(paths.webapp, 'tools'), path.join(paths.webapp, 'bcomponents')],
        src: [path.join(paths.webapp, 'tools')],
        dest: paths.devdeploy
    });
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

function getBundler(files, output, nominify) {
    var bundler;
    
    if (nominify) {
        bundler = new browserify();
    } else {
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
    }

    bundler.add(files);

    return bundler;
}

function getMainBundler() {
    return getBundler(paths.mainjs, 'bundle.js', argv.nominify);
}

function doBundling(bundler, output) {
    return bundler.bundle()
        .on('error', reportError)
        .pipe(source(output))
        .pipe(gulp.dest(paths.distjs));
}

function reportError(ex) {
    gutil.log(gutil.colors.yellow.bgRed('Error: '), ex.message);
}

gulp.task('browserify-tools', function() {
    return doBundling(getBundler(path.join(paths.srcjs, 'tools.js'), 'tools-bundle.js'), 'tools-bundle.js', false);
});

gulp.task('browserify', function() {
    return doBundling(getMainBundler(), 'bundle.js');
});

gulp.task('watchify', function() {
    let watcher = watchify(getMainBundler());

    function bundle() {
        return doBundling(watcher, 'bundle.js').on('end', updatewar);
    }

    // Listen for changes to paths.mainjs or any of its dependencies
    watcher.on('update', bundle)
           .on('time', function(time) {gutil.log('Compiled JS in', time);});

    // Run it right away.
    return bundle();
});
