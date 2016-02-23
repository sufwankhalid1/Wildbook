/*global angular */
'use strict';

angular.module('wildbook.admin').directive(
    'wbEncounterSearchFields',
    [function() {
        return {
            restrict: 'E',
            templateUrl: 'encounters/encounter_search_fields.html',
            scope: {
                encounter: '='
            },
            replace: true,
            link: function($scope, ele, attr) {
                $scope.daterange = ["on", "before", "after", "between"];
            }
        };
    }]
);
