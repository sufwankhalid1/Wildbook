/* global angular, alertplus */
'use strict';

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
        ["agGrid", "ui.bootstrap", "ngMaterial", "templates", "cgBusy", "wildbook.util",
         "wildbook.admin", "leaflet-directive", "ngFileSaver"])
       .config(['$mdThemingProvider', '$logProvider','moment', '$mdDateLocaleProvider', function($mdThemingProvider, $logProvider, moment, $mdDateLocaleProvider) {
            $mdDateLocaleProvider.formatDate = function (date) {
                if (!date) {
                    return null;
                }
                return moment(date).format('YYYY-MM-DD');
            };
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
      .defaultIconSet('icons/mdi.svg');
  });

app.factory('$exceptionHandler', function() {
    return function(ex, cause) {
        //
        // TODO: Make this configurable so that if on production the errors
        // are sent to the console instead of a dialog?
        //
        if (ex.data) {
            alertplus.error(ex.data);
        } else {
            alertplus.error(ex);
        }
      };
});

app.factory("wbConfig", ["$http", "$exceptionHandler", "$q", function($http, $exceptionHandler, $q) {
    var config;

    function getVessels(orgs, org) {
        //
        // Let's find our master organization so that we can add the vessels to
        // it and thus cache the results for future occerences of the user picking
        // this organization again in the list.
        //
        var orgfilter = orgs.filter(function(value) {
            return (value.orgId === org.orgId);
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
        if (config) {
            return config;
        }

        config = $http({url:"util/init"})
            .then(function(result) {
                return result.data;
            }, $exceptionHandler);

        return config;
    }

    //running this to kick it off incase it was never initialized when we need it
    getConfig();

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
    };
});

app.factory("wbDateUtils", ["wbConfig", "moment", function(wbConfig, moment) {
    var dateFormat;
    var datetimeFormat;

    wbConfig.config().then(function(config) {
        dateFormat = config.props["moment.date.format"];
        datetimeFormat = config.props["moment.datetime.format"];
    });

    function restToMoment(rest) {
        if (! rest) {
            return null;
        }

        if (rest.constructor === Array) {
            return moment(fixMonth(rest));
        }
        return null;
    }

    function formatMoment(aMoment) {
        if (aMoment) {
            if (aMoment.hour() === 0 && aMoment.minute() === 0 && aMoment.second() === 0) {
                return aMoment.format(dateFormat || "YYYY-MM-DD");
            }
            return aMoment.format(datetimeFormat || "YYYY-MM-DD HH:mm:ss");
        }
        return null;
    }

    function fixMonth(rest) {
        //
        // The month is zero-based in moment because javascript Date is also apparently. Sheeesh.
        // Therefore we have to do the following which involves copying the array so that we
        // don't affect the data passed in.
        //
        var datearray = rest.slice();
        datearray[1]--;
        return datearray;
    }

    return {
        formatTimeArrayToString: function(time) {
            var array = angular.copy(time);

            if (!array || typeof array !== 'object' || !array.length) {
                return;
            }

            array.forEach(function(val, index) {
                if (/[A-Z]|[a-z]/.test(val)) {
                    array.splice(index,1);
                } else {
                    if (parseInt(val) < 10 && val.toString().length === 1) {
                        array[index] = "0"+val;
                    }
                }
            });

            if (array.length === 1) {
                array.push("00");
                array.push("00");
            } else if (array.length === 2) {
                array.push("00");
            }

            return array.join(':');
        },
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
        sameDay: function(dates) {
            if (!dates) {
                return false;
            }

            if (dates.length <= 1) {
                return true;
            }

            var first = dates[0];
            for (var ii=1; ii < dates.length; ii++) {
                //
                // This is not working for some damn reason all the time.
                // I had 01/30/2015 == 02/02/2015 using this method!
                //
//                    if (!moment(dates[ii]).isSame(first, 'day')) {
//                        return false;
//                    }
                var current = dates[ii];
                if (current[0] !== first[0] || current[1] !== first[1] || current[2] !== first[2]) {
                    return false;
                }
            }
            return true;
        },
        compareDates: function(dates) {
            if (!dates || !dates.length) {
                return null;
            }

            //initialize (doesnt really matter which dates, theyll probably change)
            var newest = dates[0];
            var oldest = dates[dates.length-1];
            for (var ii = 0; ii < dates.length; ii++) {
                var fixedDate = moment(dates[ii]);
                if (fixedDate.isBefore(oldest, 'second')) {
                    oldest = dates[ii];
                }
                if (fixedDate.isAfter(newest, 'second')) {
                    newest = dates[ii];
                }
            }

            return {newest: newest, oldest: oldest};
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
    var config;
    wbConfig.config()
    .then(function(theConfig) {
        config = theConfig;
    });
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
        createNewEncData: function(selectedPhotos, submission) {
            //if photos are selected add them to the new encounter
            var encounter = {individual: {species: config.defaultSpecies || config.species[0] }};

            if (!selectedPhotos || !selectedPhotos.length) {
                return $q.resolve({
                    encounter: encounter,
                    photos: [],
                    autofilledFrom: null
                });
            }

            var platitude = null;
            var plongitude = null;
            var dates = [];

            var photo_encounters;
            var photo_encounter;

            var encounter_used;

            function encounterExist(data) {
                //
                //taking first photo's  first encounter and setting up encounter based on that info
                //
                photo_encounters = data;
                photo_encounters.forEach(function(resencounter){
                    if (resencounter) {
                        photo_encounter = resencounter;
                    }
                });

                if ((photo_encounter.location.latitude && photo_encounter.location.longitude)
                    || photo_encounter.location.verbatimLocation || photo_encounter.location.locationid) {
                    encounter.location = photo_encounter.location;
                }

                if (photo_encounter.starttime) {
                    encounter.starttime = photo_encounter.starttime;
                }

                if (photo_encounter.endtime) {
                    encounter.endtime = photo_encounter.endtime;
                }

                if (photo_encounter.encDate) {
                    encounter.encDate = photo_encounter.encDate;
                } else {
                    encounter.encDate = createDate();
                }

                if (photo_encounter.individual.avatarFull) {
                    encounter.individual.avatarFull = photo_encounter.individual.avatarFull;
                    encounter.individual.avatar = photo_encounter.individual.thumbUrl;
                }

                if (encounter.encDate) {
                    encounter_used = "Autofilled from encounter: " + photo_encounter.individual.displayName + ", " + wbDateUtils.dateStringFromRest(encounter.encDate);
                } else {
                    encounter_used = "Autofilled from encounter: " + photo_encounter.individual.displayName;
                }

            }

            function newEncounter() {
                selectedPhotos.forEach(function(photo){
                    if (photo.latitude) {
                        platitude = photo.latitude;
                    }
                    if (photo.longitude) {
                        plongitude = photo.longitude;
                    }

                    //create date array for wbDateUtils
                    if (photo.timestamp) {
                        dates.push(photo.timestamp);
                    }

                    if (!encounter.individual.avatarFull) {
                        encounter.individual.avatarFull = photo;
                        encounter.individual.avatar = photo.thumbUrl;
                    }
                });

                if (platitude && plongitude) {
                    encounter.location = {latitude: platitude, longitude: plongitude};
                } else if (submission) {
                    encounter.location = {latitude: submission.latitude, longitude: submission.longitude};
                } else {
                    encounter.location = {latitude: null, longitude: null};
                }

                compareDates();

                setDate();
            }

            function createDate(selectedPhotos) {
                var found = false;
                var timeline;
                selectedPhotos.forEach(function(photo) {
                    if (photo.timestamp && !found) {
                        timeline = wbDateUtils.compareDates(dates);

                        if (timeline) {
                            found = true;
                        }
                    }
                });

                if (found) {
                    return timeline.newest.slice(0, 3);
                }
            }

            function setDate() {
                var timeline = wbDateUtils.compareDates(dates);

                if (timeline) {
                    encounter.encDate = timeline.newest.slice(0, 3);
                    encounter.starttime = timeline.oldest.slice(3, timeline.oldest.length);
                    encounter.starttime.push("Z");
                    encounter.endtime = timeline.newest.slice(3, timeline.newest.length);
                    encounter.endtime.push("Z");
                }
            }

            function compareDates() {
                //check if same day, if so, compare
                if (!wbDateUtils.sameDay(dates)) {
                    return $q.reject("These photos were taken on different days!<br/> Please choose images that occured during the same encounter.");
                }
            }


            return $http.post('obj/encounter/checkDuplicateImage', selectedPhotos)
                .then(function(res){
                    if (res.data && res.data.length && res.data[0] !== null) {
                        encounterExist(res.data);
                    } else {
                        newEncounter();
                    }

                    return $q.resolve({
                        encounter: encounter,
                        photos: selectedPhotos,
                        autofilledFrom: encounter_used
                    });
            });
        },
        getEncData: function(encounter) {
            return this.getMedia({encounter: encounter})
            .then(function(encdata) {
                return encdata;
            });

        },
        saveEnc: function(enc) {
            return  $http.post('obj/encounter/save', enc)
            .then(function(result) {
                enc.id = result.data.encounterid;
                enc.individual.id = result.data.individualid;
                return enc;
            });
        },
        delEnc: function(enc, alreadyAlerted) {
            if (alreadyAlerted) {
                return $http.post("obj/encounter/delete", enc)
                .then(function() {
                }, $exceptionHandler);
            } else {
                return alertplus.confirm('Are you sure you want to delete this encounter?', "Delete Encounter", true)
                .then(function() {
                    $http.post("obj/encounter/delete", enc)
                    .then(function() {
                    }, $exceptionHandler);
                });
            }
        }
    };
}]);

app.directive('cancelButton', [function() {
    return {
        restrict: 'E',
        scope: {
                hideme: '=?',
                cancel: '&'
                },
        template: '<md-icon md-svg-icon="close-circle" ng-show="!hideme" ng-click="cancel()"><md-tooltip>Cancel</md-tooltip></md-icon>'
    };
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
    ["moment", "$timeout", function(moment, $timeout) {
        return {
            scope: {
                time:'=',
                placeholder:'@',
                format: '@'
            },
            template: '<div class="customTime">'
                            +'  <div class="{{hourFocused || minuteFocused ||  secondFocused ? \'blue-border\'  : \'gray-border\'}} height-50 overflow-hidden" layout="row" layout-align="start center">'
                            +'      <span flex="25" class="timePlaceholder">{{placeholder}}: </span>'
                            +'      <div flex="75" layout="row" layout-align="start center">'
                            +'          <div timeedit>'
                            +'              <input ng-class="{\'blue-background\': hourFocused}" ng-focus="hourFocused = true; selectTime($event);" ng-blur="hourFocused = false;  padZero(\'hour\');" '
                            +'              class="customTimeInput" ng-keyup="changeHour()" ng-model="hour" ng-show="showHour" />'
                            +'              <div ng-focus="hourFocused = true; focus(\'hour\', $event)" ng-blur="hourFocused = false;" ng-click="showHour && !hour ? showHour = true : showHour = false;"'
                            +'              ng-show="hour == null">--</div>'
                            +'          </div>'
                            +'          <div>:</div>'
                            +'          <div timeedit>'
                            +'              <input ng-class="{\'blue-background\': minuteFocused}" ng-focus="minuteFocused = true; selectTime($event);" ng-blur="minuteFocused = false;  padZero(\'minute\');" '
                            +'              class="customTimeInput" ng-keyup="changeMinute()" ng-model="minute" ng-show="showMinute" />'
                            +'              <div ng-focus="minuteFocused = true; focus(\'minute\', $event)" ng-blur="minuteFocused = false;" ng-click="showMinute && !hour ? showMinute = true : showMinute = false;"'
                            +'              ng-show="minute == null">--</div>'
                            +'          </div>'
                            +'          <div>:</div>'
                            +'          <div timeedit>'
                            +'              <input ng-class="{\'blue-background\': secondFocused}" ng-focus="secondFocused = true; selectTime($event);" ng-blur="secondFocused = false; padZero(\'second\');" '
                            +'              class="customTimeInput" ng-model="second" ng-keyup="changeSecond()" ng-show="showSecond" />'
                            +'              <div ng-focus="secondFocused = true; focus(\'second\', $event)" ng-blur="secondFocused = false;" ng-click="showSecond && !hour ? showSecond = true : showSecond = false;"'
                            +'              ng-show="second == null">--</div>'
                            +'          </div>'
                            +'          <md-select ng-model="ampm" ng-if="format==\'12\'" ng-change="changeAmPm()" placeholder="am/pm"class="border-bottom-0 ml-10">'
                            +'              <md-option ng-value="\'am\'">AM</md-option>'
                            +'              <md-option ng-value="\'pm\'">PM</md-option>'
                            +'          </md-select>'
                            +'          <span ng-if="format==\'24\' || !format">24hr</span>'
                            +'      </div>'
                            +'    </div>'
                            +'</div>',
            replace: false,
            link: function($scope, elm, attrs) {
                    $scope.hour = null;
                    $scope.minute = null;
                    $scope.second = null;
                    $scope.showSecond = null;
                    $scope.showMinute = null;
                    $scope.showHour = null;
                    var hourFilter = /[1-9]/;

                    $scope.focus = function(type, $e) {
                        if (!$scope.time) {
                            $scope.time = [null, null, null];
                        }

                        switch(type) {
                            case 'hour':
                            	$scope.hour = $scope.hour || "00";
                            	$scope.showHour = true;
                                break;
                            case 'minute':
                            	$scope.minute = $scope.minute || "00";
                            	$scope.showMinute = true;
                                break;
                            case 'second':
                            	$scope.second = $scope.second || "00";
                            	$scope.showSecond = true;
                                break;
                        }

                        $scope.time[3] = "Z";

                        $timeout(function() {
                            $e.target.previousElementSibling.focus(function() {
                                $scope.selectTime($e);
                            });
                        }, 100);
                    };

                    $scope.selectTime = function($e) {
                        $e.target.select();
                    };

                    $scope.changeHour = function() {
                        if (!$scope.hour.length) {
                            $scope.hour = '';
                        } else if ($scope.hour.length > 2
                                   || (!hourFilter.test($scope.hour) && $scope.hour !== "0")
                                   || ($scope.format === "12" ? parseInt($scope.hour) > 12 : parseInt($scope.hour) > 24) ) {
                            $scope.hour = "00";
                        } else {
                        	$scope.time[0] = parseInt($scope.hour);
                        }

                        if ($scope.format === "12") {
                            $scope.changeAmPm();
                        }
                    };

                    $scope.changeMinute = function() {
                        if ($scope.minute.length > 2 || !$scope.minute.length || ! /^[0-5]\d*/.test($scope.minute) || parseInt($scope.minute) > 59) {
                            $scope.minute = "00";
                        } else {
                            $scope.time[1] = parseInt($scope.minute);
                        }
                    };

                    $scope.changeSecond = function() {
                        if ($scope.second.length > 2 || !$scope.second.length || ! /^[0-5]\d*/.test($scope.second) || parseInt($scope.second) > 59) {
                            $scope.second = "00";
                        } else {
                            $scope.time[2] = parseInt($scope.second);
                        }
                    };

                    $scope.changeAmPm = function() {
                        if ($scope.ampm === "am") {
                            if ($scope.hour) {
                                if (parseInt($scope.hour) === 12) {
                                    $scope.time[0] = 0;
                                }
                            }
                        } else {
                            if ($scope.hour) {
                                if (parseInt($scope.hour) === 0) {
                                    $scope.time[0] = 12;
                                } else if (parseInt($scope.hour) !== 12 ) {
                                    $scope.time[0] = parseInt($scope.hour) + 12;
                                }
                            }
                        }
                    };

                    $scope.padZero = function(type) {
                        switch(type) {
                            case 'hour' :
                            	if ($scope.hour && $scope.hour.length === 1) {
                            		$scope.hour = "0" + $scope.hour;
                            		$scope.time[0] = parseInt($scope.hour);
                            	}
                                break;
                            case 'minute':
                            	if ((typeof $scope.minute === 'string' && $scope.minute && $scope.minute.length === 1)
                                    || (typeof $scope.minute === 'number' && $scope.minute < 10)) {
                                		$scope.minute = "0" + $scope.minute;
                                		$scope.time[1] = parseInt($scope.minute);
                            	}
                                break;
                            case 'second' :
                            	if ((typeof $scope.second === 'string' && $scope.second && $scope.second.length === 1)
                                    || (typeof $scope.second === 'number' && $scope.second < 10)) {
                                		$scope.second = "0" + $scope.second;
                                		$scope.time[2] = parseInt($scope.second);
                            	}
                                break;
                        }
                    };

                    $scope.$watch('time', function(newVal, oldVal) {
                        init();
                    });

                    //time init
                    var init = function() {
                        if (!$scope.time) {
                            $scope.time = [];
                            return false;
                        } else if ($scope.time && $scope.time.length && (!$scope.format || $scope.format === "24")){
                                $scope.hour = $scope.time[0].toString();
                        } else if ($scope.time && $scope.time.length && $scope.format === "12") {
                            if ($scope.time[0]  < 12 && $scope.time[0] !== 0) {
                                $scope.hour = $scope.time[0].toString();
                                $scope.ampm = "am";
                            } else if ($scope.time[0] === 0) {
                                $scope.hour = "12";
                                $scope.ampm = "am";
                            } else if ($scope.time[0] === 12) {
                                $scope.hour = $scope.time[0].toString();
                                $scope.ampm = "pm";
                            } else if ($scope.time[0] > 12) {
                                $scope.hour = $scope.time[0] - 12;
                                $scope.hour = $scope.hour.toString();
                                $scope.ampm = "pm";
                            }
                        }

                        if ($scope.time.length) {
                            if ($scope.time[2] === "Z") {
                                $scope.time[2] = 0;
                                $scope.time[3] = "Z";
                            } else if ($scope.time[1] === "Z") {
                                $scope.time[1] = 0;
                                $scope.time[2] = 0;
                                $scope.time[3] = "Z";
                            }

                            $scope.minute = $scope.time[1].toString();
                            $scope.second = $scope.time[2].toString();

                            $scope.padZero('hour');
                            $scope.padZero('minute');
                            $scope.padZero('second');

                            $scope.showHour = true;
                            $scope.showMinute = true;
                            $scope.showSecond = true;
                    }
                };
            }
        };
}]);

KeyEventHandler.attach(app);

app.directive(
    "wbKeyHandlerForm",
    ["keyEvents", function(keyEvents) {
        function getKeySetup(priority) {
            return function(scope, element) {
                // Focus the input so the user can start typing right-away.
                var elem = element[0].querySelector("input[ng-model], select[ng-model]");
                if (elem) {
                    elem.focus();
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

        return({
            link: function(scope, element, attrs) {
                KeyEventHandler.link(getKeySetup(attrs.wbKeyHandlerPriority), scope, element, attrs);
            },
            restrict: "A"
        });
    }]
);
