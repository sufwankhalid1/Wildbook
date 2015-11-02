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
                return moment.format($scope.main.config.props["moment.date.format"]);// || "YYYY-MM-DD");
            }
            return moment.format($scope.main.config.props["moment.datetime.format"] || "YYYY-MM-DD hh:mm:ss");
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
                       return formatMoment(restToMoment(rest));
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

//
// Can't get this to work for sub-controllers. Plus you should be able to
// use the built-in ng-keypress directive anyway and check for escape in there.
// Problem though is that, even if you put ng-keypress on <body> element or on document,
// the main controller picks it up
//
//wildbook.app.directive('escKey', function () {
//    return function (scope, element, attrs) {
//        element.bind("keydown keypress", function (event) {
//            if ((event.which || event.keyCode) === 27) {
//                scope.$apply(function (){
//                    scope.$eval(attrs.escKey);
//                });
//
//                event.preventDefault();
//            }
//        });
//    };
//});

if (KeyEventHandler) {
    KeyEventHandler.attach(wildbook.app);

    //
    // Create "generic" key handling form directive.
    //
    wildbook.app.directive(
        "wbKeyHandlerForm",
        function( keyEvents ) {
            // Return the directive configuration object.
            return({
                link: link,
                restrict: "A"
            });
            
            function setup(scope, element) {
                // Focus the input so the user can start typing right-away.
                element[0].querySelectorAll("input[ng-model]")[0].focus();

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
            
            // I bind the JavaScript events to the local scope.
            function link(scope, element, attrs) {
                var keyHandler;
                // Handle this differently whether we are using ng-if or ng-show
                if (attrs.ngIf) {
                    keyHandler = setup(scope, element);
                    // teardown the key handler when the element is destroyed
                    scope.$on(
                        "$destroy",
                        function() {
                            keyHandler.teardown();
                        }
                    );
                } else {
                    // Setup a watch so that as the element is shown or hidden
                    // we either recreate the keyhandler or tear it down.
                    scope.$watch(attrs.ngShow, function(val) {
                        //
                        // If the variable we are watching is undefined then this is
                        // a false call. Not sure what triggers that.
                        if (val === undefined) {
                            return;
                        }
                        if (val) {
                            keyHandler = setup(scope, element);
                        } else {
                            // Since we are listening for key events on a service (ie, not on
                            // the current Element), we have to be sure to teardown the bindings
                            // so that we don't get rogue event handlers persisting in the
                            // application.
                            if (keyHandler) {
                                keyHandler.teardown();
                                keyHandler == null;
                            }
                        }
                    });
                }
            }
        }
    );
}
