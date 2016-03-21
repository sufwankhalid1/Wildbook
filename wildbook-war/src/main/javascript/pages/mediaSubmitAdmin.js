/* global angular, alertplus */
'use strict';

require('../encounters/encounter_edit.js');
require('../encounters/encounter_search.js');
require('../survey/survey_edit.js');
require('../survey/survey_search.js');
require('../survey/crew_edit.js');
require('../user/user_search.js');

angular.module('wildbook.admin').directive(
    "wbMediaSubmissionAdmin",
    ["$http", "$q", "$exceptionHandler", "wbDateUtils", "wbLangUtils", "wbEncounterUtils", "$window", "wbSurveyUtils",
     function ($http, $q, $exceptionHandler, wbDateUtils, wbLangUtils, wbEncounterUtils, $window, wbSurveyUtils) {
        return {
            restrict: 'E',
            templateUrl: 'pages/mediaSubmitAdmin.html',
            scope:{},
            replace: true,
            controller: function($scope) {
                $scope.numencs = {};
                $scope.encOpen = [];
                $scope.searchStatus = '';
                $scope.data = {
                    submission: null,
                    photos: null,
                    encs: [],
                    selectedimgs: null,
                    surveyEncs: [],
                    activeEncData: null,
                    searchingSubmitter: false,
                    newSurveyEnc: false,
                    module: {
                        encounterEdit: null,
                        encounterSearch: false,
                        surveyEdit: null,
                        surveySearch: false
                    },
                    tbActions: [{
                        code: "add",
                        shortcutKeyCode: 65,
                        icon: "bookmark-plus",
                        tooltip: "Add to active encounter"
                    }],
                    indicators: [{def: {displayClass: "encounter", type: "number"},
                        values: $scope.numencs
                    }]
                };

                $scope.active = {
                        survey: false,
                        surveyEnc: false,
                        encounter: false
                };

                function attachEncounter(encdata, surveyEnc) {
                    if (! encdata) {
                        return;
                    }

                    //close side nav if open and clear actives
                    $scope.encOpen = [false];

                    if (surveyEnc) {
                        if (surveyEnc.encs) {
                            if (wbLangUtils.existsInArray(surveyEnc.encs, function(item) {
                                if (item.encounter.id === encdata.encounter.id) {
                                    alertplus.alert("This encounter is already in this survey");
                                }
                                return item.encounter.id === encdata.encounter.id;
                            })) {
                                //
                                // Just return as we already have this encounter attached to this survey.
                                //
                                return;
                            }
                        } else {
                            //
                            // Create empty array for encounters so that we can add one below.
                            //
                            surveyEnc.encs = [];
                        }
                        // Call to add encounter to survey.
                        $http.post("admin/api/survey/addencounter",
                                   {surveypartid: surveyEnc.surveypart.part.surveyPartId, encounterid: encdata.encounter.id})
                        .then(function() {
                            surveyEnc.encs.push(encdata);
                            alertplus.alert("Added encounter to survey number: " + surveyEnc.surveypart.survey.surveyNumber);

                            $scope.data.encs.forEach(function(enc, index) {
                                if (enc.encounter.id === encdata.encounter.id) {
                                    $scope.data.encs.splice(index, 1);
                                }
                            });
                            $scope.data.newSurveyEnc = false;
                        });
                    } else {
                        if (! wbLangUtils.existsInArray($scope.data.encs, function(item) {
                            return (item.encounter.id === encdata.encounter.id);
                        })) {
                            $scope.data.encs.push(encdata);
                        }
                    }
                }

                function addSurveyEncounters(surveypart, encounters) {
                    $scope.data.surveyEncs.push({surveypart: surveypart, encs: encounters});
                }

                $scope.emailSubmitter = function() {
                    $http.get("admin/api/user/get/" + $scope.data.submission.user.id)
                    .then(function(result) {
                        $window.location = "mailto:" + result.data.email;
                    }, $exceptionHandler);
                };

                //call in function for user typeahead directive
                $scope.getUser = function() {
                    if ($scope.data.submission && $scope.data.submission.user) {
                        return $scope.data.submission.user;
                    }
                };

                $scope.searchUserDone = function(user) {
                    if (user) {
                        $http.post("admin/api/mediasubmission/reassign", {msid: $scope.data.submission.id, userid: user.id})
                        .then(function() {
                            $scope.data.submission.user = user;
                            //
                            // TODO: Now loop through all the photos and reassign the user.
                            //
                            $scope.searchingSubmitter = false;
                        }, $exceptionHandler);
                    } else {
                        $scope.searchingSubmitter = false;
                    }
                };

                $scope.dateStringFromRest = function(date) {
                    return wbDateUtils.dateStringFromRest(date);
                };

                $scope.editEncounterDone = function(encdata) {
                    if (encdata) {
                        attachEncounter(encdata, $scope.data.activeSurveyEnc);
                    }
                    $scope.data.module.encounterEdit = null;
                    $scope.data.activeSurveyEnc = null;
                    $scope.data.activeEncData = null;
                };

                function photosRemovedFromEncounter(photos) {
                    //
                    // Decrease the numencs for these photos by one.
                    //
                    photos.forEach(function(item) {
                        $scope.numencs[item.id]--;
                    });
                }

                $scope.encounterDeleted = function(encdata) {
                    //
                    // Remove photos from our number counters.
                    //
                    photosRemovedFromEncounter(encdata.photos);

                    //
                    // Then remove the encounter from the lists.
                    //
                    $scope.data.encs = $scope.data.encs.filter(function(item) {
                        return (item.encounter.id !== encdata.encounter.id);
                    });

                    $scope.data.surveyEncs.forEach(function(surveyEnc) {
                        surveyEnc.encs = surveyEnc.encs.filter(function(item) {
                            return (item.encounter.id !== encdata.encounter.id);
                        });
                    });

                    $scope.data.module.encounterEdit = null;
                    $scope.data.activeEncData = null;
                };

                $scope.encounterPhotosDetached = function(photos) {
                    photosRemovedFromEncounter(photos);
                };

                $scope.searchEncounter = function(survey) {
                    if (survey) {
                        $scope.searchEncForSurvey = true;
                        $scope.data.activeSurveyEnc = survey;
                    }

                    $scope.data.module.encounterEdit = null;
                    // This one
                    $scope.data.module.encounterSearch = true;
                    $scope.data.module.surveyEdit = null;
                    $scope.data.module.surveySearch = false;
                };

                $scope.searchEncounterDone = function(encounter) {
                    $scope.data.module.encounterSearch = false;

                    if (encounter) {
                        wbEncounterUtils.getEncData(encounter)
                        .then(function(encdata) {
                            if ($scope.searchEncForSurvey) {
                                attachEncounter(encdata, $scope.data.activeSurveyEnc);
                                $scope.searchEncForSurvey = false;
                            } else {
                                attachEncounter(encdata);
                            }
                        });
                    }
                };

                $scope.activeEle = function(ele){
                    $scope.active = {
                            survey: false,
                            surveyEnc: false,
                            encounter: false
                    };
                    switch(ele){
                        case 'survey': $scope.active.survey = true;
                            break;
                        case 'surveyEnc': $scope.active.surveyEnc = true;
                            break;
                        case 'encounter': $scope.active.encounter = true;
                            break;
                    }
                    if($scope.active.encounter){
                        angular.forEach($scope.encOpen, function(item, key){
                           $scope.encOpen[key] = false;
                        });
                    }
                };

                function editEncounter(encdata, surveyEnc) {
                    $scope.data.module.encounterEdit = encdata;
                    $scope.data.activeEncData = encdata;
                    $scope.data.activeSurveyEnc = surveyEnc;

                    $scope.data.module.encounterSearch = false;
                    $scope.data.module.surveyEdit = null;
                    $scope.data.module.surveySearch = false;
                }

                function incPhotoNumEncs(newphotos) {
                    //
                    // Now increase the numencs for these photos by one.
                    //
                    newphotos.forEach(function(item) {
                        $scope.numencs[item.id]++;
                    });
                }

                $scope.addEncounter = function(surveyEnc, newSurveyEnc) {
                    if (surveyEnc && $scope.data.activeEncData && !newSurveyEnc) {
                        attachEncounter($scope.data.activeEncData, surveyEnc);
                        $scope.data.activeEncData = null;
                        return;
                    }

                    if (newSurveyEnc) {
                        $scope.data.activeSurveyEnc = surveyEnc;
                        $scope.data.newSurveyEnc = true;
                    }

                    wbEncounterUtils.createNewEncData($scope.data.selectedimgs.selected, $scope.data.submission)
                    .then(function(encdata) {
                        incPhotoNumEncs(encdata.photos);
                        editEncounter(encdata, surveyEnc);
                    }, $exceptionHandler);
                };

                $scope.showDialog = function(msg){
                    alertplus.alert(msg);
                };

                $scope.selectEncounter = function($event, encdata, surveyEnc) {
                    wbEncounterUtils.getMedia(encdata)
                    .then(function() {
                        editEncounter(encdata, surveyEnc);
                    });
                    //
                    // Need to keep it from clicking through to the containing survey.
                    //
                    $event.stopPropagation();
                };

                function editSurvey(surveypart) {
                    $scope.data.module.encounterEdit = null;
                    $scope.data.module.encounterSearch = false;
                    // This one
                    $scope.data.module.surveyEdit = surveypart;
                    $scope.data.module.surveySearch = false;
                }

                $scope.selectSurvey = function(surveyEnc, noEdit, index) {
                    $scope.data.activeSurveyEnc = surveyEnc;
                    angular.forEach($scope.encOpen, function(val, key){
                        if (key !== index) {
                            $scope.encOpen[key] = false;
                        }
                    });
                    if (noEdit) {
                        return;
                    }
                    editSurvey(surveyEnc.surveypart);
                };

                $scope.searchSurvey = function() {
                    $scope.data.module.encounterEdit = null;
                    $scope.data.module.encounterSearch = false;
                    $scope.data.module.surveyEdit = null;
                    // This one
                    $scope.data.module.surveySearch = true;
                };

                $scope.addSurvey = function() {
                    // wbSurveyUtils.createNewSurveyData()
                    // .then(function(surveyid){
                    //     editSurvey({surveyid: surveyid});
                    // });
                    editSurvey({surveyid:null});
                };

                function findSurveyEnc(surveypart) {
                    return wbLangUtils.findInArray($scope.data.surveyEncs, function(item) {
                        return (item.surveypart.part.surveyPartId === surveypart.part.surveyPartId);
                    });
                }

                $scope.editSurveyDone = function(surveypart) {
                    $scope.data.module.surveyEdit = null;

                    $scope.active.survey = false;

                    if (!surveypart) {
                        return;
                    }

                    var surveyEnc = findSurveyEnc(surveypart);
                    if (! surveyEnc) {
                        addSurveyEncounters(surveypart);
                    }
                };

                $scope.searchSurveyDone = function(surveypart) {
                    $scope.data.module.surveySearch = false;
                    if (! surveypart) {
                        return;
                    }

                    var surveyEnc = findSurveyEnc(surveypart);
                    //
                    // If already in the list then there is nothing to do,
                    // the user simply searched for a surveypart they already have.
                    //
                    if (surveyEnc) {
                        return;
                    }

                    //
                    // Look for any encounters attached to this survey already
                    //
                    $http.get("api/survey/encounters/" + surveypart.part.surveyPartId)
                    .then(function(result) {
                        addSurveyEncounters(surveypart, result.data);
                    }, $exceptionHandler);
                };

                //=================================
                // START wb-thumb-box
                //=================================
                $scope.performAction = function(code, photos) {
                    if (!photos) {
                        return;
                    }

                    switch (code) {
                    case "add": {
                        if (!$scope.data.activeEncData) {
                            alertplus.alert("please choose an encounter to add these to.");
                            return;
                        } else if(!$scope.data.activeEncData.encounter.id){
                            alertplus.alert("Please save your current encounter before adding images.");
                            return;
                        }

                        wbEncounterUtils.getMedia($scope.data.activeEncData)
                        .then(function() {
                            //
                            // Filter photos based on one's that are already attached to this encounter.
                            //
                            var currentPhotos = $scope.data.activeEncData.photos;
                            var newphotos = photos.filter(function(item) {
                                for (var ii = 0; ii < currentPhotos.length; ii++) {
                                    if (item.id === currentPhotos[ii].id) {
                                        return false;
                                    }
                                }
                                return true;
                            });

                            function doneAddingPhotos() {
                                $scope.data.activeEncData.photos = currentPhotos.concat(newphotos);
                                incPhotoNumEncs(newphotos);
                            }

                            //
                            // If we haven't saved the encounter yet then just set the new photos on it.
                            //
                            if (!$scope.data.activeEncData.encounter.id) {
                                doneAddingPhotos();
                            } else {
                                wbEncounterUtils.addPhotos($scope.data.activeEncData.encounter, newphotos)
                                .then(doneAddingPhotos);
                            }
                        });
                        break;
                    }
                    case "del": {
                        var photoids = photos.map(function(photo) {
                            return photo.id;
                        });
                        var promise = $http.post("admin/api/mediasubmission/deletemedia", {submissionid: $scope.data.submission.id, mediaids: photoids})
                        .then(function() {
                            //
                            // Remove the numencs for these photos.
                            //
                            photos.forEach(function(item) {
                                delete $scope.numencs[item.id];
                            });
                        });
                        promise.catch($exceptionHandler);
                        return promise;
                    }}
                };
                //=================================
                // END wb-thumb-box
                //=================================

                function photoInNumEncs(encs, photo) {
                    var sum = 0;
                    function photoCheck(item) {
                        return (item.id === photo.id);
                    }
                    for (var ii = 0; ii < encs.length; ii++) {
                        if (wbLangUtils.existsInArray(encs[ii].photos, photoCheck)) {
                            sum++;
                        }
                    }
                    return sum;
                }

                function calcEncCounts() {
                    for (var ii = 0; ii < $scope.data.photos.length; ii++) {
                        var photo = $scope.data.photos[ii];
                        var sum = 0;
                        sum += photoInNumEncs($scope.data.encs, photo);

                        for (var jj = 0; jj < $scope.data.surveyEncs.length; jj++) {
                            sum += photoInNumEncs($scope.data.surveyEncs[jj].encs, photo);
                        }

                        $scope.numencs[photo.id] = sum;
                    }
                }

                $scope.editSubmission = function(submission) {
                    $scope.busy = $q.all([$http({url:"api/mediasubmission/photos/" + submission.id}),
                                   $http({url:"api/mediasubmission/encounters/" + submission.id})])
                    .then(function(results) {
                        $scope.data.submission = submission;
                        $scope.data.photos = results[0].data;

                        $scope.data.encs = results[1].data.encs || [];
                        $scope.data.surveyEncs = results[1].data.surveyEncounters || [];

                        calcEncCounts();
                    }, $exceptionHandler);
                };

                $scope.deleteSubmission = function(submission) {
                    return alertplus.confirm('Are you sure you want to delete the <b>entire</b> submission '+ submission.id +'?', "Delete Submission", true)
                    .then(function() {
                        $http.post("admin/api/mediasubmission/delete", {submissionid: submission.id})
                        .then(function() {
                            $scope.updateSubmissionData();
                            $scope.doneEditing();
                        }, $exceptionHandler);
                    });
                };

                $scope.timeToDate = function(time) {
                    return wbDateUtils.timeToDateString(time);
                };

                $scope.timeFromRest = function(time) {
                    return wbDateUtils.formatTimeArrayToString(time);
                };

                $scope.dateFromRest = function(date) {
                    return wbDateUtils.dateStringFromRest(date);
                };

                $scope.msGridOptions = {
                    columnDefs:
                        [{headerName: "",
                          field: "id",
                          width: 24,
                          template: '<a href="javascript:;" ng-click="editSubmission(data)"><md-icon md-svg-icon="table-edit"></md-icon></a>'
                         },
                         {headerName: "Submitted",
                          field: "timeSubmitted",
                          cellRenderer: function(params) {
                              return $scope.timeToDate(params.value);
                          },
                          sort: 'desc'
                         },
                         {headerName: "Submitted By",
                             field: "user",
                             cellRenderer: function(params) {
                                 if (!params.value) {
                                     return null;
                                 }
                                 if (params.value.avatar) {
                                     return '<img width="*" height="24px" src="' + params.value.avatar + '"/><span class="ml-10">'
                                            + params.value.displayName + "</span>";
                                 }
                                 return params.value.displayName;
                             },
                             rowHeight: 24
                         },
                         {headerName: "Survey ID", field: "submissionid"},
                         {headerName: "Description", field: "description"},
                         {headerName: "Location", field: "verbatimLocation"},
                         {headerName: "Status", field: "status"}],
                    rowData: null,
                    enableServerSideSorting: true,
                    //enableSorting: false,
                    // this pins it such that you can't scroll the page in that spot too
                    // which when you have no scroll bar in the page is annoying because then no scrolling either way
                    //pinnedColumnCount: 3,
                    sortingOrder: ['desc', 'asc'],
                    angularCompileRows: true
                };

                $scope.doneEditing = function() {
                    $scope.msGridOptions.api.refreshView();
                    $scope.data.submission = null;

                    $scope.data.module.encounterEdit = null;
                    $scope.data.module.encounterSearch = false;
                    $scope.data.module.surveyEdit = null;
                    $scope.data.module.surveySearch = false;

                    $scope.data.photos = null;
                    $scope.data.encs = [];
                    $scope.data.surveyEncs = [];
                    $scope.data.activeEncData = null;
                    $scope.data.activeSurveyEnc = null;
                };

                // Source: http://www.ag-grid.com/angular-grid-pagination/index.php
                function sortSubmissionData(sortModel, data) {
                    // do an in memory sort of the data, across all the fields
                    var resultOfSort = data.slice();
                    resultOfSort.sort(function(a,b) {
                        for (var k = 0; k<sortModel.length; k++) {
                            var sortColModel = sortModel[k];
                            var valueA = a[sortColModel.colId];
                            var valueB = b[sortColModel.colId];

                            // this filter didn't find a difference, move onto the next one
                            if (valueA === valueB) {
                                continue;
                            }

                            var sortDirection = sortColModel.sort === 'asc' ? 1 : -1;
                            if (valueA > valueB) {
                                return sortDirection;
                            }
                            else {
                                return sortDirection * -1;
                            }
                        }
                        // no filters found a difference
                        return 0;
                    });
                    return resultOfSort;
                }

                var dataSource = {
                    pageSize: 20,
                    getRows: function(args) {
                        if(args.sortModel) {
                            $scope.rowData = sortSubmissionData(args.sortModel, $scope.rowData);
                        }
                        setTimeout(function() {
                            args.successCallback($scope.rowData.slice(args.startRow, args.endRow), $scope.rowData.length);
                        }, 100);
                    }
                };

                function setDataSource(dataSource) {
                    $scope.msGridOptions.api.setDatasource(dataSource);
                }

                $scope.updateSubmissionData = function(status) {
                    var statusUrl;

                    if(status) {
                        statusUrl = "api/mediasubmission/get/status/" + status;
                    } else {
                        statusUrl = "api/mediasubmission/get/uncompleted";
                    }

                    $http({url: statusUrl})
                    .then(function(result) {
                        $scope.rowData = result.data;
                        setDataSource(dataSource);
                    }, $exceptionHandler);
                };

                $scope.setStatus = function(status) {
                    if(!status){
                        status = "null";
                    }
                    $http.post("admin/api/mediasubmission/setstatus/"+$scope.data.submission.id, status)
                    .then(function(){
                        //filter out changed status from current grid that isnt set to status = all
                        if($scope.searchStatus !== '*'){
                            for(var nn = 0; nn < $scope.rowData.length; nn++) {
                                if($scope.rowData[nn].id === $scope.data.submission.id) {
                                    $scope.rowData.splice(nn,1);
                                    setDataSource(dataSource);
                                    $scope.msGridOptions.api.refreshView();
                                }
                            }
                        }
                    });
                };

                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {

                    $scope.active = {
                            survey: false,
                            surveyEnc: false,
                            encounter: false
                        };

                    if ($scope.data.module.encounterEdit) {
                        $scope.data.module.encounterEdit = null;
                    } else if ($scope.data.module.encounterSearch) {
                        $scope.data.module.encounterSearch = false;
                    } else if ($scope.data.module.surveyEdit) {
                        $scope.data.module.surveyEdit = null;
                    } else if ($scope.data.module.surveySearch) {
                        $scope.data.module.surveySearch = false;
                    } else {
                        $scope.doneEditing();
                    }
                };

                $scope.cmdEnter = function() {
                    // do nothing
                    // want this here to override any parent scope cmdEnter event though.
                };

                //
                // Finally, kick us off.
                //
                $scope.updateSubmissionData();
            }
        };
    }]
);
