require('../user/user_edit_fields');

angular.module('wildbook.admin').directive(
    'wbMyAccountPage',
    ["$http", "$exceptionHandler", "$mdToast", function($http, $exceptionHandler, $mdToast) {
        return {
            restrict: 'E',
            templateUrl: 'pages/myAccountPage.html',
            replace: true,
            link: function($scope, element, attr) {
                var origSelf;

                $http.get('obj/user/self').then(function(response){
                    $scope.self = response.data;
                    origSelf = angular.copy(response.data);
                });

                $scope.save = function() {
                    $http.post("useradmin/usersave", $scope.self)
                    .then(function(response){
/*                        $mdToast.show(
                            $mdToast.simple()
                                .content('Info Updated!')
                                .position('middle')
                                .hideDelay(3000)
                        )*/
                        $scope.edit = false;
                        origSelf = angular.copy($scope.self);
                    });
                };

                $scope.cancel = function() {
                    $scope.edit = false;
                    $scope.self = angular.copy(origSelf);
                }
            }
        }
    }]
);
