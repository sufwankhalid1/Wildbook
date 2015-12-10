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
    ["$http", "$exceptionHandler", "wbConfig", "wbEncounterUtils", "leafletMapEvents", "leafletData",
     function($http, $exceptionHandler, wbConfig, wbEncounterUtils, leafletMapEvents, leafletData) {
        return {
            restrict: 'E',
            scope: {
                data: "=encData",
                editEncounterDone: "&",
                photosDetached: "&",
                deleted: "&"
            },
            templateUrl: 'partials/encounter_edit.html',
            replace: true,
            link: function($scope, elem, attr) {
                $scope.module = {};
                var unbindHandler = null;

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
                    
                    wbEncounterUtils.saveEnc($scope.data.encounter)
                    .then(function(result) {
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
                // START leaflet
                //=================================
                $scope.mapTile = {
                    url: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
                    options: {
                        attribution: '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    }
                };

                //create map on encounter change
                $scope.$watch('data', function(newVal, oldVal){
                    $scope.mapBuilt = $scope.buildMap();
                }, true);

                $scope.buildMap = function(){
                    //build marker object
                    $scope.markers = {
                        mainMaker:{
                            lat: $scope.data.encounter.location.latitude,
                            lng: $scope.data.encounter.location.longitude,
                            icon: {},
                            draggable: false
                        }
                    };

                    $scope.data.photos.forEach(function (photo){
                        if(photo.latitude && photo.longitude){
                            $scope.markers['p' + photo.id] = {
                                lat: photo.latitude,
                                lng: photo.longitude,
                                group: 'markercluster',
                                draggable: false
                            };
                        }
                    });

                    //set  lat/long center and scopes
                    $scope.mapData = {
                        activeLatLng: {
                            lat: $scope.data.encounter.location.latitude,
                            lng: $scope.data.encounter.location.longitude,
                            zoom: 15
                        },
                        layers: {
                            overlays: {
                                clusterGroup: {
                                    name: 'markercluster',
                                    type: 'markercluster',
                                    visible: true,
                                    layerOptions: {
                                        showCoverageOnHover: false,
                                        maxClusterRadius: 50
                                    }
                                }
                            }
                        }
                    };

                    return true;
                };

                //enable location picker
                $scope.locationPickerState = false;

                $scope.pickLocation = function() {
                    $scope.locationPickerState = true;
                    var mapEvents = leafletMapEvents.getAvailableMapEvents();
                    unbindHandler = $scope.$on('leafletDirectiveMap.click', function(e,  args){
                        $scope.data.encounter.location.latitude = args.leafletEvent.latlng.lat;
                        $scope.data.encounter.location.longitude = args.leafletEvent.latlng.lng;
                        $scope.locationPickerState = false;
                        unbindHandler();
                        unbindHandler = null;
                    });
                };

                $scope.latLngListener = function() {
                    unbindHandler = $scope.$on('leafletDirectiveMap.click', function(e,  args){
                        $scope.data.encounter.location.latitude = args.leafletEvent.latlng.lat;
                        $scope.data.encounter.location.longitude = args.leafletEvent.latlng.lng;
                    });
                }


                $scope.deleteEncounter = function() {
                    return alertplus.confirm('Are you sure you want to delete this encounter?', "Delete Encounter", true)
                    .then(function() {
                        $http.post("obj/encounter/delete", $scope.data.encounter)
                        .then(function() {
                            $scope.deleted({encdata: $scope.data});
                        }, $exceptionHandler);
                    });
                };

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
                            delete $scope.markers['p'+photoids];
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
