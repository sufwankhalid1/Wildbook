angular.module('wildbook.admin').directive(
    'wbIndividualEdit',
    ["$http", "$exceptionHandler", "wbConfig", "wbEncounterUtils",
     function($http, $exceptionHandler, wbConfig, wbEncounterUtils) {
        return {
            restrict: 'E',
            scope: {
                data: "=indData",
                editIndividualDone: "&"
            },
            templateUrl: 'partials/individual_edit.html',
            replace: true,
            controller: function($scope) {
                $scope.module = {};
                $scope.getSpecies = function() {
                    return wbConfig.config().species;
                }
                
//                function getDisplayName(individual) {
//                    var name = individual.nickname || "[Unnamed]";
//                    if (! individual.alternateId) {
//                        return name;
//                    }
//                    
//                    return name + " (" + individual.alternateId + ")";
//                }
//                
                $scope.save = function() {
                    $http.post('obj/individual/save', $scope.data)
                    .then(function(result) {
                        $scope.data.id = result.data.id;
                        //
                        // Reset the display name to the new one.
                        //
                        //$scope.data.displayName = getDisplayName($scope.data);
                        $scope.data.displayName = result.data.displayName;
                        
                        $scope.editIndividualDone({individual: $scope.data});
                    }, $exceptionHandler);
                };
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.editIndividualDone({individual: null});
                }
                
                $scope.cmdEnter = function() {
                    $scope.save();
                }
            }
        };
    }]
);
