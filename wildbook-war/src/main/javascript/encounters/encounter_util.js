/* global angular, alertplus */
'use strict';

angular.module('wildbook.encounters', [])
.factory("wbEncounterUtils", ["$http", "$q", "wbConfig", "wbDateUtils", "$exceptionHandler", "$mdDialog",
         function($http, $q, wbConfig, wbDateUtils, $exceptionHandler, $mdDialog) {
    var config;
    wbConfig.config()
    .then(function(theConfig) {
        config = theConfig;
    });
    return {
        getMedia: function(encdata) {
            if (! encdata.photos) {
                return $http.get("admin/api/encounter/getmedia/" + encdata.encounter.id)
                .then(function(result) {
                    encdata.photos = result.data;
                    return encdata;
                });
            }
            return $q.resolve();
        },
        addPhotos: function(encounter, newphotos) {
            var newphotoids = newphotos.map(function(photo) {
                return photo.id;
            });

            return $http.post("admin/api/encounter/addmedia/" + encounter.id, newphotoids);
        },
        createNewEncData: function(selectedPhotos, submission) {
            //if photos are selected add them to the new encounter
            function emptyEnc() {
                return {individual: {species: config.defaultSpecies || config.species[0] }};
            }

            if (!selectedPhotos || !selectedPhotos.length) {
                return $q.resolve({
                    encounter: emptyEnc(),
                    photos: [],
                    autofilledFrom: null
                });
            }

            var metadata = {
                dates: []
            };

            selectedPhotos.forEach(function(photo){
                if (metadata.latitude && photo.latitude) {
                    metadata.latitude = photo.latitude;
                }

                if (!metadata.longitude && photo.longitude) {
                    metadata.longitude = photo.longitude;
                }

                //create date array for wbDateUtils
                if (photo.timestamp) {
                    metadata.dates.push(photo.timestamp);
                }
            });

            if (!wbDateUtils.sameDay(metadata.dates)) {
                return $q.reject("These photos were taken on different days!<br/> Please choose images that occured during the same encounter.");
            }

            function setEncounterImage(encounter, displayPhoto) {
                encounter.displayImage = displayPhoto;
                if (!encounter.individual.avatarFull) {
                    encounter.individual.avatarFull = displayPhoto;
                    encounter.individual.avatar = displayPhoto.thumbUrl;
                }
            }

            function createFromExisting(existingEnc, displayPhoto) {
                var encounter = emptyEnc();

                if ((existingEnc.location.latitude && existingEnc.location.longitude)
                    || existingEnc.location.verbatimLocation || existingEnc.location.locationid) {
                    encounter.location = existingEnc.location;
                }

                if (existingEnc.starttime) {
                    encounter.starttime = existingEnc.starttime;
                }

                if (existingEnc.endtime) {
                    encounter.endtime = existingEnc.endtime;
                }

                encounter.encDate = existingEnc.encDate;

                setEncounterImage(encounter, displayPhoto);
                return encounter;
            }

            function newEncounter(metadata, displayPhoto) {
                var encounter = emptyEnc();
                if (metadata.latitude && metadata.longitude) {
                    encounter.location = {latitude: metadata.latitude, longitude: metadata.longitude};
                } else if (submission) {
                    encounter.location = {latitude: submission.latitude, longitude: submission.longitude};
                } else {
                    encounter.location = {latitude: null, longitude: null};
                }

                var timeline = wbDateUtils.compareDates(metadata.dates);

                if (timeline) {
                    encounter.encDate = timeline.newest.slice(0, 3);
                    encounter.starttime = timeline.oldest.slice(3, timeline.oldest.length);
                    encounter.endtime = timeline.newest.slice(3, timeline.newest.length);
                }

                setEncounterImage(encounter, displayPhoto);
                return encounter;
            }

            return $http.post('admin/api/encounter/getFromMedia', selectedPhotos.map(function(photo) {return photo.id;}))
            .then(function(res) {
                function getEncDisplay(encounter) {
                    return encounter.individual.displayName + " on " + wbDateUtils.dateStringFromRest(encounter.encDate);
                }

                if (res.data && res.data.length) {
                    if (res.data.length === 1) {
                        return $q.resolve({
                            encounter: createFromExisting(res.data[0], selectedPhotos[0]),
                            photos: selectedPhotos,
                            autofilledFrom: getEncDisplay(res.data[0])
                        });
                    } else {
                        return $mdDialog.show({
                             locals: {
                                 encounters: res.data
                             },
                             template:
                               '<md-dialog aria-label="List dialog">' +
                               '    <md-toolbar>' +
                               '        <div class="md-toolbar-tools">' +
                               '            <h2>Pick Encounter To Autofill From</h2>' +
                               '            <span flex></span>' +
                               '        </div>' +
                               '    </md-toolbar>' +
                               '    <md-dialog-content layout-align="center center" class="md-dialog-content" layout="row" layout-wrap>' +
                               '        <md-button flex="100" ng-click="select(encounter)" class="not-all-caps mlrb-8" ng-repeat="encounter in encounters">' +
                               '            <img ng-src="{{encounter.displayImage.thumbUrl}}">' +
                               '            <span>{{label(encounter)}}</span>' +
                               '        </md-button>' +
                               '        <md-button class="not-all-caps" ng-click="select()" flex="100">New Encounter Not Auto-filled From Any Of These</md-button>' +
                               '        <md-button ng-click="cancel()" flex="100">Cancel</md-button>' +
                               '    </md-dialog-content>' +
                               '</md-dialog>'
                             ,
                             controller: ['$scope', '$mdDialog', 'encounters', function($scope, $mdDialog, encounters) {
                                 $scope.encounters = encounters;
                                 $scope.select = function(encounter) {
                                     $mdDialog.hide(encounter);
                                 };
                                 $scope.cancel = function() {
                                     $mdDialog.cancel();
                                 };
                                 $scope.label = function(encounter) {
                                     return getEncDisplay(encounter);
                                 };
                             }]
                        })
                        .then(function(encounter) {
                            if (encounter) {
                                return $q.resolve({
                                    encounter: createFromExisting(encounter, selectedPhotos[0]),
                                    photos: selectedPhotos,
                                    autofilledFrom: getEncDisplay(encounter)
                                });
                            }

                            return $q.resolve({
                                encounter: newEncounter(metadata, selectedPhotos[0]),
                                photos: selectedPhotos,
                                autofilledFrom: null
                            });
                        }, function() {
                            return $q.reject();
                        });
                    }
                } else {
                    return $q.resolve({
                        encounter: newEncounter(metadata, selectedPhotos[0]),
                        photos: selectedPhotos,
                        autofilledFrom: null
                    });
                }
            });
        },
        getEncData: function(encounter) {
            return this.getMedia({encounter: encounter})
            .then(function(encdata) {
                return encdata;
            });

        },
        saveEnc: function(enc) {
            return  $http.post('admin/api/encounter/save', enc)
            .then(function(result) {
                enc.id = result.data.encounterid;
                enc.individual.id = result.data.individualid;
                return enc;
            });
        },
        delEnc: function(enc, alreadyAlerted) {
            if (!enc.id) {
                return $q.resolve();
            }

            function deleteIt() {
                return $http.post("admin/api/encounter/delete", enc)
                .then(function(result) {
                    if (result.data.orphanedIndividual) {
                        if (! enc.individual.id) {
                            return;
                        }

                        //
                        // If we don't have a display name defined then we are definitely just deleting a new dummy
                        // individual so don't even ask.
                        //
                        if (enc.displayName === undefined) {
                            return $http.post("admin/api/individual/delete", enc.individual);
                        }

                        return alertplus.confirm('The individual ['
                                                 + enc.individual.displayName || enc.individual.id
                                                 + '] would be orphaned, delete as well?', "Delete Individual", true)
                        .then(function() {
                            return $http.post("admin/api/individual/delete", enc.individual);
                        });
                    }
                }, $exceptionHandler);
            }

            if (alreadyAlerted) {
                return deleteIt();
            } else {
                return alertplus.confirm('Are you sure you want to delete this encounter?', "Delete Encounter", true)
                .then(deleteIt);
            }
        }
    };
}]);
