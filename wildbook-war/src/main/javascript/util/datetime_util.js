/* global angular */
'use strict';

angular.module('wildbook.util')
.factory("wbDateUtils", ["wbConfig", "moment", function(wbConfig, moment) {
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
}]).
directive(
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
).directive(
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
