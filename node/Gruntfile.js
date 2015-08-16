module.exports = function(grunt) {

  grunt.loadNpmTasks('grunt-contrib-sass');
  grunt.loadNpmTasks('grunt-contrib-watch');

  
  grunt.initConfig({
    sass: {
      dist: {
        files: {
          'public/css/app.css': 'public/scss/app.scss',
          'public/css/vendors.css': 'public/scss/vendors.scss',
          'public/cust/css/cust.css': 'public/cust/scss/cust.scss'
        },
      },
    },
    watch: {
      css: {
        files: ['public/scss/**', 'public/cust/scss/**'],
        tasks: ['sass'],
      },
    },
  });
  
  grunt.registerTask('default', ['sass']);
  grunt.registerTask('devwatch', ['watch']);
};