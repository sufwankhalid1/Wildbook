angular.module('wildbook.admin').directive(
    'wbSpeciesAdmin',
    ["$http", "wbConfig", function($http, wbConfig) {
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

                $scope.search = function() {
                    $scope.cancel();
                    
                    $scope.showSearch = true;
                    $scope.speciesSearch = "";
                }
                
                $scope.cancel = function() {
                    originalCode = null;
                    $scope.species = null;
                    $scope.speciesInput = null;
                    $scope.showSearch = false;
                };

                $scope.edit = function(species) {
                    if (!species) {
                        return;
                    }
                    
                    originalCode = species.code;
                    $scope.speciesInput = angular.copy(species);
                };

                $scope.newSpecies = function() {
                    $scope.cancel();
                    $scope.edit({code: '', name: '', icon: ''});
                };

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
                };
            }
        }
    }]
);