angular.module('wildbook.admin').directive(
    'wbEncounterSearchFields',
    ["$http", "$exceptionHandler", function($http, $exceptionHandler) {
        return {
            restrict: 'E',
            templateUrl: 'encounters/encounter_search_fields.html',
            scope: {
                encounter: '='
            },
            replace: true,
            link: function($scope, ele, attr) {
            }
        }
    }]
);