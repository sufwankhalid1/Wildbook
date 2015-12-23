angular.module('wildbook.admin').directive(
    'wbIndividualSearchFields',
    ["$http", "$exceptionHandler", function($http, $exceptionHandler) {
        return {
            restrict: 'E',
            scope: {

            },
            templateUrl: 'encounters/individual_search_fields.html',
            replace: true,
            link: function($scope, ele, attr) {
            
            }
        }
    }]
);