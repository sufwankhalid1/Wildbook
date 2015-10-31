wildbook.app.controller("EncounterEditController", function($scope, $http, $exceptionHandler) {
    var panelName = "encounter_edit";
    $scope.panelList.push(panelName);

    $scope.$on(panelName, function(event, data) {
        if (typeof data === "boolean") {
            $scope.panels[panelName] = false;
            return;
        }

        $scope.panels[panelName] = true;
        if (data) {
            $scope.encounter = data;
        } else {
            $scope.encounter = {individual: {species: $scope.main.config.species[0]}};
        }
    });
    
    function closePanel() {
        $scope.panels[panelName] = false;
        $scope.$emit(panelName + "_done", $scope.encounter);
    }
    
    $scope.cancel = function() {
        $scope.encounter = null;
        closePanel();
    }

    $scope.save = function() {
        $http.post('obj/encounter/save', $scope.encounter)
        .then(function(result) {
            $scope.encounter.id = result.data;
            closePanel();
        }, $exceptionHandler);
    };
});

wildbook.app.directive(
    "wbKeyHandlerForm",
    function( keyEvents ) {
        // Return the directive configuration object.
        return({
            link: link,
            restrict: "A"
        });
        // I bind the JavaScript events to the local scope.
        function link( scope, element, attributes ) {
            // Focus the input so the user can start typing right-away.
            element[0].querySelectorAll("input[ ng-model ]")[0].focus();

            // Create a new key-handler with priority (100) - this means that
            // this handler's methods will be invoked before the the root
            // controller. This gives the form an opportunity to intercept events
            // and stop them from propagating.
            var keyHandler = keyEvents.handler( 100 )
                // NOTE: Some key-combinations, like the ESC key are more
                // consistently reported across browsers in the keydown event.
                .keydown(
                    function handleKeyDown( event ) {
                        // If the user hits the ESC key, we want to close the form.
                        if ( event.is.esc ) {
                            // Change the view-model inside a digest.
                            // --
                            // CAUTION: This scope method is inherited from the
                            // root controller.
                            scope.$applyAsync(scope.cancel);
                            // Kill the event entirely.
                            return( false );
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
                            scope.$applyAsync(scope.save);
                        }
                    }
                )
                .keypress(
                    function handleKeyPress( event ) {
                        // If the event is triggered by an input-based event,
                        // we want to stop any propagation of the event. This way,
                        // no other event-handlers will have a chance to try and
                        // mess with it. This makes sense since we don't want to
                        // interrupt the user while they are typing.
                        if ( event.is.input ) {
                            event.stopImmediatePropagation();
                        }
                    }
                )
            ;
            // Since we are listening for key events on a service (ie, not on
            // the current Element), we have to be sure to teardown the bindings
            // so that we don't get rogue event handlers persisting in the
            // application.
            scope.$on(
                "$destroy",
                function() {
                    keyHandler.teardown();
                }
            );
        }
    }
);
