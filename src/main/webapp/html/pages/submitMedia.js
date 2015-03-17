//
// This makes it so that it appears in the chrome debugger as a source so
// you can set breakpoints and debug. It shows up under the "(no domain)" section.
// By default, scripts loaded via ajax (in this case the jquery.load() method will
// not appear in the debugger.
//

var submitMedia = (function () {
    'use strict';
//    var smms;
    
    function guid() {
        function s4() {
          return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
          s4() + '-' + s4() + s4() + s4();
    }
    
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
                $scope.media = {"username": wildbookGlobals.username, "endTime": null, "startTime": null};
                $scope.msModel = null;
                $scope.uuid = guid();
                
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
                
                $scope.addSubmission = function() {
                    function saveSubmission(media, survey) {
                        //
                        // TODO: Make a function on the base class that takes
                        // an object with attributes and sets all of the values
                        // check for ownProperty?
                        //
                        if (!$scope.msModel) {
                            $scope.msModel = new wildbook.Model.MediaSubmission();
                        }
                        $scope.msModel.set("username", media.username);
                        $scope.msModel.set("name", media.name);
                        $scope.msModel.set("email", media.email);
                        $scope.msModel.set("submissionid", media.submissionid);
                        $scope.msModel.set("description", media.description);
                        $scope.msModel.set("verbatimLocation", media.verbatimLocation);
                        if (media.endTime && ! isNullDate(media.endTime)) {
                            $scope.msModel.set("endTime", new Date(media.endTime).getTime());
                        } else {
                            $scope.msModel.set("endTime", null);
                        }
                        if (media.startTime && ! isNullDate(media.startTime)) {
                            $scope.msModel.set("startTime", new Date(media.startTime).getTime());
                        } else {
                            $scope.msModel.set("startTime", null);
                        }
//                        $scope.msModel.set("latitude", media.latitude);
//                        $scope.msModel.set("longitude", media.longitude);

                        if (survey) {
                            //
                            //TODO
                            // This seems only necessary (check this) if we are attaching this as
                            // a many-to-one relationship to survey. Maybe always
                            // do this? But also make Base.js do this automatically?
                            //
                            $scope.msModel.set("class", "org.ecocean.media.MediaSubmission");
                            
                            var medias = survey.get("media");
                            
                            if (!medias) {
                                medias = [];
                                survey.set("media", medias);
                            }
                            medias.push($scope.msModel);
                            //
                            // Do we need to set again if pushing above?
                            //
//                                survey.set("media", ms);
                            survey.save();
                        } else {
//                            ms.set("class", "org.ecocean.media.MediaSubmission");
                            $scope.msModel.save();
                        }
                        
//                        ms.save({"success": function() {
//                                    attachMedia();
//                                 },
//                                 "error": function(jqXHR, ex) {
//                                     console.log(ex.status + ": " + ex.statusText);
//                                 }});
                    }
                    
                    //
                    // Fetch the survey by "ID". (We probably want to add a user-specified survey name?
                    // If the survey exists (use a callback method to fetch({success: function(){})) then
                    // after submitting the MediaSubmission (further use of a callback) add the mediasubmission
                    // to the survey.
                    //
                    var survey = new wildbook.Model.Survey({"id": this.media.submissionid});
                    var mediasub = this.media;
                    survey.fetch({"success": function() {
                                      if (mediasub.submissionid) {
                                          saveSubmission(mediasub, survey);
                                      } else {
                                          //
                                          // The user did'nt specify a submissionid which results
                                          // in a successful return from the fetch but with no survey.
                                          // Sigh. DataNucleus.
                                          //
                                          saveSubmission(mediasub);
                                      }
                                  },
                                  "error": function(jqXHR, ex) {
                                      //console.log(ex.status + ": " + ex.statusText);
                                      
                                      //
                                      // NOTE: Stupidly the DataNucleus rest api throws a
                                      // 500 error if the requested object does not exist.
                                      // So that you can't tell what really happened.
                                      // It will say "No such database row" if you can find the cause
                                      // of the 500 error and you want to just do the following
                                      // if you get that specific error. For now, I'm just going to
                                      // assume all errors mean that it just didn't find it.
                                      //
                                      saveSubmission(mediasub);
                                  }});
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
