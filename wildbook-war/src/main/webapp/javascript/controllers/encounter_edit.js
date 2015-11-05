wildbook.app.directive(
    'wbEncounterEdit',
    ["$http", "$exceptionHandler", "wbConfig", function($http, $exceptionHandler, wbConfig) {
        return {
            restrict: 'E',
            scope: {
                encounter: "=encounterToEdit",
                editEncounterDone: "&"
            },
            templateUrl: 'util/render?j=partials/encounter_edit',
            replace: true,
            controller($scope) {
                if ($scope.encounter === "new") {
                    $scope.encounter = {individual: {species: wbConfig.config().species[0]}};
                }
                
                function closePanel() {
                    $scope.editEncounterDone({encounter: $scope.encounter});
                }

                $scope.getSpecies = function() {
                    return wbConfig.config().species;
                }
                
                $scope.save = function() {
                    $http.post('obj/encounter/save', $scope.encounter)
                    .then(function(result) {
                        $scope.encounter.id = result.data;
                        closePanel();
                    }, $exceptionHandler);
                };
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.encounter = null;
                    closePanel();
                }
                
                $scope.cmdEnter = function() {
                    $scope.save();
                }
            }
        };
    }]
);
