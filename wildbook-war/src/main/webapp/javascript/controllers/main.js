wildbook = {};

wildbook.app = angular.module('appWildbook', ["agGrid"]);
wildbook.app.factory('$exceptionHandler', function() {
    return function(ex, cause) {
        //
        // TODO: Make this configurable so that if on production the errors
        // are sent to the console instead of a dialog?
        //
        alertplus.error(ex);
      };
});

wildbook.app.factory("wbConfig", ["$http", "$exceptionHandler", function($http, $exceptionHandler) {
    var config;
    
    function getVessels(orgs, org) {
        //
        // Let's find our master organization so that we can add the vessels to
        // it and thus cache the results for future occerences of the user picking
        // this organization again in the list.
        //
        var orgfilter = orgs.filter(function(value) {
            return (value.orgId == org.orgId);
        });

        var orgmaster;
        if (orgfilter.length > 0) {
            orgmaster = orgfilter[0];
        } else {
            //
            // Just set it to org so we don't have to check for null below
            // but it should *never* not be an array of size 1
            //
            orgmaster = org;
        }

        if (orgmaster.vessels) {
            return $q.resolve(orgmaster.vessels);
        }

        return $http({url: "obj/survey/vessels/get", params: {orgid: org.orgId}})
        .then(function(results) {
            //
            // Set it here so that next time we ask for this org's vessels we
            // get the cached values.
            //
            orgmaster.vessels = results.data;
            return results.data;
        });
    }
    
    function getConfig() {
        return config;
    }
    
    $http({url:"util/init"})
    .then(function(result) {
        config = result.data;
        //
        // When everything is ready, initialize tooltips on the page
        //
        $('[data-toggle="tooltip"]').tooltip();
    }, $exceptionHandler);

    return {
        config: getConfig,
        getVessels: function(org) {
            return getVessels(config.orgs, org);
        }
    };
}]);

wildbook.app.factory("wbDateUtils", ["wbConfig", function(wbConfig) {
    function restToMoment(rest) {
        if (! rest) {
            return null;
        }

        if (rest.constructor === Array) {
            //
            // The month is zero-based in moment because javascript Date is also apparently. Sheeesh.
            // Sooo, because of that we can't just use the array constructor on moment
            //   e.g. moment(rest)
            // and therefore we have to do the following.
            //
//            return moment(rest);
            var mobj = {year: rest[0], month: rest[1] - 1, date: rest[2]};
            if (rest.length > 3) {
                mobj.hour = rest[3];
                mobj.minute = rest[4];
                mobj.second = rest[5];
            }
            return moment(mobj);
        }
        return null;
    }

    function formatMoment(moment) {
        if (moment) {
            if (moment.hour() === 0 && moment.minute() === 0 && moment.second() === 0) {
                return moment.format(wbConfig.config().props["moment.date.format"]);// || "YYYY-MM-DD");
            }
            return moment.format(wbConfig.config().props["moment.datetime.format"] || "YYYY-MM-DD hh:mm:ss");
        }
        return null;
    }

    return {
        dateToRest: function(date) {
            if (! date) {
                return null;
            }
            var mdate = moment(date);
            //
            // For some reason the month is zero-based and nothing else is. Sigh.
            //
            return [mdate.year(), mdate.month() + 1, mdate.date()];
        },
        dateFromRest: function(rest) {
            var moment = restToMoment(rest);
            if (moment) {
                return moment.toDate();
            }
            return null;
        },
        dateStringFromRest: function(rest) {
            return formatMoment(restToMoment(rest));
        },
        test: function() {
            alertplus.alert("hello");
        }
    };
}]);

wildbook.app.directive(
    'dateInput',
    ['wbDateUtils', function(wbDateUtils) {
        return {
            require: 'ngModel',
            template: '<input type="date"></input>',
            replace: true,
            link: function(scope, elm, attrs, ngModelCtrl) {
                ngModelCtrl.$formatters.push(function (modelValue) {
                    return wbDateUtils.dateFromRest(modelValue);
                });

                ngModelCtrl.$parsers.push(function(viewValue) {
                    return wbDateUtils.dateToRest(viewValue);
                });
            }
        };
    }]
);

