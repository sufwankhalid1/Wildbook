wildbook.app.directive("wbMediaSubmissionAdmin",
    ["$http", "$q", "$exceptionHandler", "wbDateUtils", "wbLangUtils", "wbEncounterUtils",
     function ($http, $q, $exceptionHandler, wbDateUtils, wbLangUtils, wbEncounterUtils) {
        return {
            restrict: 'E',
            scope: {},
            templateUrl: 'util/render?j=partials/media_submission_admin',
            replace: true,
            controller: function($scope) {
                $scope.data = {
                    submission: null,
                    photos: null,
                    encounters: [],
                    surveyEncs: [],
                    activeEncounter: null,
                    module: {
                        encounterEdit: null,
                        encounterSearch: false,
                        surveyEdit: null,
                        surveySearch: false
                    },
                    tbActions: [{
                        code: "add",
                        shortcutKeyCode: 65,
                        glyphicon: "plus",
                        tooltip: "Add to active encounter"
                    },
                    {
                        code: "del",
                        shortcutKeyCode: 68,
                        type: "danger",
                        glyphicon: "trash",
                        tooltip: "Delete",
                        confirm: { message: "Are you sure you want to delete selected images?"}
                    }]
                };
            
                function attachEncounter(encounter, surveyEnc) {
                    if (! encounter) {
                        return;
                    }
                    
                    if (surveyEnc) {
                        if (surveyEnc.encounters) {
                            if (wbLangUtils.existsInArray(surveyEnc.encounters, function(item) {
                                return item.id === encounter.id
                            })) {
                                //
                                // Just return as we already have this encounter attached to this survey.
                                //
                                return;
                            };
                        } else {
                            //
                            // Create empty array for encounters so that we can add one below.
                            //
                            surveyEnc.encounters = [];
                        }
                        // Call to add encounter to survey.
                        $http.post("obj/survey/addencounter",
                                   {surveypartid: surveyEnc.surveypart.part.surveyPartId, encounterid: encounter.id})
                        .then(function() {
                            surveyEnc.encounters.push(encounter);
                        });
                    } else {
                        if (! wbLangUtils.existsInArray($scope.data.encounters, function(item) {
                            return (item.id === encounter.id);
                        })) {
                            $scope.data.encounters.push(encounter);
                        };
                    }
                }
            
                function addSurveyEncounters(surveypart, encounters) {
                    $scope.data.surveyEncs.push({surveypart: surveypart, encounters: encounters});
                }
            
                $scope.dateStringFromRest = function(date) {
                    return wbDateUtils.dateStringFromRest(date);
                }
            
                $scope.editEncounterDone = function(encounter) {
                    if (encounter) {
                        attachEncounter(encounter, $scope.data.activeSurveyEnc);
                    }
                    $scope.data.module.encounterEdit = null;
                    $scope.data.activeSurveyEnc = null;
                }
                
                $scope.searchEncounter = function() {
                    $scope.data.module.encounterEdit = null;
                    // This one
                    $scope.data.module.encounterSearch = true;
                    $scope.data.module.surveyEdit = null;
                    $scope.data.module.surveySearch = false;
                }
                
                $scope.searchEncounterDone = function(encounter) {
                    $scope.data.module.encounterSearch = false;
                    attachEncounter(encounter);
                }
                
                function editEncounter(encounter, surveyEnc) {
                    $scope.data.module.encounterEdit = encounter;
                    $scope.data.activeEncounter = encounter;
                    $scope.data.activeSurveyEnc = surveyEnc;
                    
                    $scope.data.module.encounterSearch = false;
                    $scope.data.module.surveyEdit = null;
                    $scope.data.module.surveySearch = false;
                }
                
                $scope.addEncounter = function(surveyEnc) {
                    editEncounter(wbEncounterUtils.createNewEncounter(), surveyEnc);
                }
                
                $scope.selectEncounter = function($event, encounter, surveyEnc) {
                    wbEncounterUtils.getMedia(encounter)
                    .then(function() {
                        editEncounter(encounter, surveyEnc);
                    });
                    //
                    // Need to keep it from clicking through to the containing survey.
                    //
                    $event.stopPropagation();
                }
                
                function editSurvey(surveypart) {
                    $scope.data.module.encounterEdit = null;
                    $scope.data.module.encounterSearch = false;
                    // This one
                    $scope.data.module.surveyEdit = surveypart;
                    $scope.data.module.surveySearch = false;
                }
                
                $scope.selectSurvey = function(surveyEnc) {
                    $scope.data.activeSurveyEnc = surveyEnc;
                    editSurvey(surveyEnc.surveypart);
                }
                
                $scope.searchSurvey = function() {
                    $scope.data.module.encounterEdit = null;
                    $scope.data.module.encounterSearch = false;
                    $scope.data.module.surveyEdit = null;
                    // This one
                    $scope.data.module.surveySearch = true;
                }

                $scope.addSurvey = function() {
                    editSurvey({});
                }
                
                function findSurveyEnc(surveypart) {
                    return wbLangUtils.findInArray($scope.data.surveyEncs, function(item) {
                        return (item.surveypart.part.surveyPartId === surveypart.part.surveyPartId);
                    });
                }
                
                $scope.editSurveyDone = function(surveypart) {
                    $scope.data.module.surveyEdit = null;

                    if (!surveypart) {
                        return;
                    }
                    
                    var surveyEnc = findSurveyEnc(surveypart);
                    if (! surveyEnc) {
                        addSurveyEncounters(surveypart);
                    }
                }
                
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
                    $http.get("obj/survey/encounters/" + surveypart.part.surveyPartId)
                    .then(function(result) {
                        addSurveyEncounters(surveypart, result.data);
                    }, $exceptionHandler)
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
                    case "add": {
                        if (!$scope.data.activeEncounter) {
                            alertplus.alert("No active encounter selected.");
                            return;
                        }
                        wbEncounterUtils.getMedia($scope.data.activeEncounter)
                        .then(function() {
                            $http.post("obj/encounter/addmedia/" + $scope.data.activeEncounter.id, photoids)
                            .then(function() {
                                $scope.data.activeEncounter.photos = $scope.data.activeEncounter.photos.concat(photos);
                            });
                        });
                        break;
                    }
                    case "del": {
                        return $http.post("obj/mediasubmission/deletemedia", {submissionid: $scope.data.submission.id, mediaids: photoids})
                        .catch($exceptionHandler);
                    }}
                }
                //=================================
                // END wb-thumb-box
                //=================================
            
                $scope.editSubmission = function(submission) {
                    return $q.all([$http({url:"obj/mediasubmission/photos/" + submission.id}),
                                   $http({url:"obj/mediasubmission/encounters/" + submission.id})])
                    .then(function(results) {
                        $scope.data.submission = submission;
                        $scope.data.photos = results[0].data;
            
                        $scope.data.encounters = results[1].data.encounters || [];
                        $scope.data.surveyEncs = results[1].data.surveyEncounters || [];
                    }, $exceptionHandler);
                }
            
                $scope.deleteSubmission = function(submission) {
                    return alertplus.confirm('Are you sure you want to delete the <b>entire</b> submission '+ submission.id +'?', "Delete Submission", true)
                    .then(function() {
                        $http.post("obj/mediasubmission/delete", {submissionid: submission.id})
                        .then(function() {
                            updateSubmissionData();
                            $scope.doneEditing();
                        }, $exceptionHandler);
                    });
                };
            
                $scope.timeToDate = function(time) {
                    return moment(time).format('lll');
                };
            
                $scope.msGridOptions = {
                    columnDefs:
                        [{headerName: "",
                          field: "id",
                          width: 24,
                          template: '<a href="javascript:;" ng-click="editSubmission(data)"><i class="glyphicon glyphicon-edit"></i></a>'
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
                                 if (params.value) {
                                     return params.value.displayName;
                                 }
                                 return null;
                             }
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
                    // Why do we need to update here?
                    //updateSubmissionData();
                    $scope.data.submission = null;
                    
                    $scope.data.module.encounterEdit = null;
                    $scope.data.module.encounterSearch = false;
                    $scope.data.module.surveyEdit = null;
                    $scope.data.module.surveySearch = false;
                    
                    $scope.data.photos = null;
                    $scope.data.encounters = [];
                    $scope.data.surveyEncs = [];
                    $scope.data.activeEncounter = null;
                    $scope.data.activeSurveyEnc = null;
                };
            
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
                }
            
                function setDataSource() {
                    $scope.msGridOptions.api.setDatasource(dataSource);
                }
            
                function updateSubmissionData() {
                    $http({url:"obj/mediasubmission/get/status"})
                    .then(function(result) {
                        $scope.rowData = result.data;
                        $scope.msGridOptions.api.setDatasource(dataSource);
                    }, $exceptionHandler);
                }
            
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
                            if (valueA==valueB) {
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
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.doneEditing();
                }
                
                $scope.cmdEnter = function() {
                    // do nothing
                    // want this here to override any parent scope cmdEnter event though.
                }
            
                //
                // Finally, kick us off.
                //
                updateSubmissionData();
            }
        }
    }]
);
