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
                $timeout(function(){console.log(wbConfig.config());},1000)
                //$scope.orgs = wbConfig.config().orgs;
            }
        }
    }]
);