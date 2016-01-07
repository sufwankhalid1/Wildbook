require('../user/user_edit_fields');

angular.module('wildbook.admin').directive(
    'wbMyAccountPage',
    ["$http", "$exceptionHandler", "$mdToast", "$timeout", function($http, $exceptionHandler, $mdToast, $timeout) {
        return {
            restrict: 'E',
            templateUrl: 'pages/myAccountPage.html',
            replace: true,
            link: function($scope, element, attr) {
                var origSelf;
                $scope.finishedExports = 0;
                $scope.pendingExports = 0;
                $scope.exportErrors = 0;

                $http.get('obj/user/self').then(function(response){
                    $scope.self = response.data;
                    origSelf = angular.copy(response.data);
                });

                $scope.getExports = function() {
                    $scope.finishedExports = 0;
                    $scope.pendingExports = 0;
                    $scope.exportErrors = 0;

                    $http.get('obj/user/exports').then(function(response){
                        $scope.exports = response.data;

                        $scope.exports.forEach(function(item){
                            if (item.status == 2) {
                                $scope.finishedExports++;
                            }

                            if (item.status == 1) {
                                $scope.pendingExports++;
                            }

                            if (item.error) {
                                $scope.exportErrors++;
                            }

                        });
                    });
                }

                $scope.$watch('refresh', function(){

                    function refreshExports() {
                        if (!$scope.refresh) {
                            return false;
                        }

                        $scope.getExports();
                        refreshTimeout();
                    }
                    
                    function refreshTimeout(){ 
                        $timeout(function(){
                            refreshExports();
                        }, 5000);
                    }

                    refreshExports();
                });

                $scope.timestampToDate = function(timestamp) {
                    if (!timestamp) {
                        return "Not Set"
                    }
                    var date = new Date(timestamp);
                    var hours = date.getHours();
                    var minutes = "0" + date.getMinutes();
                    var seconds = "0" + date.getSeconds();
                    return hours + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);
                }

                $scope.hideExports = function() {
                    $scope.showExports = false;
                }

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

                $scope.viewError = function(err) {
                    alertplus.error(err);
                }

                $scope.cancel = function() {
                    $scope.edit = false;
                    $scope.self = angular.copy(origSelf);
                }

                $scope.getExports();
            }
        }
    }]
);
