angular.module('wildbook.admin').directive(
    'wbSpeciesAdmin',
    ["$http", "$exceptionHandler", "wbConfig", function($http, $exceptionHandler, wbConfig) {
        return {
            restrict: 'E',
            scope: {
            },
            templateUrl: 'admin/species_admin.html',
            replace: true,
            link: function($scope, element, attr) {

                var originalCode = null;

                wbConfig.config()
                .then(function(config) {
                    $scope.allSpecies = config.species;
                });

                $scope.cancel = function() {
                    originalCode = null;
                    $scope.species = null;
                    $scope.speciesInput = null;
                    $scope.speciesSearch = "";
                    $scope.showEdit = false;
                }

                $scope.setOriginal = function() {
                    if ($scope.species && 'code' in $scope.species) {
                        originalCode = $scope.species.code;
                        $scope.speciesInput = angular.copy($scope.species);
                    }
                }

                $scope.newSpecies = function() {
                    $scope.cancel();
                    $scope.speciesInput = {name: null, code: null, icon: null};
                    $scope.showEdit = true;
                }

                $scope.save = function() {
                    if(!$scope.speciesInput.name && !$scope.speciesInput.code) {
                        alertplus.alert("Please enter both a species name and a code.");
                        return;
                    }

                    $http.post("siteadmin/savespecies/"+originalCode, $scope.speciesInput)
                    .then(function () {
                        if (originalCode) {
                            $scope.allSpecies.forEach(function(obj, key) {
                                if (obj.code == originalCode) {
                                    $scope.allSpecies[key] = $scope.speciesInput;
                                }
                            });
                        } else {
                            $scope.allSpecies.push($scope.speciesInput);
                        }

                        $scope.cancel();
                    });
                }
            }
        }
    }]
);