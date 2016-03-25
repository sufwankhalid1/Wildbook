/*global angular, alertplus */
'use strict';

angular.module('wildbook.admin').directive(
    'wbIndividualIdSearch',
    ['$http', function($http) {
        return {
            restrict: 'E',
            templateUrl: 'encounters/individual_id_search.html',
            scope: {
                id: '=',
                callback: '&'
            },
            replace: true,
            link: function($scope, ele, attr) {
                //searches for individuals by id
                $scope.search = function() {
                    if ($scope.id) {
                        $http.get('search/individual/'+$scope.id)
                        .then(function(res) {
                            $scope.callback({ind: res.data});
                        });
                    } else {
                        alertplus.alert("Please enter an ID");
                    }
                };
            }
        };
    }]
);
