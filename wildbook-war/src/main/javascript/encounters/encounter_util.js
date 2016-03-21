/* global angular, alertplus */
'use strict';

angular.module('wildbook.encounters', [])
.factory("wbEncounterUtils", ["$http", "$q", "wbConfig", "wbDateUtils", "$exceptionHandler", function($http, $q, wbConfig, wbDateUtils, $exceptionHandler) {
    var config;
    wbConfig.config()
    .then(function(theConfig) {
        config = theConfig;
    });
    return {
        getMedia: function(encdata) {
            if (! encdata.photos) {
                return $http.get("api/encounter/getmedia/" + encdata.encounter.id)
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
            var encounter = {individual: {species: config.defaultSpecies || config.species[0] }};

            if (!selectedPhotos || !selectedPhotos.length) {
                return $q.resolve({
                    encounter: encounter,
                    photos: [],
                    autofilledFrom: null
                });
            }

            var platitude = null;
            var plongitude = null;
            var dates = [];

            var photo_encounters;
            var photo_encounter;

            var encounter_used;

            function createDate(selectedPhotos) {
                var found = false;
                var timeline;
                selectedPhotos.forEach(function(photo) {
                    if (photo.timestamp && !found) {
                        timeline = wbDateUtils.compareDates(dates);

                        if (timeline) {
                            found = true;
                        }
                    }
                });

                if (found) {
                    return timeline.newest.slice(0, 3);
                }
            }

            function setDate() {
                var timeline = wbDateUtils.compareDates(dates);

                if (timeline) {
                    encounter.encDate = timeline.newest.slice(0, 3);
                    encounter.starttime = timeline.oldest.slice(3, timeline.oldest.length);
                    encounter.endtime = timeline.newest.slice(3, timeline.newest.length);
                }
            }

            function compareDates() {
                //check if same day, if so, compare
                if (!wbDateUtils.sameDay(dates)) {
                    return $q.reject("These photos were taken on different days!<br/> Please choose images that occured during the same encounter.");
                }
            }

            function encounterExist(data) {
                //
                //taking first photo's  first encounter and setting up encounter based on that info
                //
                photo_encounters = data;
                photo_encounters.forEach(function(resencounter){
                    if (resencounter) {
                        photo_encounter = resencounter;
                    }
                });

                if ((photo_encounter.location.latitude && photo_encounter.location.longitude)
                    || photo_encounter.location.verbatimLocation || photo_encounter.location.locationid) {
                    encounter.location = photo_encounter.location;
                }

                if (photo_encounter.starttime) {
                    encounter.starttime = photo_encounter.starttime;
                }

                if (photo_encounter.endtime) {
                    encounter.endtime = photo_encounter.endtime;
                }

                if (photo_encounter.encDate) {
                    encounter.encDate = photo_encounter.encDate;
                } else {
                    encounter.encDate = createDate();
                }

                if (photo_encounter.individual.avatarFull) {
                    encounter.individual.avatarFull = photo_encounter.individual.avatarFull;
                    encounter.individual.avatar = photo_encounter.individual.thumbUrl;
                }

                if (encounter.encDate) {
                    encounter_used = "Autofilled from encounter: " + photo_encounter.individual.displayName + ", " + wbDateUtils.dateStringFromRest(encounter.encDate);
                } else {
                    encounter_used = "Autofilled from encounter: " + photo_encounter.individual.displayName;
                }
            }

            function newEncounter() {
                selectedPhotos.forEach(function(photo){
                    if (photo.latitude) {
                        platitude = photo.latitude;
                    }
                    if (photo.longitude) {
                        plongitude = photo.longitude;
                    }

                    //create date array for wbDateUtils
                    if (photo.timestamp) {
                        dates.push(photo.timestamp);
                    }

                    if (!encounter.individual.avatarFull) {
                        encounter.individual.avatarFull = photo;
                        encounter.individual.avatar = photo.thumbUrl;
                    }
                });

                if (platitude && plongitude) {
                    encounter.location = {latitude: platitude, longitude: plongitude};
                } else if (submission) {
                    encounter.location = {latitude: submission.latitude, longitude: submission.longitude};
                } else {
                    encounter.location = {latitude: null, longitude: null};
                }

                compareDates();

                setDate();
            }

            return $http.post('api/encounter/checkDuplicateImage', selectedPhotos)
                .then(function(res){
                    if (res.data && res.data.length && res.data[0] !== null) {
                        encounterExist(res.data);
                    } else {
                        newEncounter();
                    }

                    return $q.resolve({
                        encounter: encounter,
                        photos: selectedPhotos,
                        autofilledFrom: encounter_used
                    });
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
            if (alreadyAlerted) {
                return $http.post("admin/api/encounter/delete", enc)
                .then(function() {
                }, $exceptionHandler);
            } else {
                return alertplus.confirm('Are you sure you want to delete this encounter?', "Delete Encounter", true)
                .then(function() {
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
                });
            }
        }
    };
}]);
