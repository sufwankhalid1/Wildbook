angular.module('wildbook.admin').directive(
    "wbSiteAdmin",
    ["$http", "$q", "$exceptionHandler",
     function ($http, $q, $exceptionHandler) {
        return {
            restrict: 'E',
            templateUrl: 'pages/siteAdmin.html',
            replace: true,
            controller: function($scope) {
                console.log('test');
            }
        }
    }]
);