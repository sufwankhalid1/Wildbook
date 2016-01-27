/* global angular, alertplus */
'use strict';

angular.module('wildbook.admin').directive(
    'wbSpeciesAdmin',
    ["$http", "wbConfig", function($http, wbConfig) {
        return {
            restrict: 'E',
            scope: {},
            templateUrl: 'admin/species_admin.html',
            replace: true,
            link: function($scope, element, attr) {

                var originalCode = null;

                wbConfig.config()
                .then(function(config) {
                    $scope.allSpecies = config.species;
                });

                $scope.search = function() {
                    $scope.cancel();

                    $scope.showSearch = true;
                    $scope.speciesSearch = "";
                };

                $scope.cancel = function() {
                    originalCode = null;
                    $scope.species = null;
                    $scope.speciesInput = null;
                    $scope.showSearch = false;
                    delete $scope.existingSpecies;
                };

                $scope.edit = function(species) {
                    if (!species) {
                        return;
                    }
                    $scope.existingSpecies = true;
                    originalCode = species.code;
                    $scope.speciesInput = angular.copy(species);
                };

                $scope.newSpecies = function() {
                    $scope.cancel();
                    $scope.edit({code: '', name: '', icon: ''});
                    originalCode = null;
                    delete $scope.existingSpecies;
                };

                $scope.save = function() {
                    if(!$scope.speciesInput.name || !$scope.speciesInput.code) {
                        alertplus.alert("Please enter both a species name and a code."
                                        + "<br/> The species code must be unique, lowercase, and have no spaces."
                                        + "<br/> EXAMPLE: <br/> Code: happy_whale <br/> Name: Happy Whale <br/> Icon Code: happy_whale");
                        return;
                    }

                    $http.post("siteadmin/savespecies/"+originalCode, $scope.speciesInput)
                    .then(function () {
                        if (originalCode) {
                            $scope.allSpecies.forEach(function(obj, key) {
                                if (obj.code === originalCode) {
                                    $scope.allSpecies[key] = $scope.speciesInput;
                                }
                            });
                        } else {
                            $scope.allSpecies.push($scope.speciesInput);
                        }

                        $scope.cancel();
                    });
                };

                $scope.delete = function() {
                    return alertplus.confirm('Are you sure you want to remove '+ $scope.speciesInput.name +'?', "Delete Species", true)
                    .then(function(){
                        $http.post('siteadmin/deletespecies', $scope.speciesInput.code)
                        .success(function() {
                            $scope.allSpecies.forEach(function(obj, key) {
                                if (obj.code === $scope.speciesInput.code) {
                                    $scope.allSpecies.splice(key, 1);
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
