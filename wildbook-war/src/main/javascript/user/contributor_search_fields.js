angular.module('wildbook.admin').directive(
    'wbContributorSearchFields',
    ["$http", "$exceptionHandler",  function($http, $exceptionHandler) {
        return {
            restrict: 'E',
            scope: {
                contributor: '='
            },
            templateUrl: 'user/contributor_search_fields.html',
            replace: true,
            link: function($scope, ele, attr) {

            }
        }
    }]
);