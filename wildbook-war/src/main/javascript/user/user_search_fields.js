angular.module('wildbook.admin').directive(
    'wbUserSearchFields',
    ["$http", "$exceptionHandler",  function($http, $exceptionHandler) {
        return {
            restrict: 'E',
            scope: {
                user: '='
            },
            templateUrl: 'user/user_search_fields.html',
            replace: true,
            link: function($scope, ele, attr) {

            }
        }
    }]
);