module.exports = function(grunt) {
  /*
  * Run 'grunt -v' to generate the css assets and watch
  */
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-watch');

  var target = grunt.option('target') || 'happywhale';

  grunt.initConfig({
    less: {
      dist: {
        files: {
          'css/wildbook.css': 'less/wildbook.less'
        },
      },
    },
    watch: {
      css: {
        files: ['less/**'],
        tasks: ['less'],
      },
    },
  });

  grunt.registerTask('default', ['less', 'watch']);
};