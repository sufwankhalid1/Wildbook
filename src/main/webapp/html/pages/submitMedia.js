//
// This makes it so that it appears in the chrome debugger as a source so
// you can set breakpoints and debug. It shows up under the "(no domain)" section.
// By default, scripts loaded via ajax (in this case the jquery.load() method will
// not appear in the debugger.
//

var submitMedia = (function () {
    'use strict';
//    var smms;
    
//    function guid() {
//        function s4() {
//          return Math.floor((1 + Math.random()) * 0x10000)
//            .toString(16)
//            .substring(1);
//        }
//        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
//          s4() + '-' + s4() + s4() + s4();
//    }
    
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
//                smms = $scope;
                
                $scope.media = {"username": (wildbookGlobals) ? wildbookGlobals.username : null, "endTime": null, "startTime": null};
//                $scope.msModel = null;
//                $scope.uuid = guid();
                
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
//                    console.log(JSON.stringify(this.media));
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
                
                $scope.addSubmission = function() {
//                    //
//                    // TODO: Make a function on the base class that takes
//                    // an object with attributes and sets all of the values
//                    // check for ownProperty?
//                    //
//                    if (!$scope.msModel) {
//                        $scope.msModel = new wildbook.Model.MediaSubmission();
//                    }
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
//                        $scope.msModel.set("latitude", media.latitude);
//                        $scope.msModel.set("longitude", media.longitude);
                    
                    var media = this.media;
                    $.post("obj/mediasubmission/save", this.media)
                        .success(function(data) {
                            media.id = data;
                            
                            //
                            // TODO: Why is the controller not working here with angular?!
                            //       I have to set this manually it seems.
                            //
                            $("[name='mediaid']").val(data);
                         })
                         .error(function(ex) {
                             console.log(JSON.stringify(ex.responseJSON));
                         });
                    
                    //
                    // When I use this to save the media it comes across as
                    // all nulls even though the network traffic seems to look fine.
                    // Weird.
                    //
//                    $scope.msModel.save(media, {"success": function(result) {
////                        if (!media.submissionid) {
////                            return;
////                        }
////                        var ms = result;
////                        var query = new wildbook.Collection.Surveys();
////                        query.fetch({
////                            "fields": {"surveyId": media.submissionid},
////                            "success": function(data) {
////                                var survey;
////                                if (!data.models.length) {
////                                    //
////                                    // No survey found.
////                                    //
////                                    return;
////                                    // for testing
////                                    //survey = new wildbook.Model.Survey({"surveyId": media.submissionid});
////                                } else {
////                                    //
////                                    // Just pick first for now. We should probably have a unique
////                                    // index on surveyId so that you can only get one anyway.
////                                    //
////                                    survey = data.models[0];
////                                }
////                                
////                                if (!medias) {
////                                    medias = [];
////                                }
////                                medias.push(ms.attributes);
////                                survey.set("media", medias);
////                                
////                                survey.save();
//                        alert("ID = " + $scope.msModel.get("id"));
//                    },
//                    "error": function(jqXHR, ex) {
//                        console.log(ex.status + ": " + ex.statusText);
//                    }});
                };
  
                $scope.completeWizard = function() {
                    alert('Completed!');
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
