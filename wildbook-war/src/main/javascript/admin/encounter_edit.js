require('./individual_search');
require('./individual_edit');

angular.module('wildbook.admin').directive(
    'wbEncounterView',
    [function() {
        return {
            restrict: 'E',
            scope: {
                data: "=encData"
            },
            templateUrl: 'partials/encounter_view.html',
            replace: true
        };
    }]
);

angular.module('wildbook.admin').directive(
    'wbEncounterEdit',
    ["$http", "$exceptionHandler", "wbConfig", "wbEncounterUtils",
     function($http, $exceptionHandler, wbConfig, wbEncounterUtils) {
        return {
            restrict: 'E',
            scope: {
                data: "=encData",
                editEncounterDone: "&",
                photosDetached: "&"
            },
            templateUrl: 'partials/encounter_edit.html',
            replace: true,
            link: function($scope, ele, attr) {
                $scope.module = {};
                //$scope.data = angular.copy($scope.originalData);
                $scope.tbActions = [{
                    code: "del",
                    shortcutKeyCode: 68,
                    type: "warn",
                    icon: "bookmark-remove",
                    tooltip: "Remove/Detach",
                    confirm: { message: "Are you sure you want to detach selected images from this encounter?"}
                }];
                
                if (!$scope.data.photos) {
                    wbEncounterUtils.getMedia($scope.data.encounter);
                }
                $scope.getSpecies = function() {
                    return wbConfig.config().species;
                }
                
                $scope.save = function() {
                    if ($scope.encounterForm.$invalid) {
                        alertplus.alert("There are errors on the form.");
                        return;
                    }
                    
                    $http.post('obj/encounter/save', $scope.data.encounter)
                    .then(function(result) {
                        $scope.data.encounter.id = result.data;
                        //angular.copy($scope.data, $scope.originalData);
                        $scope.editEncounterDone({encdata: $scope.data});
                    }, $exceptionHandler);
                };
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.editEncounterDone(null);
                }
                
                $scope.cmdEnter = function() {
                    $scope.save();
                }
                
                $scope.findIndividual = function() {
                    $scope.module.individualSearch = true;
                }
                
                $scope.editIndividual = function() {
                    $scope.module.individualEdit = true;
                }
                
                $scope.editIndividualDone = function(individual){
                    $scope.module.individualEdit = false;
                    if(individual){
                    	$scope.data.encounter.individual = individual;
                    }
                }
                
                $scope.searchIndividualDone = function(individual) {
                    $scope.module.individualSearch = false;
                    if (individual) {
                        $scope.data.encounter.individual = individual;
                    }
                }

                //=================================
                // START wb-thumb-box
                //=================================
                $scope.performAction = function(code, photos) {
                    if (!photos) {
                        return;
                    }
                    
                    var photoids = photos.map(function(photo) {
                        return photo.id;
                    });
                    
                    switch (code) {
                    case "del": {
                        //
                        // Have to handle the error this way because catch() returns a *new* promise (whose
                        // state is pending, and thus any then's called on the return object will happen as
                        // soon as that is resolved which appears to happen rather than being rejected).
                        // So instead we return the original promise from the post and call catch on that
                        // same promise.
                        //
                        var promise = $http.post("obj/encounter/detachmedia/" + $scope.data.encounter.id, photoids)
                        .then(function() {
                            $scope.photosDetached({photos: photos});
                        });
                        promise.catch($exceptionHandler);
                        return promise;
                    }}
                }
                //=================================
                // END wb-thumb-box
                //=================================
            }
        };
    }]
);
