/*global angular */
'use strict';

angular.module('wildbook.admin').directive(
    'wbOrphanedEncSearch',
    ['$http', function($http) {
        return {
            restrict: 'E',
            templateUrl: 'encounters/orphaned_search.html',
            scope: {
                callback: '&'
            },
            replace: true,
            link: function($scope, ele, attr) {
                //searches for individuals without encounters
                $scope.orphaned = function() {
                    $http.get('search/orphaned')
                    .then(function(res) {
                        $scope.callback({inds: res.data});
                    });
                };
            }
        };
    }]
);
