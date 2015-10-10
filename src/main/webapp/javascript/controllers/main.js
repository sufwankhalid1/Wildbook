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

wildbook.app.controller("MainController", function($scope, $http, $q, $exceptionHandler) {
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

    function restToMoment(rest) {
        if (! rest) {
            return null;
        }

        if (rest.constructor === Array) {
            //
            // For some reason the month is zero-based and nothing else is. Sigh.
            //
            return moment({year: rest[0], month: rest[1] - 1, date: rest[2]});
        }
        return null;
    }

    function formatMoment(moment) {
        if (momment) {
            return moment.format(this.main.config.props["moment.date.format"]);// || "YYYY-MM-DD");
        }
        return null;
    }

    $scope.main = {config: null,
                   getVessels: function(org) {
                       return getVessels(this.config.orgs, org);
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
                   dateFromRest: function(rest) {
                       var moment = restToMoment(rest);
                       if (moment) {
                           return moment.toDate();
                       }
                       return null;
                   },
                   dateStringFromRest: function(rest) {
                       formatMoment(restToMoment(rest));
                   }};

    //
    // Initialize app by returning the promise used to kick us off.
    //
    return $http({url:"util/init"})
    .then(function(result) {
        $scope.main.config = result.data;
        //
        // When everything is ready, initialize tooltips on the page
        //
        $('[data-toggle="tooltip"]').tooltip();

    }, $exceptionHandler);
});

wildbook.app.directive(
        'dateInput',
        function() {
            return {
                require: 'ngModel',
                template: '<input type="date"></input>',
                replace: true,
                link: function(scope, elm, attrs, ngModelCtrl) {
                    ngModelCtrl.$formatters.push(function (modelValue) {
                        if (!scope.main) {
                            //
                            // This happens if this control is used in another directive
                            // template when the directive is loaded (but not yet used).
                            //
                            return null;
                        }
                        return scope.main.dateFromRest(modelValue);
                    });

                    ngModelCtrl.$parsers.push(function(viewValue) {
                        return scope.main.dateToRest(viewValue);
                    });
                },
            };
    });

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
                            return moment({hour: modelValue[0],
                                           minute: modelValue[1],
                                           second: modelValue[2]}).format("hh:mm:ss a");
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
                },
            };
    });
