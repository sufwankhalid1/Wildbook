//
// This makes it so that it appears in the chrome debugger as a source so
// you can set breakpoints and debug. It shows up under the "(no domain)" section.
// By default, scripts loaded via ajax (in this case the jquery.load() method will
// not appear in the debugger.
//

var submitMedia = (function () {
    'use strict';

    var map = L.map('mediasubmissionmap');
    var marker = null;
    var hasGPS = false;

    L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 18
    }).addTo(map);

    function addToMap(latitude, longitude) {
        if (marker) {
            map.removeLayer(marker);
        }
        if (latitude && longitude) {
            marker = L.marker(L.latLng(latitude, longitude)).addTo(map);
            map.setView(latlng, 2);
        }

        //
        // Fixes problem with bootstrap keeping the map div
        // from properly auto-sizing.
        //
//        setTimeout(function () {
//            map.invalidateSize();
//        }, 1000);
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
            map.on('click', function(event) {
                if (! hasGPS) {
                    $scope.$apply(function() {
                        $scope.media.latitude = event.latlng.lat;
                        $scope.media.longitude = event.latlng.lng;
                    });
//                    addToMap(latlng);
                }
            });

            function urlParam(name) {
                var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
                if (results==null){
                   return null;
                }
                else{
                   return results[1] || 0;
                }
            }

            //
            // NOTE: This is within an apply already so no need to wrap it in one.
            // In fact we CAN'T because it throws an error since it is already
            // in an apply. Can't run an apply within an apply.
            //
            $scope.media = {
                "username": (wildbookGlobals) ? wildbookGlobals.username : null,
                "submissionid": urlParam('submissionid'),
                "email": urlParam('email'),
                "name": urlParam('name'),
                "endTime": null,
                "startTime": null
            };

            function longToDate(date) {
                if (date) {
                    return new Date(date);
                }

                return null;
            }

            function savems(media, method) {
                //
                // Don't alter the object directly because it causes
                // a conflict between the date string of the control and
                // the long we use here.
                //
                var ms = $.extend({}, media);
                if (ms.endTime && ! isNullDate(ms.endTime)) {
                    ms.endTime = new Date(ms.endTime).getTime();
                } else {
                    ms.endTime = null;
                }
                if (ms.startTime && ! isNullDate(ms.startTime)) {
                    ms.startTime = new Date(ms.startTime).getTime();
                } else {
                    ms.startTime = null;
                }

                return $.post("obj/mediasubmission/" + method, ms)
                 .fail(function(ex) {
                     wildbook.showError(ex);
                 });
            }

            //
            // showTime = true is my addition to angular-ui/ui-date
            // which allows it to use the timepicker. See the mymaster branch.
            //
            $scope.dateOptions = {
                changeMonth: true,
                changeYear: true,
                dateFormat: 'yy-mm-dd',
//                dateFormat: '@',
                showTime: true,
                defaultDate: null
            };

            $scope.isLoggedIn = function() {
                return (this.media.username);
            };

//                $scope.allMediaUploaded = function() {
//                    alert("allMediaUploaded");
//                    return false;
//                };

            $scope.getExifData = function() {
                //
                // Loop through files and make sure they are all uploaded before
                // continuing.
                var allUploaded = true;
                $.each(this.queue, function() {
                    //
                    // If our object in the queue does not have
                    // thumbnailUrl property set on it then it did not
                    // come from the server. So this is a janky, but I hope effective,
                    // way of knowing whether or not all files have been uploaded.
                    //
                    if (!this.thumbnailUrl) {
                        allUploaded = false;
                    }
                });

                if (!allUploaded) {
                    wildbook.showAlert("Please finish uploading (or canceling) all of your images before continuing.");
                    return $.Deferred().reject();
                }

                //
                // Make call to get xif data
                //
                var jqXHR = $.get('obj/mediasubmission/getexif/' + $scope.media.id)
                .done(function(data) {
                    var avg = data.avg;
                    //
                    // If we have lat/long data then we don't want to show
                    // the lat/long data but rather just the stuff on the map.
                    //
                    if (avg.latitude && avg.longitude) {
                        hasGPS = true;
                        //
                        // Hide lat/long boxes if we have exif data.
                        //
                        $("#mediasubmissionlatlong").addClass("hidden");

                        //
                        // Add markers to map.
                        //
                        $.each(data.items, function() {
                            if (this.latitude && this.longitude) {
                                L.marker(L.latLng(this.latitude, this.longitude)).addTo(map);
                            }
                        });

                        map.setView(L.latLng(avg.latitude, avg.longitude), 2);
                    } else {
                        map.setView([0,0], 1);
                    }

                    //
                    // Fixes problem with bootstrap keeping the map div
                    // from properly auto-sizing.
                    //
                    setTimeout(function () {
                        map.invalidateSize();
                    }, 1000);


                    $scope.$apply(function() {
                        $scope.media.startTime = longToDate(avg.minTime);
                        $scope.media.endTime = longToDate(avg.maxTime);
                        $scope.media.latitude = avg.latitude;
                        $scope.media.longitude = avg.longitude;
                    });
                })
                .fail(function(ex) {
                    wildbook.showError(ex);
                });

                return jqXHR.promise();
            };

            $scope.saveSubmission = function() {
                var jqXHR = savems($scope.media, "save")
                .done(function(mediaid) {
                    $scope.$apply(function(){
                        //
                        // NOTE: This is bound using ng-value instead of ng-model
                        // because it is a hidden element.
                        //
                        $scope.media.id = mediaid;
                    });
                });

                return jqXHR.promise();
            };

            $scope.completeWizard = function() {
                var jqXHR = savems(this.media, "complete")
                .done(function(mediaid) {
                    $("#MediaSubmissionWizard").addClass("hidden");
                    $("#MediaSubmissionThankYou").removeClass("hidden");
                });

                return jqXHR.promise();
            };

//                //
//                // We need these watch expressions to force the changes to the model
//                // to be reflected in the view.
//                //
//                $scope.$watch("media.startTime", function(newValue, oldValue) {
//                    console.log("startTime changed from [" + oldValue + "] to [" + newValue + "]");
//                    console.log(new Error().stack);
//                });
            $scope.$watch("media.latitude", function(newVal, oldVal) {
                if ($scope.media.longitude) {
                    addToMap(newVal, $scope.media.longitude);
                }
            });
            $scope.$watch("media.longitude", function(newVal, oldVal) {
                if ($scope.media.latitude) {
                    addToMap($scope.media.latitude, newVal);
                }
            });
        }
    ]);

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
                acceptFileTypes: /(\.|\/)(gif|jpe?g|png|kmz|kml)$/i
                //
                // This does work to control the area in which files are allowed
                // to be dropped to land in the control, but then if you drop it outside
                // the control Chrome things you want to view the images and opens them
                // thus taking you away from the wizard. Hitting the back button restarts
                // the wizard. Probably could figure out how to fix that...
                //
//                dropZone: $("#fileupload .dropbox")
            });
         }
    ])
    .controller('SubmissionFileUploadController',
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
            var file = $scope.file;
            if (file.url) {
                file.$destroy = function () {
                    return $http({
                        url: 'obj/mediasubmission/delfile/' + $scope.media.id,
                        data: this.name,
                        method: 'POST'
                    }).then(function () {
                                $scope.clear(file);
                            },
                            function () {
                                console.log("failed to delete");
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
