//
// This makes it so that it appears in the chrome debugger as a source so
// you can set breakpoints and debug. It shows up under the "(no domain)" section.
// By default, scripts loaded via ajax (in this case the jquery.load() method will
// not appear in the debugger.
//

var submitMedia = (function () {
    'use strict';
    //
    // With the datetimepicker add-on the defaultDate: null option doesn't work
    // and I can't for the life of me figure it out. What happens is that it instead
    // defaults to the current date with 00:00 time. So I just check if that is
    // what the string is I then assume that the user did not enter a date.  If
    // someone does choose the current date at midnight then this will be a problem.
    // Let's hope they don't?
    //
    function isNullDate(dateString) {
        var date = new Date();
        var year = date.getFullYear();
        var month = (1 + date.getMonth()).toString();
        month = month.length > 1 ? month : '0' + month;
        var day = date.getDate().toString();
        day = day.length > 1 ? day : '0' + day;
        return (year + '-' + month + '-' + day + ' 00:00' === dateString);
      }
    
    var wizard = angular.module('MediaSubmissionWizard',
                                ['rcWizard', 'rcForm', 'rcDisabledBootstrap', 'ui.date', 'blueimp.fileupload']);
    wizard.controller('MediaSubmissionController',
            ['$scope', '$q', '$timeout',
             function ($scope, $q, $timeout) {
                $scope.media = {"username": (wildbookGlobals) ? wildbookGlobals.username : null, "endTime": null, "startTime": null};
                
                $scope.dateOptions = {
                        changeMonth: true,
                        changeYear: true,
                        dateFormat: 'yy-mm-dd',
//                        dateFormat: '@',
                        showTime: true,
                        defaultDate: null};
                
                $scope.isLoggedIn = function() {
                    return (this.media.username);
                };
                
                $scope.getSurvey = function() {
                    //
                    // I guess we don't need to verify the Survey here unless we
                    // decide to do something with it. Leaving this method here
                    // because you can find where it is used in the model
                    // in case you want to do something.
                    //
                };
                
                $scope.addSubmission = function() {
                    if (this.media.endTime && ! isNullDate(this.media.endTime)) {
                        this.media.endTime = new Date(this.media.endTime).getTime();
                    } else {
                        this.media.endTime = null;
                    }
                    if (this.media.startTime && ! isNullDate(this.media.startTime)) {
                        this.media.startTime = new Date(this.media.startTime).getTime();
                    } else {
                        this.media.startTime = null;
                    }
                    
                    var media = this.media;
                    $.post("obj/mediasubmission/save", this.media)
                        .success(function(data) {
                            media.id = data;
                            $scope.mediaid = data;
                            
                            //
                            // TODO: Why is the controller not working here with angular?!
                            //       I have to set this manually it seems.
                            //
                            $("[name='mediaid']").val(data);
                         })
                         .error(function(ex) {
                             //
                             // TODO: Write code for displaying a message box showing our error.
                             //
                             console.log(JSON.stringify(ex.responseJSON.message));
                             console.log(JSON.stringify(ex.responseJSON.totalStackTrace));
                         });
                };
  
                $scope.completeWizard = function() {
                    $("#MediaSubmissionWizard").addClass("hidden");
                    $("#MediaSubmissionThankYou").removeClass("hidden");
                };
                
//                $scope.$watch("media.startTime", function(newValue, oldValue) {
//                    console.log("startTime changed from [" + oldValue + "] to [" + newValue + "]");
//                    console.log(new Error().stack);
//                });
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
//    
//    return {"log": function() {
//        console.log(JSON.stringify(smms.media));
//    }};
})();