wildbook.app.directive(
    'timeInput',
    function() {
        return {
            require: 'ngModel',
            template: '<input type="time"></input>',
            replace: true,
            link: function(scope, elm, attrs, ngModelCtrl) {
                ngModelCtrl.$formatters.push(function (modelValue) {
                    if (! modelValue) {
                        return null;
                    }

                    if (modelValue.constructor === Array) {
                        //
                        // Angular requires it to be a date object (even though it displays
                        // it as a time. So we turn it into a date.
                        //
                        var mdate = {hour: modelValue[0], minute: modelValue[1]};
                        if (modelValue[2] && typeof modelValue[2] === "number") {
                            mdate.seconds = modelValue[2];
                        }
                        //return moment(mdate).format("hh:mm:ss");
                        return moment(mdate).toDate();
                    }
                    return null;
                });

                ngModelCtrl.$parsers.push(function(viewValue) {
                    if (! viewValue) {
                        return null;
                    }

                    var mdate = moment(viewValue);
                    //
                    // TODO: Deal with timezone
                    //
                    return [mdate.hour(), mdate.minute(), mdate.second(), "+0"];
                });
            }
        };
});

wildbook.app.directive(
    'wbThumbBox',
    ["wbDateUtils", function(wbDateUtils) {
        return {
            restrict: 'E',
            scope: {
                photos: "=",
                delphoto: "&",
                numPhotos: "@"
            },
            templateUrl: 'util/render?j=partials/wb_thumb_box',
            replace: true,
            controller($scope) {
                var startIdx = 0;
                var idx;
                
                var numPhotos;
                if (! $scope.numPhotos) {
                    numPhotos = 18;
                } else {
                    numPhotos = parseInt($scope.numPhotos);
                }

                $scope.getTimestamp = function(photo) {
                    return wbDateUtils.dateStringFromRest(photo.timestamp);
                }
                
                function pageLeft() {
                    startIdx = startIdx - numPhotos;
                    if (startIdx < 0) {
                        startIdx = 0;
                    }
                }
                
                $scope.panLeft = function() {
                    if (! $scope.photos) {
                        return;
                    }
                    if ($scope.zoomimage) {
                        if (idx <= 0) {
                            return;
                        }
                        idx--;
                        $scope.zoomimage = $scope.photos[idx];
                        
                        //
                        // If we have panned far enough such that we are now on the next
                        // "page" of thumbnails, let's reflect that.
                        //
                        if (idx < startIdx) {
                            pageLeft();
                        }
                    } else {
                        pageLeft();
                    }
                }

                function pageRight() {
                    startIdx = startIdx + numPhotos;
                    if (startIdx + numPhotos > $scope.photos.length) {
                        startIdx = $scope.photos.length - numPhotos;
                        if (startIdx < 0) {
                            startIdx = 0;
                        }
                    }
                }
                
                $scope.panRight = function() {
                    if (! $scope.photos) {
                        return;
                    }
                    if ($scope.zoomimage) {
                        if (idx >= $scope.photos.length - 1) {
                            return;
                        }
                        idx++;
                        $scope.zoomimage = $scope.photos[idx];
                        
                        //
                        // If we have panned far enough such that we are now on the next
                        // "page" of thumbnails, let's reflect that.
                        //
                        if (idx >= startIdx + numPhotos) {
                            pageRight();
                        }
                    } else {
                        pageRight();
                    }
                }
                
                $scope.isLeftDisabled = function() {
                    if ($scope.zoomimage) {
                        return (idx <= 0);
                    }
                    return (startIdx <= 0);
                }
                
                $scope.isRightDisabled = function() {
                    if ($scope.zoomimage) {
                        return (idx >= $scope.photos.length - 1);
                    }
                    
                    if (! $scope.photos) {
                        return true;
                    }
                    return (startIdx >= $scope.photos.length - numPhotos);
                }
                
                $scope.viewImage = function(photo) {
                    $scope.zoomimage = photo;
                    
                    $scope.photos.forEach(function(value, index) {
                        if (value.id === photo.id) {
                            idx = index;
                        }
                    });
                }
                
                $scope.getVisPhotos = function() {
                    if (! $scope.photos) {
                        return [];
                    }
                    return $scope.photos.slice(startIdx, startIdx + numPhotos);
                }
                
                $scope.removePhoto = function(id) {
                    return alertplus.confirm('Are you sure you want to delete this image?', "Delete Image", true)
                    .then(function() {
                        $scope.delphoto({id: id})
                        .then(function() {
                            $scope.zoomimage = null;
                            $scope.photos = $scope.photos.filter(function(photo) {
                                return (photo.id !== id);
                            });
                        });
                    });
                }
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.zoomimage = null;
                }
                
                $scope.cmdEnter = function() {
                    // do nothing
                    // want this here to override any parent scope cmdEnter event though.
                }
            }
        };
    }]
);

