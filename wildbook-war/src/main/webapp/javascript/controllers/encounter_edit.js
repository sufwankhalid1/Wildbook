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
                $scope.tbActions = [];
                
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
                $scope.deletePhoto = function(id) {
                    return $http.post("obj/encounter/detachmedia", {submissionid: $scope.encounter.id, mediaid: id})
                           .catch($exceptionHandler);
                }
                
                $scope.performAction = function(code, photos) {
//                    switch (code) {
//                    case "blah": {
//                        
//                    }}
                }
                //=================================
                // END wb-thumb-box
                //=================================
            }
        };
    }]
);
