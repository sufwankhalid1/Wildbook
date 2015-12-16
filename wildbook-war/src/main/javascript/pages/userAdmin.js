require('../user/user_search.js');

angular.module('wildbook.admin').directive(
    "wbUserAdmin",
    ["$http", "$q", "$exceptionHandler",
     function ($http, $q, $exceptionHandler) {
        return {
            restrict: 'E',
            templateUrl: 'pages/userAdmin.html',
            replace: true,
            controller: function($scope) {

                //hide the close button on the search view
                $scope.showClose = false;

                $scope.setUser = function(user) {
                    $http.get("useradmin/user/"+user.id)
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
                    $http.post("useradmin/usersave", $scope.user);
                };
            }
        }
    }]
);