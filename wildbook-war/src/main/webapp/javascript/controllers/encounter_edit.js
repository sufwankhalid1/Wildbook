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
                    buttonIcon: "trash",
                    tooltip: "Remove/Detach",
                    confirm: { message: "Are you sure you want to detach selected images from this encounter?"}
                }];
                
                if ($scope.encounter === "new") {
                    $scope.encounter = {
                        individual: {species: wbConfig.config().species[0]},
                        photos: []
                    };
                } else {
                    if (! $scope.encounter.photos) {
                        //
                        // TODO: Do we need to have a .then($apply) here?
                        //
                        wbEncounterUtils.getMedia($scope.encounter);
                    }
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
                        $http.post("obj/encounter/detachmedia/" + $scope.encounter.id, photoids)
                        .then(function() {
                            //
                            // TODO: Do we need to do a filter like this?
                            // Might just be auto-filtered from thumb box.
                            //
//                            $scope.photos = $scope.photos.filter(function(photo) {
//                                for (var ii = 0; ii < images.length; ii++) {
//                                    if (images[ii].id === photo.id) {
//                                        return false;
//                                    }
//                                    return true;
//                                }
//                            });
                        }, $exceptionHandler);
                    }}
                }
                //=================================
                // END wb-thumb-box
                //=================================
            }
        };
    }]
);
