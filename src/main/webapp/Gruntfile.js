module.exports = function(grunt) {

  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-watch');

  /*
  * Deploy from a different cust scss:
  * $ grunt sass -target='<custName>'
  */
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