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

    function checkTimeInput(data) {
        var nn;
        //need to make sure we send either a time or nothing
        if (data.endtime) {
            var endlength = data.endtime.length;
            for (nn = 0; nn < endlength; nn++) {
                if (data.endtime && data.endtime[nn] === null ) {
                    data.endtime = null;
                }
            }
        }

        if (data.starttime) {
            var startlength = data.starttime.length;
            for (nn = 0; nn < startlength; nn++) {
                if (data.starttime && data.starttime[nn] === null ) {
                    data.starttime = null;
                }
            }
        }

        return data;
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

    return {
        verifyTimeInput: function(data) {
            return checkTimeInput(data);
        },
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
);
