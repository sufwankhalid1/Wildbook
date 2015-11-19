var gulp = require('gulp');
var jade = require('gulp-jade');
var templateCache = require('gulp-angular-templatecache');
var less = require('gulp-less');
var watch = require('gulp-watch');

gulp.task('templates', function() {
    return gulp.src('src/main/templates/**/*.jade').pipe(jade())
        .pipe(templateCache()).pipe(gulp.dest('src/main/webapp/javascript'));
});

gulp.task('less', function() {
    return gulp.src('src/main/webapp/less/wildbook.less').pipe(less()).pipe(gulp.dest('src/main/webapp/css'));
});

gulp.task('watch', function() {
    gulp.watch('src/main/webapp/less/*.less', ['less']);
    gulp.watch('src/main/templates/**/*.jade', ['templates']);
});
