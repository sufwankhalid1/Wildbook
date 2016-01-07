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
                $scope.setUser = function(user) {
                    $http.get("useradmin/user/" + user.id)
                    .then(function(result) {
                        $scope.user = result.data;
                    });
                }; 

                $scope.clearUser = function() {
                    delete $scope.user;
                };

                $scope.newUser = function() {
                    $scope.user = {};
                };

                $scope.save = function() {
                    $http.post("useradmin/usersave", $scope.user)
                    .then(function(response){
                        delete $scope.user;
                    });
                };
            }
        }
    }]
);