module.exports = function(grunt) {

  grunt.loadNpmTasks('grunt-contrib-sass');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-concat-css');

  
  grunt.initConfig({
    sass: {
      dist: {
        files: {
          'public/css/app.css': 'public/scss/app.scss',
          'public/cust/css/cust.css': 'public/cust/scss/cust.scss'
        },
      },
    },
    concat_css: {
      dist: {
        src: ['public/vendor/leaflet/leaflet.css',
             'public/vendor/leaflet.markercluster/MarkerCluster.css',
             'public/bcomponents/Leaflet.EasyButton/easy-button.css',
             'public/vendor/angularjs-utilities/css/rcWizard.css',
             'https://blueimp.github.io/Gallery/css/blueimp-gallery.min.css',
             'public/vendor/photoswipe/photoswipe.css',
             'public/vendor/photoswipe/default-skin/default-skin.css',
             'public/vendor/jquery-fileupload/css/jquery.fileupload.css',
             'public/vendor/jquery-fileupload/css/jquery.fileupload-ui.css',
             'public/vendor/zocial/zocial.css',
             'public/vendor/alertplus/css/alertplus.css'],
        dest: 'public/css/vendors.css',
      },
    },
    watch: {
      css: {
        files: ['public/scss/**', 'public/cust/scss/**'],
        tasks: ['sass'],
      },
    },
  });
  
  grunt.registerTask('default', ['sass', 'concat']);
  grunt.registerTask('devwatch', ['watch']);
  grunt.registerTask('concat', ['concat_css']);
};