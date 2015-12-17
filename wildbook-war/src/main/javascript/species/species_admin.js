angular.module('wildbook.admin').directive(
    'wbSpeciesAdmin',
    ["$http", "$exceptionHandler", function($http, $exceptionHandler) {
        return {
            restrict: 'E',
            scope: {
            },
            templateUrl: 'species/species_admin.html',
            replace: true,
            link: function($scope, element, attr) {
            
            }
        }
    }]
);