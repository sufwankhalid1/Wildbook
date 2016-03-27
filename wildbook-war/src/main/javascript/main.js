/* global angular, alertplus */
'use strict';

var KeyEventHandler = require("./util/keyevent_handler.js");
require("./util/util.js");
require("./admin/admin.js");
require("./survey/survey_util.js");
require("./encounters/encounter_util.js");
require("./config.js");
// This is where our jade templates are magically compiled into html and munged into a javascript file
// (called templates.js) which allows angular to look here for the templates rather than having to make
// an actual http call to get the template.
//
angular.module('templates', []);
//require("./templates.js");

var app = angular.module('appWildbook',
        ["agGrid", "ui.bootstrap", "ngMaterial", "templates", "cgBusy",
         "wildbook.util", "wildbook.config", "wildbook.encounters", "wildbook.survey",
         "wildbook.admin", "leaflet-directive", "ngFileSaver"])
       .config(['$mdThemingProvider', '$logProvider','moment',
                '$mdDateLocaleProvider', function($mdThemingProvider, $logProvider, moment, $mdDateLocaleProvider) {
            $mdDateLocaleProvider.formatDate = function (date) {
                if (!date) {
                    return null;
                }
                return moment(date).format('YYYY-MM-DD');
            };

            //change default color for primary
            var indigo = $mdThemingProvider.extendPalette('indigo', {
                '500': '569fd4'
            });
            $mdThemingProvider.definePalette('indigo', indigo);

            //here you change placeholder/foreground color.
            $mdThemingProvider.theme('default').foregroundPalette[3] = "rgba(0,0,0,0.40)";



            //disable leaflet log
            $logProvider.debugEnabled(false);
       }]);

//
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
        if (!ex) {
            return;
        }
        if (ex.data) {
            alertplus.error(ex.data);
        } else {
            alertplus.error(ex);
        }
      };
});

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
