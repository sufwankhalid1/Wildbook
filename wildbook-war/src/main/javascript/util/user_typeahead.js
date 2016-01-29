/* global angular */
'use strict';

angular.module('wildbook.util')
.directive('userTypeahead', ['$timeout', '$http', function($timeout, $http) {
    return {
        restrict: 'E',
        templateUrl: 'util/user_typeahead.html',
        scope: {
            search: '@',
            user: '='
        },
        link: function($scope, ele, attr) {

            $scope.$watch('search', function(){
                //console.log($scope.search);
            });

            $scope.setUser = function(selectedUser) {
                $scope.user = selectedUser;
            };

            $scope.getUsers = function() {
                $timeout(function() {
                    searchUsers($scope.search);
                }, 250);
            };

            var searchUsers = function(q) {
                console.log(q);
                if (!q) {
                    return;
                }

                $http.post('obj/user/searchusers', q)
                .success(function(res) {
                    $scope.users = res;
                });
            };

            $scope.noFocus = function(blur) {
                if (blur) {
                    $scope.$apply(function() {
                        $scope.focus = false;
                    });
                }
            };
        }
    };
}]);
