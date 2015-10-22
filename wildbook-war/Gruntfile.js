module.exports = function(grunt) {
  /*
  * Run 'grunt -v' to generate the css assets
  */
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-watch');

  var target = grunt.option('target') || 'happywhale';

  grunt.initConfig({
    less: {
      dist: {
        files: {
          'src/main/webapp/css/wildbook.css': 'src/main/webapp/less/wildbook.less'
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

  grunt.registerTask('default', ['less']);
  grunt.registerTask('lesswatch', ['less', 'watch']);
};