//
// Just put this here in case someone doesn't add the KeyEventHandler by attaching the javascript.
// At least it won't error out, keyboard shortcuts just won't work.
//
if (KeyEventHandler) {
    KeyEventHandler.attach(wildbook.app);

    wildbook.app.directive(
        "wbKeyHandlerNavig",
        function(keyEvents) {
            return({
                link: function(scope, element, attrs) {
                    KeyEventHandler.link(setup, scope, element, attrs);
                },
                restrict: "A"
            });

            function setup(scope, element) {
                return keyEvents.handler(100)
                .keydown(
                    function handleKeyDown(event) {
                        if (event.is.leftarrow) {
                            scope.$applyAsync(scope.panLeft);
                            return false;
                        }
                        if ( event.is.rightarrow ) {
                            scope.$applyAsync(scope.panRight);
                            return false;
                        }
                    }
                );
            }
        }
    );

    wildbook.app.directive(
        "wbKeyHandlerForm",
        function(keyEvents) {
            return({
                link: function(scope, element, attrs) {
                    KeyEventHandler.link(setup, scope, element, attrs);
                },
                restrict: "A"
            });

            function setup(scope, element) {
                // Focus the input so the user can start typing right-away.
                var element = element[0].querySelector("input[ng-model], select[ng-model]");
                if (element) {
                    element.focus();
                }

                // Create a new key-handler with priority (100) - this means that
                // this handler's methods will be invoked before the the root
                // controller. This gives the form an opportunity to intercept events
                // and stop them from propagating.
                return keyEvents.handler(100)
                // NOTE: Some key-combinations, like the ESC key are more
                // consistently reported across browsers in the keydown event.
                .keydown(
                    function handleKeyDown(event) {
                        // If the user hits the ESC key, we want to close the form.
                        if (event.is.esc) {
                            // Change the view-model inside a digest.
                            // --
                            // CAUTION: This scope method is inherited from the
                            // root controller.
                            scope.$applyAsync(scope.cancel);
                            // Kill the event entirely.
                            return false;
                        }
                        // As a convenience, we want to listen for the CMD-Enter
                        // key-combo and use that to submit the Form. This is
                        // becoming a standard on the web.
                        if ( event.is.cmdEnter ) {
                            // Since we are altering the meaning of this key-
                            // combo, we have to stop the browser from trying to
                            // execute the core behavior.
                            event.preventDefault();
                            // Change the view-model inside a digest.
                            scope.$applyAsync(scope.cmdEnter);
                        }
//                        if (event.is.leftarrow) {
//                            scope.$applyAsync(scope.panLeft);
//                            return false;
//                        }
//                        if ( event.is.rightarrow ) {
//                            scope.$applyAsync(scope.panRight);
//                            return false;
//                        }
                    }
                )
                .keypress(
                    function handleKeyPress(event) {
                        // If the event is triggered by an input-based event,
                        // we want to stop any propagation of the event. This way,
                        // no other event-handlers will have a chance to try and
                        // mess with it. This makes sense since we don't want to
                        // interrupt the user while they are typing.
                        if (event.is.input) {
                            event.stopImmediatePropagation();
                        }
                    }
                );
            }
        }
    );
}
