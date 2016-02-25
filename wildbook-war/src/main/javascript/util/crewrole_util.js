/* global angular, alertplus */
'use strict';

//
//&callback = returns selectedRoleId;
//
angular.module('wildbook.util')
.directive("wbCrewUtils", ["wbConfig", "$http", function(wbConfig, $http) {
    return {
        restrict: 'E',
        scope: {
            selectedRoleId: '=',
            callback: '&'
        },
        templateUrl: 'util/crewrole_util.html',
        link: function($scope, ele, attr){
            $scope.showAddNew = false;
            $scope.selectedCrewRole = {crewRoleId: null, role:null};

            if ($scope.selectedRoleId) {
                $scope.selectedCrewRole.crewRoleId = $scope.selectedRoleId;
            }

            function getConfig() {
                return wbConfig.config()
                    .then(function(config) {
                        $scope.crewroles = config.crewroles;
                        if ($scope.crewroles.length === 0) {
                            $scope.selectedCrewRole.selectedRoleId = "new";
                            $scope.showAddNew = true;
                        }
                    });
            }

            $scope.checkNew = function(crewrole) {
                if (!crewrole) {
                    $scope.showAddNew = true;
                } else {
                    $scope.showAddNew = false;
                    $scope.callback({crewrole: $scope.selectedCrewRole});
                }
            };

            $scope.cancel = function() {
                $scope.showAddNew = false;
            };

            //
            //saves a new crew role, refreshes the crew cache, gets new cache, sets the selectedRoleId to the new role
            //
            $scope.save = function() {

                if ($scope.crewroles.length > 0) {
                    $scope.crewroles.forEach(function (crewrole) {
                        if (crewrole.role.toLowerCase() === $scope.selectedCrewRole.role.toLowerCase()) {
                            alertplus.alert("This role already exist!");
                        }
                    });
                }

                $http.post('admin/api/site/savecrewrole', $scope.selectedCrewRole)
                .then(function(res) {
                    wbConfig.refreshConfig();
                    getConfig().then(function() {
                        $scope.selectedCrewRole.crewRoleId = res.data;
                        $scope.callback({crewrole: $scope.selectedCrewRole});
                        $scope.cancel();
                    });
                });
            };

            getConfig();
        }
    };
}]);
