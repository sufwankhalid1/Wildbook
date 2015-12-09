require("./util/util.js");
var KeyEventHandler = require("./util/keyevent_handler.js");
require("./admin/admin.js");
//
// This is where our jade templates are magically compiled into html and munged into a javascript file
// (called templates.js) which allows angular to look here for the templates rather than having to make
// an actual http call to get the template.
//
angular.module('templates', []);
//require("./templates.js");

var app = angular.module('appWildbook',
        ["agGrid", "ui.bootstrap", "ngMaterial", "templates", "cgBusy", "wildbook.util", "wildbook.admin", "leaflet-directive"])
       .config(['$mdThemingProvider', '$logProvider', function($mdThemingProvider, $logProvider) {
            $logProvider.debugEnabled(false);
       }]);

//
// TODO: angular-moment is not quite ready for prime time. It doesn't play well with browserify. I don't really
//       need the directives they added and I can get the injection through a constant as such. It does mean that
//       I have to package up moment with wildbook code for browserify. So the TODO is for to check back
//       at later dates to see if the situation is improved.
//
app.constant("moment", require("moment"));

app.config(function($mdIconProvider) {
    $mdIconProvider
      .defaultIconSet('icons/mdi.svg')
  });

app.factory('$exceptionHandler', function() {
    return function(ex, cause) {
        //
        // TODO: Make this configurable so that if on production the errors
        // are sent to the console instead of a dialog?
        //
        alertplus.error(ex);
      };
});

app.factory("wbConfig", ["$http", "$exceptionHandler", function($http, $exceptionHandler) {
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
    }, $exceptionHandler);

    return {
        config: getConfig,
        getVessels: function(org) {
            return getVessels(config.orgs, org);
        }
    };
}]);

app.factory("wbLangUtils", function() {
    return {
        findInArray: function(array, compare) {
            var element = null;
            array.every(function(item) {
                if (compare(item)) {
                    element = item;
                    return false;
                } else {
                    return true;
                }
            });
            return element;
        },
        findIndexInArray: function(array, compare) {
            var idx = null;
            array.every(function(item, index) {
                if (compare(item)) {
                    idx = index;
                    return false;
                } else {
                    return true;
                }
            });
            return idx;
        },
        existsInArray: function(array, compare) {
            return(this.findIndexInArray(array, compare) !== null);
        }
    }
});

