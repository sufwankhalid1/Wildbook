angular.module('wildbook.admin').directive(
    'wbIndividualSearchFields',
    ["$http", "$exceptionHandler", "wbConfig", function($http, $exceptionHandler, wbConfig) {
        return {
            restrict: 'E',
            scope: {
                individual: '='
            },
            templateUrl: 'encounters/individual_search_fields.html',
            replace: true,
            link: function($scope, ele, attr) {
                wbConfig.config()
                .then(function(config) {
                    $scope.allSpecies = config.species;
                });
                
                $scope.clearSpecies = function() {
                    $scope.individual.species = undefined;
                }
            }
        }
    }]
);