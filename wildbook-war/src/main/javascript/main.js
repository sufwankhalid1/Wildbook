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

app.factory("wbConfig", ["$http", "$exceptionHandler", "$q", function($http, $exceptionHandler, $q) {
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
    }
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

    function formatMoment(moment) {
        if (moment) {
            if (moment.hour() === 0 && moment.minute() === 0 && moment.second() === 0) {
                return moment.format(dateFormat || "YYYY-MM-DD");
            }
            return moment.format(datetimeFormat || "YYYY-MM-DD hh:mm:ss");
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
            for (ii=0; ii < dates.length; ii++) {
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
        createNewEncData: function(selectedPhotos) {
            //if photos are selected add them to the new encounter
            var encounter = {individual: {species: config.defaultSpecies || config.species[0] }};

            if (!selectedPhotos || !selectedPhotos.length) {
                return $q.resolve({
                    encounter: encounter,
                    photos: []
                });
            }
            
            var platitude = null;
            var plongitude = null;
            var dates = [];

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
            });

            //check if same day, if so, compare
            if(!wbDateUtils.sameDay(dates)){
                return $q.reject("These photos were taken on different days!<br/> Please choose images that occured during the same encounter.");
            };

            var timeline = wbDateUtils.compareDates(dates);

            if (timeline) {
                encounter.encDate = timeline.newest.slice(0, 3);
                encounter.starttime = timeline.oldest.slice(3, timeline.oldest.length);
                encounter.starttime.push("Z");
                encounter.endtime = timeline.newest.slice(3, timeline.newest.length);
                encounter.endtime.push("Z");
            }
            
            if (platitude && plongitude) {
                encounter.location = {latitude:platitude, longitude:plongitude};
            } else {
                encounter.location = {latitude:null, longitude:null};
            }

            return $q.resolve({
                encounter: encounter,
                photos: selectedPhotos
            });
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
        scope: {
                hide: '@'
                },
        template: ' <a href="javascript:;" ng-show="hide" ng-click="cancel()">'+
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
    ["moment", "$timeout", function(moment, $timeout) {
        return {
            scope: {
                time:'=',
                placeholder:'@'
            },
            template: '<div class="customTime">'
                            +'  <div class="{{hourFocused || minuteFocused ||  secondFocused ? \'blue-border\'  : \'gray-border\'}} height-50 overflow-hidden" layout="row" layout-align="start center">'
                            +'      <span class="timePlaceholder">{{placeholder}}: </span>'
                            +'      <div timeedit>'
                            +'          <input ng-class="{\'blue-background\': hourFocused}" ng-focus="hourFocused = true; selectTime($event);" ng-blur="hourFocused = false;  padZero(\'hour\');" '
                            +'          class="customTimeInput" ng-model="hour" ng-show="showHour" />'
                            +'          <div ng-focus="hourFocused = true; focus(\'hour\', $event)" ng-blur="hourFocused = false;" ng-click="showHour && !hour ? showHour = true : showHour = false;"'
                            +'          ng-show="hour == null">--</div>'
                            +'      </div>'
                            +'      <div>:</div>'
                            +'      <div timeedit>'
                            +'          <input ng-class="{\'blue-background\': minuteFocused}" ng-focus="minuteFocused = true; selectTime($event);" ng-blur="minuteFocused = false;  padZero(\'minute\');" '
                            +'          class="customTimeInput" ng-model="minute" ng-show="showMinute" />'
                            +'          <div ng-focus="minuteFocused = true; focus(\'minute\', $event)" ng-blur="minuteFocused = false;" ng-click="showMinute && !hour ? showMinute = true : showMinute = false;"'
                            +'          ng-show="minute == null">--</div>'
                            +'      </div>'
                            +'      <div>:</div>'
                            +'      <div timeedit>'
                            +'          <input ng-class="{\'blue-background\': secondFocused}" ng-focus="secondFocused = true; selectTime($event);" ng-blur="secondFocused = false; padZero(\'second\');" '
                            +'          class="customTimeInput" ng-model="second" ng-show="showSecond" />'
                            +'          <div ng-focus="secondFocused = true; focus(\'second\', $event)" ng-blur="secondFocused = false;" ng-click="showSecond && !hour ? showSecond = true : showSecond = false;"'
                            +'          ng-show="second == null">--</div>'
                            +'      </div>'
                            +'      <md-select ng-model="ampm" ng-change="changeAmPm()" placeholder="am/pm"class="border-bottom-0 ml-10">'
                            +'          <md-option ng-value="\'am\'">AM</md-option>'
                            +'          <md-option ng-value="\'pm\'">PM</md-option>'
                            +'      </md-select>'                            
                            +'  </div>'
                            +'</div>',
            replace: false,
            link: function($scope, elm, attrs) {

                    $scope.hashes = "--";
                    $scope.hour = null;
                    $scope.minute = null;
                    $scope.second = null;
                    $scope.showSecond = null;
                    $scope.showMinute = null;
                    $scope.showHour = null;

                    $scope.focus = function(type, $e) {
                        if (!$scope.time) {
                            $scope.time = [null, null, null];
                        }

                        switch(type) {
                            case 'hour': $scope.hour = $scope.hour || "00";
                                                $scope.showHour = true;
                                break;
                            case 'minute': $scope.minute = $scope.minute || "00";
                                                $scope.showMinute = true;
                                break;
                            case 'second': $scope.second = $scope.second || "00";
                                                $scope.showSecond = true;
                                break;
                        }

                        $scope.time[3] = "Z";

                        $timeout(function() {
                            $e.target.previousElementSibling.focus(function() { 
                                $scope.selectTime($e, $(this));
                            });
                        },100);
                    }

                    $scope.selectTime = function($e) {
                        $e.target.select(); 
                    }
/*
                    var $nn = 0;
                    $scope.progress = function($e) {
                        if ($nn == 1) {
                            $timeout(function() {
                                $($e).parent().nextAll().children("input").eq(0).focus(function() { 
                                    $scope.selectTime($e, $(this));
                                });
                            },100);

                            $nn = 0;
                        }

                        $nn++;
                    }
*/
                    $scope.$watch('hour', function(newVal, oldVal) {
                        if (newVal == oldVal) {
                            return false;
                        }

                        if (newVal.length > 2) {
                            $scope.hour = oldVal;
                        } else if (!newVal.length) {
                            $scope.hour = '';
                        } else if (! /^(0|1)[1-9]*/.test(newVal)){
                            $scope.hour = oldVal;
                        } else if (parseInt(newVal) > 12) {
                            $scope.hour = oldVal;
                        } else {
                            $scope.time[0] = parseInt(newVal);
                        }

                        $scope.changeAmPm();
                    });

                    $scope.$watch('minute', function(newVal, oldVal) {
                        if (newVal == oldVal) {
                            return false;
                        }
                        
                        if (newVal.length > 2) {
                            $scope.minute = oldVal;
                        } else if  (newVal.length == 1 && newVal != 0) {

                        } else if (!newVal.length) {
                            $scope.minute = '';
                        } else if (! /^[0-5]\d*/.test(newVal)){
                            $scope.minute = oldVal;
                        } else if (parseInt(newVal) > 59) {
                            $scope.minute = oldVal;
                        }  else {
                            $scope.time[1] = parseInt(newVal);
                        }
                    });

                    $scope.$watch('second', function(newVal, oldVal) {
                        if (newVal == oldVal) {
                            return false;
                        }
                        
                        if (newVal.length > 2) {
                            $scope.second = oldVal;
                        } else if  (newVal.length == 1 && newVal != 0) {

                        } else if (!newVal.length) {
                            $scope.second = '';
                        } else if (! /^[0-5]\d*/.test(newVal)){
                            $scope.second = oldVal;
                        } else if (parseInt(newVal) > 59) {
                            $scope.second = oldVal;
                        } else {
                            $scope.time[2] = parseInt(newVal);
                        }
                    });

                    $scope.changeAmPm = function() {
                        if ($scope.ampm == "am") {
                            if ($scope.hour) {
                                if (parseInt($scope.hour) == 12) {
                                    $scope.time[0] = 0;
                                }
                            }
                        } else {
                            if ($scope.hour) {
                                if (parseInt($scope.hour) == 0) {
                                    $scope.time[0] = 12;
                                } else if (parseInt($scope.hour) != 12 ){
                                    $scope.time[0] = parseInt($scope.hour)+12;
                                }
                            }
                        }
                    }

                    $scope.padZero = function(type) {
                        switch(type) {
                            case 'hour' : if ($scope.hour && $scope.hour.length == 1) {
                               $scope.hour = "0"+$scope.hour; 
                               $scope.time[0] = parseInt($scope.hour);
                            }
                                break;
                            case 'minute': if ($scope.minute && $scope.minute.length == 1) {
                               $scope.minute = "0"+$scope.minute; 
                               $scope.time[1] = parseInt($scope.minute);
                            }
                                break;
                            case 'second' : if ($scope.second && $scope.second.length == 1) {
                               $scope.second = "0"+$scope.second; 
                               $scope.time[2] = parseInt($scope.second);
                            }
                                break;
                        }
                    }

                    $scope.$watch('time', function(newVal, oldVal) {
                        init();
                    });

                    var init = function() {
                        if (!$scope.time) {
                            $scope.time = [];
                            return;
                        } else if ($scope.time && $scope.time.length) {
                            if ($scope.time[0]  < 12 && $scope.time[0] != 0) {
                                $scope.hour = $scope.time[0].toString();
                                $scope.ampm = "am";
                            } else if ($scope.time[0] == 0) {
                                $scope.hour = 12;
                                $scope.ampm = "am";
                            } else if ($scope.time[0] == 12) {
                                $scope.hour = $scope.time[0].toString();
                                $scope.ampm = "pm";
                            } else if ($scope.time[0] > 12) {
                                $scope.hour = $scope.time[0] - 12;
                                $scope.hour = $scope.hour.toString();
                                $scope.ampm = "pm";
                            }

                            if ($scope.time.length == 3) {
                                $scope.time[2] = 0;
                                $scope.time[3] = "Z"; 
                            } else if ($scope.time.length == 2) {
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
                }

                init();

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
