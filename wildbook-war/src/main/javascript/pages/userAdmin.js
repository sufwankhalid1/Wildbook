/* global angular, alertplus */
'use strict';

require('../user/user_search.js');
require('../user/user_edit_fields.js');

angular.module('wildbook.admin').directive(
    "wbUserAdmin",
    ["$http", "wbConfig", "$mdDialog",
     function ($http, wbConfig, $mdDialog) {
        return {
            restrict: 'E',
            templateUrl: 'pages/userAdmin.html',
            scope: {
            },
            replace: true,
            controller: function($scope) {

                //get user Roles
                function getRoles(id) {
                    if (!id) {
                        return;
                    }
                    $http.get('admin/api/user/roles/'+id)
                    .then(function(res){
                        $scope.userroles = res.data;
                    });
                }


                $scope.setUser = function(user) {
                    $http.get("admin/api/user/get/" + user.id)
                    .then(function(result) {
                        $scope.user = result.data;
                        getRoles(result.data.id);
                    });
                };

                $scope.clearUser = function() {
                    delete $scope.user;
                };

                $scope.newUser = function() {
                    $scope.user = {};
                };

                $scope.save = function() {
                    $http.post("admin/api/user/usersave", $scope.user)
                    .then(function(response){
                        delete $scope.user;
                    });
                };

                $scope.clearUserCache = function() {
                    $http.post("admin/api/user/cache/clear", {})
                    .then(function(response){
                        alertplus.alert("User cache has been cleared.");
                    });
                };
            }
        };
    }]
);