app.factory("wbDateUtils", ["wbConfig", "moment", function(wbConfig, moment) {
    function restToMoment(rest) {
        if (! rest) {
            return null;
        }

        if (rest.constructor === Array) {
            //
            // The month is zero-based in moment because javascript Date is also apparently. Sheeesh.
            // Therefore we have to do the following which involves copying the array so that we
            // don't affect the data passed in.
            //
            var datearray = rest.slice();
            datearray[1]--;
            return moment(datearray);
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
        dateFromRest: function(rest){
            var moment = restToMoment(rest);
            if (moment) {
                return moment.toDate();
            }
            return null;
        },
        dateStringFromRest: function(rest) {
            return formatMoment(restToMoment(rest));
        },
        timeToDateString: function(time) {
            return moment(time).format('lll');
        },
        sameDay: function(dates){
            //array of dates
            if (dates.length){
                photoTimeline = {oldest: dates[0]};
                for(ii=0; ii < dates.length; ii++){
                    if(! moment(dates[ii]).isSame(photoTimeline.oldest, 'day')) return false;
                } 
            }

            //obj with dates
            if (typeof dates == 'object' && !dates.length){
                photoTimeline = {};
                angular.forEach(dates, function(date, key){
                    //initialize (doesnt really matter which dates, theyll probably change)
                    if (!photoTimeline.compareDates){
                        photoTimeline.compareDates = date;
                    }

                    for (ii=0; ii < dates.length; ii++){
                        if (! moment(date).isSame(photoTimeline.compareDates, 'day')) return false;
                    }
                });
            }

            return true;
        },
        compareTimes: function(dates){
            //array of dates
            var oldestTime = null;

            if (dates.length){
                oldestTime = dates[0];
                for(ii=0; ii < dates.length; ii++){
                    if(! moment(dates[ii]).isAfter(oldestTime, 'second')) oldestTime = dates[ii];
                }
                return oldestTime;
            }

            //obj with dates
            if (typeof dates == 'object' && !dates.length){
                angular.forEach(dates, function(date, key){
                    //initialize (doesnt really matter which dates, theyll probably change)
                    if (!oldestTime){
                        oldestTime = date;
                    }

                    if (! moment(date).isAfter(oldestTime, 'second')) oldestTime = dates;
                });
                return oldestTime;
            }
        },
        compareDates: function(dates){
            photoTimeline = {};
            if (typeof dates == 'array'){
                //initialize (doesnt really matter which dates, theyll probably change)
                photoTimeline.oldest = dates[0];
                photoTimeline.newest = dates[dates.length];

                for (ii=0; ii < dates.length; ii++){
                    if (moment(dates[ii]).isBefore(photoTimeline.oldest, 'second')) photoTimeline.oldest = dates[ii];
                    if (moment(dates[ii]).isAfter(photoTimeline.newest, 'second')) photoTimeline.newest = dates[ii];
                }

                return photoTimeline;
            }

            if (typeof dates == 'object'){
                angular.forEach(dates, function(date, key){
                    //initialize (doesnt really matter which dates, theyll probably change)
                    if (!photoTimeline.oldest){
                        photoTimeline.oldest = date;
                        photoTimeline.newest = date;
                    }

                    for (ii=0; ii < dates.length; ii++){
                        if (moment(date).isBefore(photoTimeline.oldest, 'second')) photoTimeline.oldest = date;
                        if (moment(date).isAfter(photoTimeline.newest, 'second')) photoTimeline.newest = date;
                    }
                });

                return photoTimeline;
            }
        },
        toFileStringFromRest: function(rest) {
            var thing = restToMoment(rest);
            if (! thing) {
                thing = moment();
            }
            
            return thing.format("YYYYMMDD");
        }
    };
}]);

app.factory("wbEncounterUtils", ["$http", "$q", "wbConfig", "wbDateUtils", "$exceptionHandler", function($http, $q, wbConfig, wbDateUtils, $exceptionHandler) {
    return {
        getMedia: function(encdata) {
            if (! encdata.photos) {
                return $http.get("obj/encounter/getmedia/" + encdata.encounter.id)
                .then(function(result) {
                    encdata.photos = result.data;
                    return encdata;
                });
            }
            return $q.resolve();
        },
        createNewEncData: function(selectedPhotos) {
            //if photos are selected add them to the new encounter
            if (selectedPhotos && selectedPhotos.length){
                var platitude = null;
                var plongitude = null;
                var dates = [];

                angular.forEach(selectedPhotos, function(photo, key){
                    if (photo.latitude){
                        platitude = photo.latitude;
                    }
                    if (photo.longitude){
                        plongitude = photo.longitude;
                    }

                    //create date array for wbDateUtils
                    if(photo.timestamp.length === 6){
                        dates.push(photo.timestamp);
                    }
                });

                //check if same day, if so, compare
                if(!wbDateUtils.sameDay(dates)){
                    return $q.reject("These photos were taken on different days!<br/> Please choose images that occured during the same encounter.");
                };

                dates = wbDateUtils.compareDates(dates);
                dates.oldestTime = wbDateUtils.compareTimes(dates);

                //format dates for encounter inputs
                dates.oldestDate = dates.oldest.slice(0, 3);
                dates.oldestTime = dates.oldest.slice(3, dates.oldest.length);
                dates.newestTime = dates.newest.slice(3, dates.newest.length);
                dates.oldestTime.push("Z");
                dates.newestTime.push("Z");

                return $q.resolve({
                    encounter: {
                                individual: {species: wbConfig.config().species[0]},
                                encDate : dates.oldestDate,
                                starttime: dates.oldestTime,
                                endtime: dates.newestTime,
                                location: {latitude:platitude, longitude:plongitude}
                                },
                    photos: selectedPhotos
                });
            } 
            else {
                return $q.resolve({
                    encounter: {individual: {species: wbConfig.config().species[0]}},
                    photos: []
                });
            }
        },
        getEncData: function(encounter) {
            return this.getMedia({encounter: encounter})
            .then(function(encdata) {
                return encdata;
            });

        },
        saveEnc: function(enc){
            return  $http.post('obj/encounter/save', enc)
            .then(function(result) {
                enc.id = result.data;
                return enc;
            });
        }
    };
}]);

app.directive('cancelButton', [function() {
    return {
        restrict: 'E',
        template: ' <a href="javascript:;" ng-click="cancel()">'+
                  '     <md-icon md-svg-icon="close-circle"><md-tooltip>Cancel</md-tooltip></md-icon>'+
                  ' </a>',
        replace: true
    }
}]);

app.directive(
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

app.directive(
    'timeInput',
    ["moment", function(moment) {
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
}]);

KeyEventHandler.attach(app);

app.directive(
    "wbKeyHandlerForm",
    ["keyEvents", function(keyEvents) {
        return({
            link: function(scope, element, attrs) {
                KeyEventHandler.link(getKeySetup(attrs.wbKeyHandlerPriority), scope, element, attrs);
            },
            restrict: "A"
        });

        function getKeySetup(priority) {
            return function(scope, element) {
                // Focus the input so the user can start typing right-away.
                var element = element[0].querySelector("input[ng-model], select[ng-model]");
                if (element) {
                    element.focus();
                }

                // Create a new key-handler with priority (100) - this means that
                // this handler's methods will be invoked before the the root
                // controller. This gives the form an opportunity to intercept events
                // and stop them from propagating.
                return keyEvents.handler(priority ? parseInt(priority) : 100)
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
//                            if (event.is.leftarrow) {
//                                scope.$applyAsync(scope.panLeft);
//                                return false;
//                            }
//                            if ( event.is.rightarrow ) {
//                                scope.$applyAsync(scope.panRight);
//                                return false;
//                            }
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
            };
        }
    }]
);
