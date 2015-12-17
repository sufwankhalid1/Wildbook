angular.module('wildbook.admin').directive(
    'wbOrgAdmin',
    ["$http", "$exceptionHandler", "wbConfig", "$timeout",
    function($http, $exceptionHandler, wbConfig, $timeout) {
        return {
            restrict: 'E',
            scope: {
                searchUserDone: "&"
            },
            templateUrl: 'organizations/org_admin.html',
            replace: true,
            link: function($scope, element, attr) {
                wbConfig.config()
                        .then(function(config) {
                            $scope.species = config.species;
                        });
            }
        }
    }]
);