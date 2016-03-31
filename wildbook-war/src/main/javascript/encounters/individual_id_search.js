/*global angular */
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
                    if (!isNaN(parseInt($scope.id))) {
                        $http.get('search/individual/' + $scope.id)
                        .then(function(res) {
                            if (res.data) {
                                $scope.error = null;
                                $scope.callback({ind: res.data});
                            } else {
                                $scope.error = "Nothing found with that ID.";
                            }
                        });
                    } else {
                        $scope.error = "Please enter a valid ID.";
                    }
                };
            }
        };
    }]
);
