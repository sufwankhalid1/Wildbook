require('./individual_search');
require('./individual_edit');
require('../util/location_edit.js');

angular.module('wildbook.admin').directive(
    'wbEncounterView',
    ["wbDateUtils", function(wbDateUtils) {
        return {
            restrict: 'E',
            scope: {
                data: "=encData"
            },
            templateUrl: 'encounters/encounter_view.html',
            replace: true,
            link: function($scope, elem, attr) {
                $scope.wbDateUtils = wbDateUtils;
            }
        };
    }]
);

angular.module('wildbook.admin').directive(
    'wbEncounterEdit',
    ["$http", "$exceptionHandler", "wbConfig", "wbEncounterUtils", "leafletMapEvents", "wbDateUtils",
     function($http, $exceptionHandler, wbConfig, wbEncounterUtils, leafletMapEvents, wbDateUtils) {
        return {
            restrict: 'E',
            scope: {
                data: "=encData",
                editEncounterDone: "&",
                photosDetached: "&",
                deleted: "&"
            },
            templateUrl: 'encounters/encounter_edit.html',
            replace: true,
            link: function($scope, elem, attr) {
                $scope.module = {};

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
                
                $scope.dateObj = wbDateUtils.dateFromRest($scope.data.encounter.encDate);
                
                $scope.$watch("data.encounter.encDate", function(newVal, oldVal) {
                    $scope.dateObj = wbDateUtils.dateFromRest(newVal);
                });
                
                wbConfig.config()
                .then(function(config) {
                    $scope.allSpecies = config.species;
                });
                
                $scope.save = function() {
                    //md-datetime needs a date obj, so convert to date obj for use, convert back for save
                    $scope.data.encounter.encDate = wbDateUtils.dateToRest($scope.dateObj);

                    //need to make sure we send either a time or nothing
                    if ($scope.data.encounter.endtime) {
                        var endlength = $scope.data.encounter.endtime.length;
                        for (var nn = 0; nn < endlength; nn++) {
                            if ($scope.data.encounter.endtime && $scope.data.encounter.endtime[nn] == null ) {
                                $scope.data.encounter.endtime = null;
                            }
                        }
                    }

                    if ($scope.data.encounter.starttime) {
                        var startlength = $scope.data.encounter.starttime.length;
                        for (var nn = 0; nn < startlength; nn++) {
                            if ($scope.data.encounter.starttime && $scope.data.encounter.starttime[nn] == null ) {
                                $scope.data.encounter.starttime = null;
                            }
                        }
                    }

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
                    if (individual) {
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
                $scope.mapData = {
                    defaults: {
                        scrollWheelZoom: false
                    },
                    tiles: {
                        url: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
                        options: {
                            attribution: '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        }
                    },
                    layers: {
                        overlays: {
                            clusterGroup: {
                                name: 'markercluster',
                                type: 'markercluster',
                                visible: true,
                                layerOptions: {
                                    showCoverageOnHover: false
                                    // TODO: Add in a cluster icon something like this. Probably
                                    // no need for different sizes but...
                                    //
//                                    ,
//                                    iconCreateFunction: function (cluster) {
//                                        var childCount = cluster.getChildCount();
//
//                                        var c = ' marker-cluster-';
//                                        if (childCount < 10) {
//                                            c += 'small';
//                                        } else if (childCount < 100) {
//                                            c += 'medium';
//                                        } else {
//                                            c += 'large';
//                                        }
//
//                                        return new L.DivIcon({ html: '<div><span>' + "CUSTOM" + '</span></div>', className: 'marker-cluster' + c, iconSize: new L.Point(40, 40) });
//                                    }
                                }
                            }
                        }
                    }
                };

                //create map on encounter change
                $scope.$watch('data', function(newVal, oldVal) {
                    $scope.mapBuilt = buildMap();
                }, true);

                function buildMap() {
                    //build marker object
                    var center = {zoom: 8};
                    
                    $scope.mapData.markers = {};
                    
                    if ($scope.data.encounter.location && $scope.data.encounter.location.latitude) {
                        $scope.mapData.markers.mainMaker = {
                            lat: $scope.data.encounter.location.latitude,
                            lng: $scope.data.encounter.location.longitude,
                            draggable: false
                        };

                        center.lat = $scope.data.encounter.location.latitude;
                        center.lng = $scope.data.encounter.location.longitude;
                    }
                    
                    $scope.data.photos.forEach(function (photo) {
                        if (photo.latitude && photo.longitude) {
                            if (!center.lat) {
                                center.lat = photo.latitude;
                                center.lng = photo.longitude;
                            }
                            
                            $scope.mapData.markers['p' + photo.id] = {
                                lat: photo.latitude,
                                lng: photo.longitude,
                                group: 'markercluster',
                                draggable: false
                            };
                        }
                    });
                    
                    if (!center.lat) {
                        center = {lat: 0, lng: 0, zoom: 1};
                    }
                    
                    $scope.mapData.center = center;

                    return true;
                };

                //enable location picker
                $scope.locationPickerState = false;

                var unbindHandler;
                $scope.pickLocation = function() {
                    function unbind() {
                        unbindHandler();
                        unbindHandler = null;
                    }
                    
                    if ($scope.locationPickerState) {
                        $scope.locationPickerState = false;
                        if (unbindHandler) {
                            unbind();
                        }
                    } else {
                        $scope.locationPickerState = true;
                        var mapEvents = leafletMapEvents.getAvailableMapEvents();
                        unbindHandler = $scope.$on('leafletDirectiveMap.click', function(e,  args) {
                            $scope.data.encounter.location.latitude = args.leafletEvent.latlng.lat;
                            $scope.data.encounter.location.longitude = args.leafletEvent.latlng.lng;
                            $scope.locationPickerState = false;
                            //
                            // Hit race condition once in which this was called before the $on returned
                            // the unbindHandler function value. This will hopefully prevent that again.
                            //
                            if (! unbindHandler) {
                                $timeout(function() {
                                    unbind();
                                }, 2000)
                            } else {
                                unbind();
                            }
                        });
                    }
                };

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
                            delete $scope.mapData.markers['p'+photoids];
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
