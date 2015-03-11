//
// This makes it so that it appears in the chrome debugger as a source so
// you can set breakpoints and debug. It shows up under the "(no domain)" section.
// By default, scripts loaded via ajax (in this case the jquery.load() method will
// not appear in the debugger.
//

var submitMedia = (function () {
    'use strict';
    
    var wizard = angular.module('MediaSubmissionWizard',
                                ['rcWizard', 'rcForm', 'rcDisabledBootstrap', 'ui.date', 'blueimp.fileupload']);
    wizard.controller('MediaSubmissionController',
            ['$scope', '$q', '$timeout',
             function ($scope, $q, $timeout) {
                $scope.media = {"username": wildbookGlobals.username};
  
                $scope.dateOptions = {
                        changeMonth: true,
                        changeYear: true,
                        dateFormat: 'yy-mm-dd',
                        showTime: true};
                
                $scope.isLoggedIn = function() {
                    return (this.media.username);
                };
                
                $scope.setStartTime = function() {
                    
                };
                
                $scope.showSecond = function() {
//                    $("#media-startdatepicker").datetimepicker({
//                        changeMonth: true,
//                        changeYear: true,
//                        dateFormat: 'yy-mm-dd',
//                        defaultDate: '2010-09-13'});
//                    $("#media-startdatepicker").datetimepicker( $.timepicker.regional[ "en" ] );
                };
                
                $scope.showFileUpload = function() {
                    console.log("showFileUpload: " + JSON.stringify(this.media));
//                    initUpload();
                };
                
                $scope.getSurvey = function() {
                    console.log(JSON.stringify(this.media));
                    //
                    // I guess we don't need to verify the Survey here unless we
                    // decide to do something with it.
                    //
//                    var s = new wildbook.Model.Survey({"id":this.media.surveyid});
//                    s.fetch();
                    
                    //
                    // Just showing what you can do with deferreds and resolve using angular
                    //
//                    var deferred = $q.defer();
//    
//                    $timeout(function() {
//                        deferred.resolve();
//                    }, 1000);
//    
//                    return deferred.promise;
                };
  
                $scope.completeWizard = function() {
                    alert('Completed!');
                };
            }]);

    var url = "mediaupload";
    
    wizard.config(['$httpProvider',
                 'fileUploadProvider',
                 function ($httpProvider, fileUploadProvider) {
                    delete $httpProvider.defaults.headers.common['X-Requested-With'];
                    fileUploadProvider.defaults.redirect = window.location.href.replace(
                            /\/[^\/]*$/,
                            '/cors/result.html?%s'
                    );
                    // Demo settings:
                    angular.extend(fileUploadProvider.defaults, {
                        // Enable image resizing, except for Android and Opera,
                        // which actually support image resizing, but fail to
                        // send Blob objects via XHR requests:
                        disableImageResize: /Android(?!.*Chrome)|Opera/
                            .test(window.navigator.userAgent),
                            maxFileSize: 5000000,
                            acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i
                    });
                 }
            ])
        .controller('DemoFileUploadController',
                ['$scope', '$http', '$filter', '$window',
                 function ($scope, $http) {
                    $scope.options = {
                        url: url
                    };
                    $scope.loadingFiles = true;
                    $http.get(url)
                    .then(function (response) {
                            $scope.loadingFiles = false;
                            $scope.queue = response.data.files || [];
                        },
                        function () {
                            $scope.loadingFiles = false;
                        }
                    );
                }
                ])
           .controller('FileDestroyController',
                   ['$scope', '$http',
                    function ($scope, $http) {
                       var file = $scope.file, state;
                       if (file.url) {
                           file.$state = function () {
                               return state;
                           };
                           file.$destroy = function () {
                               state = 'pending';
                               return $http({
                                   url: file.deleteUrl,
                                   method: file.deleteType
                               }).then(
                                       function () {
                                           state = 'resolved';
                                           $scope.clear(file);
                                       },
                                       function () {
                                           state = 'rejected';
                                       }
                               );
                           };
                       } else if (!file.$cancel && !file._index) {
                           file.$cancel = function () {
                               $scope.clear(file);
                           };
                       }
                   }
                   ]);

    
    function guid() {
        function s4() {
          return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
          s4() + '-' + s4() + s4() + s4();
    }
      
    function initUpload() {
//        // Initialize the jQuery File Upload widget:
////        $('#fileupload').fileupload({
////            // Uncomment the following to send cross-domain cookies:
////            //xhrFields: {withCredentials: true},
////            url: 'placeholder'
////        });
//        $("#fileupload").fileupload();
//        
//        //
//        // Create uuid for upload. Will need a way to refresh this with new downloads.
//        //
//        $('#fileupload_uuid').val(guid());
//                
//        // Enable iframe cross-domain access via redirect option:
//    //    $('#fileupload').fileupload(
//    //        'option',
//    //        'redirect',
//    //        window.location.href.replace(
//    //            /\/[^\/]*$/,
//    //            '/cors/result.html?%s'
//    //        )
//    //    );
//    
//        // Load existing files
//        $('#fileupload').addClass('fileupload-processing');
////        var url = $('#fileupload').fileupload('option', 'url');
////        var url = "media/test?data=testing";
////        var url = "http://localhost:8888/";
//        $.ajax({
//            // Uncomment the following to send cross-domain cookies:
//            //xhrFields: {withCredentials: true},
//            type: "POST",
//            url: "mediaupload",
//            dataType: 'json',
//            context: $('#fileupload')[0]
//        }).always(function () {
//            $(this).removeClass('fileupload-processing');
//        }).done(function (result) {
////            alert(JSON.stringify(result));
//            $(this).fileupload('option', 'done')
//                   .call(this, $.Event('done'), {result: result});
//        }).fail(function( jqXHR, textStatus, errorThrown ) {
//            console.log(JSON.stringify(jqXHR));
//        });
    }
        
    return {"init": initUpload};
})();
