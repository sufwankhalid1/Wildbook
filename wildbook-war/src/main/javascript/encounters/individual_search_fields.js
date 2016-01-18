/* global angular */
'use strict';

angular.module('wildbook.admin').directive(
    'wbIndividualSearchFields',
    ["wbConfig", function(wbConfig) {
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
                };
            }
        };
    }]
);
