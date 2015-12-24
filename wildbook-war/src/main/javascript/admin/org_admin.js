angular.module('wildbook.admin').directive(
    'wbOrgAdmin',
    ["$http", "wbConfig", function($http, wbConfig) {
        return {
            restrict: 'E',
            scope: {
            },
            templateUrl: 'admin/org_admin.html',
            replace: true,
            link: function($scope, element, attr) {
                wbConfig.config()
                .then(function(config) {
                    $scope.orgs = config.orgs;
                });

                $scope.edit = function(org) {
                    $scope.orgInput = angular.copy(org);
                }

                $scope.newOrg = function() {
                    $scope.cancel();
                    $scope.orgInput = {};
                    $scope.showEdit = true;
                }

                $scope.cancel = function() {
                    $scope.org = null;
                    $scope.orgInput = null;
                    $scope.orgSearch = null;
                    $scope.showEdit = false;
                }

                $scope.save = function() {
                    if (!$scope.orgInput.name) {
                        alertplus.alert("Please enter an organization name.");
                        return ;
                    };

                    var exists = false;
                    $scope.orgs.forEach(function(obj) {
                        if (obj.name == $scope.orgInput.name) {
                            exists = true;
                        }
                    });

                    if (exists) {
                        alertplus.alert("Organization " + $scope.orgInput.name + " already exist!");
                        return;
                    }

                    if (!exists) {
                        $http.post("siteadmin/saveorg", $scope.orgInput)
                        .then(function (response) {
                            if ($scope.orgInput.orgId) {
                                $scope.orgs.forEach(function(obj, key) {
                                    if (obj.orgId == $scope.orgInput.orgId) {
                                        $scope.orgs[key] = $scope.orgInput;
                                    }
                                });
                            } else {
                                $scope.orgInput.orgId = response.data;
                                $scope.orgs.push($scope.orgInput);
                            }
                            $scope.cancel();
                        });
                    }
                };
            }
        }
    }]
);