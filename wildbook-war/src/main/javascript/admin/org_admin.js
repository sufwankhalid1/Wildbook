angular.module('wildbook.admin').directive(
    'wbOrgAdmin',
    ["$http", "$exceptionHandler", "wbConfig", "$timeout",
    function($http, $exceptionHandler, wbConfig, $timeout) {
        return {
            restrict: 'E',
            scope: {
            },
            templateUrl: 'organizations/org_admin.html',
            replace: true,
            link: function($scope, element, attr) {

                var cacheOrgs;
                var id = null;

                wbConfig.config()
                .then(function(config) {
                    cacheOrgs = config.orgs;
                    $scope.orgs = angular.copy(cacheOrgs);
                });

                $scope.setOriginal = function() {
                    if ($scope.org) {
                        if ('orgId' in $scope.org) {
                            id = angular.copy($scope.org.orgId);
                        }

                        if ('name' in $scope.org) {
                            $scope.originalName = angular.copy($scope.org.name);
                        }

                        $scope.orgInput = angular.copy($scope.org);
                    }
                }

                $scope.newOrg = function() {
                    $scope.cancel();
                    $scope.orgInput = {displayName: '', name: '', orgId: null};
                    id = null;
                    $scope.showEdit = true;
                }

                $scope.cancel = function() {
                    $scope.org = null;
                    $scope.orgInput = null;
                    $scope.orgSearch = null;
                    id = null;
                    $scope.originalName = null;
                    $scope.orgs = angular.copy(cacheOrgs);
                    $scope.showEdit = false;
                }

                $scope.save = function() {
                    var stopCall = false;

                    if (!$scope.orgInput.name) {
                        alertplus.alert("Please enter an organization name.");
                        return false;
                    };

                    cacheOrgs.forEach(function(obj) {
                        if (obj.name == $scope.orgInput.name) {
                            alertplus.alert("Organization "+$scope.orgInput.name+" already exist!");
                            stopCall = true;
                        }
                    });

                    if (!stopCall) {
                        $http.post("siteadmin/saveorg", $scope.orgInput)
                        .then(function (response) {
                            $scope.$applyAsync(function() {
                                if (id || id === 0) {
                                    $scope.orgs.forEach(function(obj, key) {
                                        if (obj.orgId == id) {
                                            $scope.orgs[key] = $scope.orgInput;
                                        }
                                    });
                                } else {
                                    $scope.orgInput.orgId = response.data;
                                    $scope.orgs.push($scope.orgInput);
                                }
                                cacheOrgs = angular.copy($scope.orgs);
                                $scope.cancel();
                            });
                        });
                    } else {
                        $scope.originalName = null;
                    }
                };
            }
        }
    }]
);