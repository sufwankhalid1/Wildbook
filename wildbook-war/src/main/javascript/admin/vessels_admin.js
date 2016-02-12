/* global angular, alertplus */
'use strict';

angular.module('wildbook.admin').directive(
    'wbVesselsAdmin',
    ["$http", "wbConfig", function($http, wbConfig) {
        return {
            restrict: 'E',
            scope: {
                readonly: '=',
                vesselid: '=',
                vesselobj: '=?',
                editOnSelect: '@',
                callback: '&'
            },
            templateUrl: 'admin/vessels_admin.html',
            replace: true,
            link: function($scope, element, attr) {
                $http.get('siteadmin/getvessels')
                .then(function(res){
                    $scope.vessels = res.data;
                });

                function showEditOnSelect() {
                    if ($scope.editOnSelect === "true") {
                        $scope.showEdit = true;
                    }
                }

                if ($scope.vesselid) {
                    $scope.vesselInput.vesselId = $scope.vesselid;
                    showEditOnSelect();
                }

                if ($scope.vesselobj) {
                    $scope.vessel = $scope.vesselobj;
                    showEditOnSelect();
                }

                $scope.showEditVessel = function() {
                    $scope.showEdit = true;
                };

                $scope.edit = function(vessel) {
                    if (!vessel) {
                        return;
                    }
                    $scope.vesselInput = angular.copy(vessel);
                    $scope.existingVessel = true;

                    showEditOnSelect();

                    if ($scope.callback) {
                        $scope.callback({vessel: $scope.vesselInput});
                    }
                };

                $scope.newVessel = function() {
                    $scope.cancel();
                    $scope.vesselInput = {};
                    $scope.showEdit = true;
                };

                $scope.cancel = function() {
                    $scope.vessel = null;
                    $scope.vesselInput = null;
                    $scope.vesselSearch = null;
                    $scope.showEdit = false;
                    delete $scope.existingVessel;

                };

                $scope.getOrgId = function(orgId) {
                    $scope.vesselInput.orgId = orgId;
                };

                $scope.save = function() {
                    if (!$scope.vesselInput.name || !$scope.vesselInput.orgId) {
                        if (!$scope.vesselInput.name && $scope.vesselInput.orgId) {
                            alertplus.alert("Please enter a vessel name and choose an organization.");
                        } else if (!$scope.vesselInput.name) {
                            alertplus.alert("Please enter a vessel name.");
                        } else if (!$scope.vesselInput.orgId) {
                            alertplus.alert("Please choose an organization.");
                        }

                        return ;
                    }

                    var exists = false;
                    $scope.vessels.forEach(function(obj) {
                        if (obj === $scope.vesselInput) {
                            exists = true;
                        }
                    });

                    if (exists) {
                        alertplus.alert("Vessel " + $scope.vesselInput.name + " already exist!");
                        return;
                    }

                    if (!exists) {

                        //
                        // this needs to be fixed. so on the vesseltype directive a "new" option is pushed to the config array
                        // I could give it some randomly long id or something (needs some value to return that isnt null since it'll always set the null value as selcted when created)
                        // choose the word new, need to set it null when sending to the server, then need to set it to the id response since that vesseltype is inserted
                        //

                        if ($scope.vesselInput.vesselTypeId === "new") {
                            $scope.vesselInput.vesselTypeId = null;
                            $http.post("siteadmin/savevesseltype", $scope.vesselInput)
                            .then(function(vesselTypeId) {
                                $scope.vesselInput.vesselTypeId = vesselTypeId.data;
                                saveVessel();
                                wbConfig.refreshConfig();
                            });
                        } else {
                            saveVessel();
                        }

                        if ($scope.callback) {
                            $scope.callback({vessel: $scope.vesselInput});
                        }
                    }
                };

                function saveVessel() {
                    $http.post("siteadmin/savevessel", $scope.vesselInput)
                    .then(function (response) {
                        if ($scope.vesselInput.vesselId) {
                            $scope.vessels.forEach(function(obj, key) {
                                if (obj.vesselId === $scope.vesselInput.vesselId) {
                                    $scope.vessels[key] = $scope.vesselInput;
                                }
                            });
                        } else {
                            $scope.vesselInput.vesselId = response.data;
                            $scope.vessels.push($scope.vesselInput);
                        }
                        $scope.cancel();
                    });
                }

                $scope.delete = function() {
                    return alertplus.confirm('Are you sure you want to remove '+ $scope.vesselInput.name +'?', "Delete Vessel", true)
                    .then(function(){
                        $http.post('siteadmin/deletevessel/' + $scope.vesselInput.vesselId)
                        .success(function() {
                            $scope.vessels.forEach(function(vessel, key) {
                                if (vessel.vesselId === $scope.vesselInput.vesselId) {
                                    $scope.vessel.splice(key, 1);
                                }
                            });
                            $scope.cancel();
                        })
                        .error(function(res){
                            alertplus.error(res.message);
                        });
                    });
                };
            }
        };
    }]
);
