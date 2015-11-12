wildbook.app.directive(
    'wbEncounterEdit',
    ["$http", "$exceptionHandler", "wbConfig", "wbEncounterUtils",
     function($http, $exceptionHandler, wbConfig, wbEncounterUtils) {
        return {
            restrict: 'E',
            scope: {
                encounter: "=encounterToEdit",
                editEncounterDone: "&"
            },
            templateUrl: 'util/render?j=partials/encounter_edit',
            replace: true,
            controller: function($scope) {
                $scope.tbActions = [{
                    code: "del",
                    shortcutKeyCode: 68,
                    type: "danger",
                    glyphicon: "remove",
                    tooltip: "Remove/Detach",
                    confirm: { message: "Are you sure you want to detach selected images from this encounter?"}
                }];
                
                if (! $scope.encounter.photos) {
                    wbEncounterUtils.getMedia($scope.encounter);
                }

                $scope.getSpecies = function() {
                    return wbConfig.config().species;
                }
                
                $scope.save = function() {
                    $http.post('obj/encounter/save', $scope.encounter)
                    .then(function(result) {
                        $scope.encounter.id = result.data;
                        $scope.editEncounterDone({encounter: $scope.encounter});
                    }, $exceptionHandler);
                };
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.editEncounterDone(null);
                }
                
                $scope.cmdEnter = function() {
                    $scope.save();
                }

                //=================================
                // START wb-thumb-box
                //=================================
                $scope.performAction = function(code, photos) {
                    if (!photos) {
                        return;
                    }
                    
                    var photoids = photos.map(function(photo) {
                        return photo.id;
                    });
                    
                    switch (code) {
                    case "del": {
                        //
                        // Have to handle the error this way because catch() returns a *new* promise (whose
                        // state is pending, and thus any then's called on the return object will happen as
                        // soon as that is resolved which appears to happen rather than being rejected).
                        // So instead we return the original promise from the post and call catch on that
                        // same promise.
                        //
                        var promise = $http.post("obj/encounter/detachmedia/" + $scope.encounter.id, photoids);
                        promise.catch($exceptionHandler);
                        return promise;
                    }}
                }
                //=================================
                // END wb-thumb-box
                //=================================
            }
        };
    }]
);
