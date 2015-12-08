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
                $scope.save = function() {
                    $http.post('obj/encounter/save', $scope.data)
                    .then(function(result) {
                        //set display name for now
                        $scope.data.individual.displayName = $scope.data.individual.nickname;
                        $scope.editIndividualDone({individual: $scope.data.individual});
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
