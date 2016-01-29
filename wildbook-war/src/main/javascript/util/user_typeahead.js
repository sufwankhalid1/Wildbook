/* global angular */
'use strict';

/*
@search = starting search string.
&getuser = avoiding two way binding to prevent modifying user obj, using a getter function
&callback = callback function with selected user obj
*/

angular.module('wildbook.util')
.directive('userTypeahead', ['$timeout', '$http', '$exceptionHandler', '$q', function($timeout, $http, $exceptionHandler, $q) {
    return {
        restrict: 'E',
        templateUrl: 'util/user_typeahead.html',
        scope: {
            search: '@',
            getuser: '&',
            callback: '&'
        },
        link: function($scope, ele, attr) {
            $scope.focus = false;

            $scope.user = $scope.getuser();

            function reset() {
                $scope.focus = false;
                $scope.showEdit = false;
            }

            $scope.searchUsers = function(query) {
                if (!query) {
                    return $q.reject();
                }

                return $http.post('obj/user/searchusers', query)
                .then(function(res) {
                    return res.data;
                });
            };

            $scope.setUser = function(selectedUser) {
                if (!selectedUser) {
                    return;
                }

                $scope.user = selectedUser;

                if ($scope.callback) {
                    $scope.callback({user: $scope.user});
                }

                reset();
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
